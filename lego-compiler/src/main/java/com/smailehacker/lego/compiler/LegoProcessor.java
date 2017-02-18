package com.smailehacker.lego.compiler;

import com.google.auto.service.AutoService;
import com.smilehacker.lego.annotation.Component;
import com.smilehacker.lego.annotation.LegoIndex;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class LegoProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        List<ComponentAnnotatedClass> componentAnnotatedClassList = new LinkedList<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(Component.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", Component.class.getSimpleName());
                return true;
            }
            TypeElement element = (TypeElement) annotatedElement;
            ComponentAnnotatedClass componentAnnotatedClass = new ComponentAnnotatedClass(element);
            componentAnnotatedClassList.add(componentAnnotatedClass);
        }

        List<VariableElement> indexList = new LinkedList<>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(LegoIndex.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "Only field can be annotated with @%s", Component.class.getSimpleName());
                return true;
            }
            VariableElement element = (VariableElement) annotatedElement;
            indexList.add(element);
        }


        FileMaker.makeComponentFactory(filer, componentAnnotatedClassList, indexList);
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Component.class.getCanonicalName());
        annotations.add(LegoIndex.class.getCanonicalName());
        return annotations;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void log(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args),
                e);
    }
}
