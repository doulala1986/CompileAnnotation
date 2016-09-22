package com.compile.components.bindview;

import com.annotaions.bindview.BindView;
import com.compile.base.BaseFieldProcessor;
import com.compile.utils.AnnotationUtil;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.TypeName.VOID;

/**
 * Created by doulala on 16/9/22.
 */


@AutoService(Processor.class)//设置成自动编译
public class BindViewProcessor extends BaseFieldProcessor<FieldViewBinding> {

    private static final ClassName VIEW_BINDER = ClassName.get("com.doulala.annotation.components.bindview", "ViewBinder");

    private static final String BINDING_CLASS_SUFFIX = "$ViewBinder";//生成类的后缀 以后会用反射去取

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

    }

    @Override
    protected Class<? extends Annotation> CaptureAnnotationClass() {
        return BindView.class;
    }

    @Override
    protected boolean isInValidateElement(TypeElement enclosingElement, Element element) {


        if (!AnnotationUtil.isSubtypeOfType(element.asType(), "android.view.View") && !AnnotationUtil.isInterface(element)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("@%s fields must extend from View or be an interface. (%s.%s)",
                    BindView.class.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName()));
            return true;
        }

        return false;
    }

    @Override
    protected FieldViewBinding generateItem(ProcessingEnvironment evn, TypeElement enclosedElement, Element element, String packageName, TypeName targetType, String fieldName, TypeMirror fieldType) {
        int id = element.getAnnotation(BindView.class).value();//取值
        FieldViewBinding fieldViewBinding = new FieldViewBinding(fieldName, fieldType, id);
        return fieldViewBinding;
    }

    @Override
    protected TypeSpec generateTypeAndMethods(TypeElement enclosedElement, List<FieldViewBinding> list) {
        String packageName = AnnotationUtil.getPackageName(processingEnv, enclosedElement);
        ClassName targetClassName = bestGuess(AnnotationUtil.getClassName(enclosedElement, packageName));
        TypeSpec.Builder builder = TypeSpec.classBuilder(AnnotationUtil.getClassName(enclosedElement, packageName).toString() + BINDING_CLASS_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", targetClassName))
                .addSuperinterface(ParameterizedTypeName.get(VIEW_BINDER, targetClassName))
                .addMethod(createMethod(list, targetClassName));


        return builder.build();
    }


    /**
     * @param list
     * @param
     * @return
     * @Override public void bind(TargetClassName XXX){
     * <p>
     * <p>
     * <p>
     * }
     */
    private MethodSpec createMethod(List<FieldViewBinding> list, ClassName targetClassName) {

        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(VOID)
                .addParameter(targetClassName, "target", Modifier.FINAL);

        for (FieldViewBinding field : list) {

            String format = "target.$L=($T) target.findViewById($L)";
            ClassName className= bestGuess(field.getType().toString());
            builder.addStatement(format, field.getName(),className, field.getResId());

        }
        return builder.build();
    }
}