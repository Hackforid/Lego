package com.smailehacker.lego.compiler;

import com.smilehacker.lego.annotation.Component;
import com.smilehacker.lego.annotation.LegoField;
import com.smilehacker.lego.annotation.LegoIndex;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


/**
 * Created by zhouquan on 17/2/18.
 */

public class FileMaker {
    private Messager mMessager;
    private Elements mElements;
    private Types mTypes;

    public FileMaker(Types types, Elements elements, Messager messager) {
        mTypes = types;
        mElements = elements;
        mMessager = messager;
    }

    public void make(Filer filer, RoundEnvironment roundEnvironment) {
        TypeName ILegoFactoryName = ClassName.get("com.smilehacker.lego", "ILegoFactory");

        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder("LegoFactory");
        codeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        codeBuilder.addSuperinterface(ILegoFactoryName);
        codeBuilder.addMethod(getModelMethod(roundEnvironment));
        codeBuilder.addMethod(getModelIndexMethod(roundEnvironment));
        codeBuilder.addMethod(getMethodModelEquals(roundEnvironment));
        JavaFile javaFile = JavaFile.builder("com.smilehacker.lego", codeBuilder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private MethodSpec getModelMethod(RoundEnvironment roundEnvironment) {
        TypeName legoComponentClassName = ClassName.get("com.smilehacker.lego", "LegoComponent");
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getModelClass")
                .addParameter(legoComponentClassName, "component")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Class.class);

        boolean isCheckStart = false;
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(Component.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", Component.class.getSimpleName());
                return null;
            }
            TypeElement element = (TypeElement) annotatedElement;
            DeclaredType declaredType = (DeclaredType) element.getSuperclass();
            TypeMirror modelTypeMirror = declaredType.getTypeArguments().get(1);
            if (!isCheckStart) {
                isCheckStart = true;
                methodBuilder
                        .beginControlFlow("if (component.getClass().equals($T.class))", element);
            } else {
                methodBuilder
                        .nextControlFlow("else if (component.getClass().equals($T.class))", element);
            }


            methodBuilder
                    .addStatement("return $T.class", modelTypeMirror);
        }
        if (isCheckStart) {
            methodBuilder.endControlFlow();
        }
        methodBuilder
                .addStatement("return null");
        return methodBuilder.build();
    }

    private MethodSpec getModelIndexMethod(RoundEnvironment roundEnvironment) {
        TypeName legoModelClassName = ClassName.get("com.smilehacker.lego", "LegoModel");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("getModelIndex")
                .addParameter(legoModelClassName, "model")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Object.class);

