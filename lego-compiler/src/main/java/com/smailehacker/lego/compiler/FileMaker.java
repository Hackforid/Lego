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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


/**
 * Created by zhouquan on 17/2/18.
 */

public class FileMaker {

    public static void makeComponentFactory(Filer filer, List<ComponentAnnotatedClass> classList, List<VariableElement> indexEles) {
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
        codeBuilder.addMethod(getIndexMethod(indexEles));
        JavaFile javaFile = JavaFile.builder("com.smilehacker.lego", codeBuilder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec getIndexMethod(List<VariableElement> elements) {
        TypeName legoModelClassName = ClassName.get("com.smilehacker.lego", "LegoModel");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("getModelIndex")
                .addParameter(legoModelClassName, "model")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Object.class);

        for (VariableElement element: elements) {
            TypeMirror fieldType = element.asType();
            TypeElement parent = (TypeElement) element.getEnclosingElement();
            builder.beginControlFlow("if ($T.class.equals(model.getClass()))", parent)
                    .addStatement("return (($T) model).$N", parent, element.getSimpleName())
                    .endControlFlow();
        }
        builder.addStatement("return null");

        return builder.build();
    }
}
