package com.compile.base;

import com.annotaions.bindview.BindView;
import com.compile.utils.AnnotationUtil;
import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.squareup.javapoet.ClassName.bestGuess;

/**
 * Created by doulala on 16/9/22.
 */

public abstract class BaseFieldProcessor<T extends BaseFieldAnnotationCollector> extends AbstractProcessor {

    protected Filer filer;
    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";//生成类的后缀 以后会用反射去取
    private static final ClassName VIEW_BINDER = ClassName.get("com.doulala.annotation.components.bindview", "ViewBinder");


    private Class<? extends Annotation> annotation;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        annotation = CaptureAnnotationClass();
    }

    protected abstract Class<? extends Annotation> CaptureAnnotationClass();

    protected abstract boolean isInValidateElement(TypeElement enclosedElement, Element element);

    protected abstract T generateItem(ProcessingEnvironment evn, TypeElement enclosedElement, Element element, String packageName, TypeName targetType, String fieldName, TypeMirror fieldType);

    protected abstract TypeSpec generateTypeAndMethods(TypeElement enclosedElement, List<T> list);

    /**
     * 设置需要处理的Annotation
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(annotation.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, List<T>> targetClassMap = new LinkedHashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

            if (!SuperficialValidation.validateElement(element))//对Element进行校验
                continue;


            /** 判断注解方式是否正确 */
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            boolean hasError = AnnotationUtil.isInaccessibleViaGeneratedCode(processingEnv, BindView.class, "fields", element)
                    || AnnotationUtil.isBindingInWrongPackage(processingEnv, BindView.class, element);

            if (isInValidateElement(enclosingElement, element)) {
                hasError = true;
                continue;
            }

            if (hasError) {
                continue;
            }

            //收集数据,保存在map里,key为Target,value是bindList
            List<T> list = targetClassMap.get(enclosingElement);

            if (list == null) {
                list = new ArrayList<>();
                targetClassMap.put(enclosingElement, list);
            }

            String packageName = AnnotationUtil.getPackageName(processingEnv, enclosingElement);//获取包名
            TypeName targetType = TypeName.get(enclosingElement.asType());//获取Target类型
            int id = element.getAnnotation(BindView.class).value();//取值
            String fieldName = element.getSimpleName().toString();
            TypeMirror fieldType = element.asType();//获取标签类型
            T object = generateItem(processingEnv, enclosingElement, element, packageName, targetType, fieldName, fieldType);
            list.add(object);
        }


        for (Map.Entry<TypeElement, List<T>> item : targetClassMap.entrySet()) {
            List<T> list = item.getValue();
            if (list == null || list.size() == 0) {
                continue;
            }
            TypeElement enclosingElement = item.getKey();
            String packageName = AnnotationUtil.getPackageName(processingEnv, enclosingElement);
            ClassName typeClassName = bestGuess(AnnotationUtil.getClassName(enclosingElement, packageName));

            TypeSpec result = generateTypeAndMethods(enclosingElement, list);


//            TypeSpec.Builder result = TypeSpec.classBuilder(AnnotationUtil.getClassName(enclosingElement, packageName) + BINDING_CLASS_SUFFIX)
//                    .addModifiers(Modifier.PUBLIC)
//                    .addTypeVariable(TypeVariableName.get("T", typeClassName))
//                    .addSuperinterface(ParameterizedTypeName.get(VIEW_BINDER, typeClassName));
//            result.addMethod(createBindMethod(list, typeClassName));
            try {
                JavaFile.builder(packageName, result)
                        .addFileComment(" This codes are generated automatically. Do not modify!")
                        .build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

//    /**
//     * 写入方法
//     *
//     * @param list
//     * @param typeClassName
//     * @return
//     */
//    private MethodSpec createBindMethod(List<FieldViewBinding> list, ClassName typeClassName) {
//        MethodSpec.Builder result = MethodSpec.methodBuilder("bind")
//                .addModifiers(Modifier.PUBLIC)
//                .returns(TypeName.VOID)
//                .addAnnotation(Override.class)
//                .addParameter(typeClassName, "target", Modifier.FINAL);
//
//        for (int i = 0; i < list.size(); i++) {
//            FieldViewBinding fieldViewBinding = list.get(i);
//
//            String packageString = fieldViewBinding.getType().toString();
////            String className = fieldViewBinding.getType().getClass().getSimpleName();
//            ClassName viewClass = bestGuess(packageString);
//            result.addStatement("target.$L=($T)target.findViewById($L)", fieldViewBinding.getName(), viewClass, fieldViewBinding.getResId());
//        }
//        return result.build();
//    }


    protected void print(Diagnostic.Kind kind, String message) {
        processingEnv.getMessager().printMessage(kind, message);
    }
}

