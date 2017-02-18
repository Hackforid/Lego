package com.smailehacker.lego.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by zhouquan on 17/2/18.
 */

public class ComponentAnnotatedClass {
    public TypeElement element;
    public TypeMirror modelTypeMirror;

    public ComponentAnnotatedClass(TypeElement element) {
        this.element = element;

        DeclaredType declaredType = (DeclaredType) element.getSuperclass();
        modelTypeMirror = declaredType.getTypeArguments().get(1);
    }

    @Override
    public String toString() {
        return String.format("%s{model=%s}", element.getQualifiedName(), modelTypeMirror.toString());
    }
}
