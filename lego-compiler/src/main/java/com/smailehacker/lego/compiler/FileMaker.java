package com.smailehacker.lego.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;


/**
 * Created by zhouquan on 17/2/18.
 */

public class FileMaker {

    public static void make(Filer filer, List<ComponentAnnotatedClass> classList) {
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder("LegoFactory");
        codeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        TypeName legoComponentClassName = ClassName.get("com.smilehacker.lego", "LegoComponent");

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getModelClass")
                .addParameter(legoComponentClassName, "component")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Class.class);

        for (ComponentAnnotatedClass annotatedClass: classList) {
            methodBuilder
                    .beginControlFlow("if (component.getClass().equals($T.class))", annotatedClass.element)
                    .addStatement("return $T.class", annotatedClass.modelTypeMirror)
                    .endControlFlow();
        }
        methodBuilder
                .addStatement("return null");

        codeBuilder.addMethod(methodBuilder.build());
        JavaFile javaFile = JavaFile.builder("com.smilehacker.lego", codeBuilder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
