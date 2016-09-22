package com.compile.components.bindview;

import com.compile.base.BaseFieldAnnotationCollector;

import javax.lang.model.type.TypeMirror;

/**
 *  标签绑定的实体类
 */
final class FieldViewBinding  extends BaseFieldAnnotationCollector {
    private final int resId;

    public FieldViewBinding(String name, TypeMirror type, int resId) {
        super(name,type);
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }
}