        boolean isCheckStart = false;
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoIndex.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoIndex.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();
            if (!isCheckStart) {
                isCheckStart = true;
                builder.beginControlFlow("if ($T.class.equals(model.getClass()))", parent);
            } else {
                builder.nextControlFlow("else if ($T.class.equals(model.getClass()))", parent);
            }
            builder
                    .addStatement("return (($T) model).$N", parent, element.getSimpleName());
        }
        if (isCheckStart) {
            builder.endControlFlow();
        }
        builder.addStatement("return null");
        return builder.build();
    }


    private MethodSpec getaMethodModelEquals(RoundEnvironment roundEnvironment) {
        TypeName legoModelClassName = ClassName.get("com.smilehacker.lego", "LegoModel");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("isModelEquals")
                .addParameter(legoModelClassName, "model0")
                .addParameter(legoModelClassName, "model1")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(int.class);
        builder.beginControlFlow("if (!model0.getClass().equals(model1.getClass()))");
        builder.addStatement("return -1");
        builder.endControlFlow();

        HashMap<TypeElement, List<VariableElement>> fieldMap = new HashMap<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoField.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoField.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();

            List<VariableElement> list = fieldMap.get(parent);
            if (list == null) {
                list = new LinkedList<>();
                fieldMap.put(parent, list);
            }
            list.add(element);
        }

        for (Map.Entry<TypeElement, List<VariableElement>> entry: fieldMap.entrySet()) {
            TypeElement parent = entry.getKey();
            builder.beginControlFlow("if ($T.class.equals(model0.getClass()))", parent);
            builder.addStatement("$T m0 = ($T) model0", parent, parent);
            builder.addStatement("$T m1 = ($T) model1", parent, parent);
            for (VariableElement element: entry.getValue()) {
                if (isPrimaryField(element)) {
                    builder.beginControlFlow("if (m0.$N != m1.$N)", element.getSimpleName(), element.getSimpleName());
                } else {
                    Name name = element.getSimpleName();
                    builder.beginControlFlow("if (m0.$N != null && m1.$N != null && !m0.$N.equals(m1.$N))", name, name, name, name);
                }
                builder.addStatement("return -1");
                builder.endControlFlow();
            }
            builder.addStatement("return 1");
            builder.endControlFlow();
        }

        builder.addStatement("return 0");
        return builder.build();
    }

    private boolean isPrimaryField(VariableElement element) {
        String type = element.asType().toString();
        if ("int".equals(type)
                || "float".equals(type)
                || "double".equals(type)
                || "short".equals(type)
                || "byte".equals(type)
                || "long".equals(type)
                || "char".equals(type)
                || "boolean".equals(type)) {
            return true;
        } else {
            return false;
        }
    }

    private MethodSpec getMethodModelEquals(RoundEnvironment roundEnvironment) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("isModelEquals")
                .addParameter(Object.class, "model0")
                .addParameter(Object.class, "model1")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(boolean.class);

        builder.beginControlFlow("if (model0 == model1)");
        builder.addStatement("return true");
        builder.endControlFlow();

        builder.beginControlFlow("if (model0 == null && model1 == null)");
        builder.addStatement("return true");
        builder.nextControlFlow("else if (model0 == null || model1 == null)");
        builder.addStatement("return false");
        builder.endControlFlow();

        builder.beginControlFlow("if (!model0.getClass().equals(model1.getClass()))");
        builder.addStatement("return false");
        builder.endControlFlow();

        // if object is List
        builder.beginControlFlow("if (model0 instanceof $T)", List.class);
            builder.addStatement("$T m0 = ($T) model0", List.class, List.class);
            builder.addStatement("$T m1 = ($T) model1", List.class, List.class);
            builder.beginControlFlow("if (m0.size() != m1.size())");
                builder.addStatement("return false");
            builder.nextControlFlow("else");
                builder.beginControlFlow("for(int i = 0, len = m0.size(); i < len; i++)");
                    builder.beginControlFlow("if (!isModelEquals(m0.get(i), m1.get(i)))");
                        builder.addStatement("return false");
                    builder.endControlFlow();
                    builder.addStatement("return true");
                builder.endControlFlow();
            builder.endControlFlow();
        builder.endControlFlow();

        // if object is Array
        builder.beginControlFlow("if (model0.getClass().isArray())");
            builder.addStatement("int len0 = $T.getLength(model0), len1 = $T.getLength(model1)", Array.class, Array.class);
            builder.beginControlFlow("if (len0 != len1)");
                builder.addStatement("return false");
            builder.nextControlFlow("else");
                builder.beginControlFlow("for(int i = 0; i < len0; i++)");
                    builder.beginControlFlow("if (!isModelEquals($T.get(model0, i), $T.get(model1, i)))", Array.class, Array.class);
                        builder.addStatement("return false");
                    builder.endControlFlow();
                    builder.addStatement("return true");
                builder.endControlFlow();
            builder.endControlFlow();
        builder.endControlFlow();

        HashMap<TypeElement, List<VariableElement>> fieldMap = new HashMap<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoField.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoField.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();

            List<VariableElement> list = fieldMap.get(parent);
            if (list == null) {
                list = new LinkedList<>();
                fieldMap.put(parent, list);
            }
            list.add(element);
        }

        boolean isGenerateIf = false;
        for (Map.Entry<TypeElement, List<VariableElement>> entry: fieldMap.entrySet()) {
            TypeElement parent = entry.getKey();
            if (!isGenerateIf) {
                isGenerateIf = true;
                builder.beginControlFlow("if ($T.class.equals(model0.getClass()))", parent);
            } else {
                builder.nextControlFlow("else if ($T.class.equals(model0.getClass()))", parent);
            }
            builder.addStatement("$T m0 = ($T) model0", parent, parent);
            builder.addStatement("$T m1 = ($T) model1", parent, parent);

            for (VariableElement element: entry.getValue()) {
                Name name = element.getSimpleName();
                if (isPrimaryField(element)) {
                    builder.beginControlFlow("if (m0.$N != m1.$N)", name, name);
                    builder.addStatement("return false");
                    builder.endControlFlow();
                } else {
                    builder.beginControlFlow("if (!isModelEquals(m0.$N, m1.$N))", name, name);
                    builder.addStatement("return false");
                    builder.endControlFlow();
                }
            }
            builder.addStatement("return true");
        }
        if (isGenerateIf) {
            builder.endControlFlow();
        }

        // generate default equal
        builder.addStatement("return model0.equals(model1)");
        return builder.build();
    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void log(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args),
                e);
    }
}
