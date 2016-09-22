package com.compile.base;

import javax.lang.model.type.TypeMirror;

/**
 * Created by doulala on 16/9/22.
 *
 * 保存标签装状态的实体类
 */

public class BaseFieldAnnotationCollector {
    private final String name;
    private final TypeMirror type;

    public BaseFieldAnnotationCollector(String name, TypeMirror type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }


    public TypeMirror getType() {
        return type;
    }


}
