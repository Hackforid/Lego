package com.smailehacker.lego.compiler;

import com.smilehacker.lego.annotation.Component;
import com.smilehacker.lego.annotation.LegoField;
import com.smilehacker.lego.annotation.LegoIndex;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String mModuleName;

    public FileMaker(Types types, Elements elements, Messager messager, String moduleName) {
        mTypes = types;
        mElements = elements;
        mMessager = messager;
        mModuleName = moduleName;
    }

    public void make(Filer filer, RoundEnvironment roundEnvironment) {
        TypeName ILegoFactoryName = ClassName.get("com.smilehacker.lego", "ILegoFactory");

        String className = "LegoFactory";
        if (mModuleName != null && !mModuleName.isEmpty()) {
            className = String.format("%s_%s", className, mModuleName);
        }
        TypeSpec.Builder codeBuilder = TypeSpec.classBuilder(className);
        codeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        codeBuilder.addSuperinterface(ILegoFactoryName);
//        codeBuilder.addMethod(getModelMethod(roundEnvironment));
        codeBuilder.addMethod(getDefineModels(roundEnvironment));
        codeBuilder.addMethod(getModelIndexMethod(roundEnvironment));
        codeBuilder.addMethod(getMethodModelEquals(roundEnvironment));
        codeBuilder.addMethod(getMethodModelEqualsByClass(roundEnvironment));
        codeBuilder.addMethod(getModelHash(roundEnvironment));
        codeBuilder.addMethod(getModelHashByClass(roundEnvironment));
        JavaFile javaFile = JavaFile.builder("com.smilehacker.lego.factory", codeBuilder.build()).build();
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

    class Pair<X, Y> {
        public X first;
        public Y second;
        public Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }
    }

    private MethodSpec getModelIndexMethod(RoundEnvironment roundEnvironment) {

        MethodSpec.Builder builder = MethodSpec.methodBuilder("getModelIndex")
                .addParameter(Object.class, "model")
                .addParameter(Class.class, "clazz")
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
                builder.beginControlFlow("if ($T.class.equals(clazz))", parent);
            } else {
                builder.nextControlFlow("else if ($T.class.equals(clazz))", parent);
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

        builder.beginControlFlow("if (model0 == null || model1 == null)");
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

        builder.addStatement("return isModelEquals(model0, model1, model0.getClass())");

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
    private void log(String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args)
                );
    }

    private MethodSpec getModelHash(RoundEnvironment roundEnvironment) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getModelHash")
                .addParameter(Object.class, "m")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(double.class);


        builder.beginControlFlow("if (m == null)");
        builder.addStatement("return 0d");
        builder.endControlFlow();

        builder.addStatement("return getModelHash(m, m.getClass())");
        return builder.build();
    }

    private MethodSpec getModelHashByClass(RoundEnvironment roundEnvironment) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getModelHash")
                .addParameter(Object.class, "m")
                .addParameter(Class.class, "clazz")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(double.class);


        builder.beginControlFlow("if (m == null)");
        builder.addStatement("return 0d");
        builder.endControlFlow();

        HashMap<TypeElement, List<VariableElement>> fieldMap = new HashMap<>();
        List<TypeElement> parents = new ArrayList<>();
        List<String> parentsName = new ArrayList<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoField.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoField.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();

            List<VariableElement> list = fieldMap.get(parent);
            if (list == null) {
                parents.add(parent);
                list = new LinkedList<>();
                fieldMap.put(parent, list);
                parentsName.add(parent.getQualifiedName().toString());
            }
            list.add(element);
        }

        // check start
        boolean isGenerateIf = false;
        for (TypeElement parent: parents) {
            if (!isGenerateIf) {
                isGenerateIf = true;
                builder.beginControlFlow("if ($T.class.equals(clazz))", parent);
            } else {
                builder.nextControlFlow("else if ($T.class.equals(clazz))", parent);
            }
            builder.addStatement("$T model = ($T) m", parent, parent);
            // body start
            List<VariableElement> fields = fieldMap.get(parent);
            if (fields == null || fields.isEmpty()) {
                builder.addStatement("return -1d");
                continue;
            } else {
                builder.addStatement("double hash = 0d");
            }

            for (VariableElement field: fields) {
                String type = field.asType().toString();
                if ("int".equals(type) || "float".equals(type) || "double".equals(type)
                        || "short".equals(type) || "long".equals(type) || "byte".equals(type)
                        || "char".equals(type)) {
                    builder.addStatement("hash += model.$N * 1000", field.getSimpleName());
                } else if ("boolean".equals(type)) {
                    builder.addStatement("hash += model.$N ? 2335: 1214", field.getSimpleName());
                } else {
                    String fieldClassName = ((TypeElement) mTypes.asElement(field.asType())).getQualifiedName().toString();
                    if (parentsName.contains(fieldClassName)) {
                        builder.addStatement("hash += getModelHash(model.$N)", field.getSimpleName());
                    } else {
                        builder.addStatement("hash += model.$N == null ? 12345 : model.$N.hashCode()", field.getSimpleName(), field.getSimpleName());
                    }
                }
            }
            builder.addStatement("return hash");

            // body end
        }

        if (isGenerateIf) {
            builder.endControlFlow();
        }
        // check end

        builder.addStatement("return -1d");

        return builder.build();
    }

    private MethodSpec getDefineModels(RoundEnvironment roundEnvironment) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getDefineModels")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ArrayTypeName.of(Class.class));
        Class[] a = new Class[] {String.class};



        Set<TypeElement> parents = new HashSet<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoIndex.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoIndex.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();
            parents.add(parent);
        }
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoField.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", LegoField.class.getSimpleName());
                return null;
            }
            VariableElement element = (VariableElement) annotatedElement;
            TypeElement parent = (TypeElement) element.getEnclosingElement();
            parents.add(parent);
        }

        builder.addCode("return new $T[] {", Class.class);
        for (TypeElement element: parents) {
            builder.addCode("$T.class,", element);
        }

        builder.addCode("};");
        return builder.build();
    }

    private MethodSpec getMethodModelEqualsByClass(RoundEnvironment roundEnvironment) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("isModelEquals")
                .addParameter(Object.class, "model0")
                .addParameter(Object.class, "model1")
                .addParameter(Class.class, "clazz")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(boolean.class);


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
                builder.beginControlFlow("if ($T.class.equals(clazz))", parent);
            } else {
                builder.nextControlFlow("else if ($T.class.equals(clazz))", parent);
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
}
