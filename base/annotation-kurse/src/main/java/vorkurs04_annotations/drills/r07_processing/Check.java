package vorkurs04_annotations.drills.r07_processing;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 7 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall07, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : true | round 1 racines 1 marques 1 ; fin round 2",
            "D02 : true | round 1 racines 1 marques 1 ; round 2 racines 1 marques 0 ; fin round 3",
            "D03 : true | round 1 racines 1 marques 3 ; CLASS A ; FIELD n ; METHOD m ; fin round 2",
            "D04 : true | round 1 racines 1 marques 1 ; n int INT ; names java.util.List<java.lang.String> DECLARED ; fin round 2",
            "D05 : true | round 1 racines 1 marques 1 ; CONSTRUCTOR FIELD FIELD METHOD ; fin round 2",
            "D06 : false | round 1 racines 1 marques 1 ; fin round 2 | compiler.err.proc.messager",
            "D07 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.messager",
            "D08 : true | round 1 racines 1 marques 1 ; FilerException ; round 2 racines 1 marques 0 ; fin round 3 | compiler.warn.proc.type.recreate",
            "D09 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.unmatched.processor.options | option salut",
            "D10 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.processor.incompatible.source.version",
            "D11 : true | jamais appele",
            "D12 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.annotations.without.processors");
            // EXPECTED-END

    static final List<String> API = List.of(
            "extends AbstractProcessor", "getSupportedAnnotationTypes", "getSupportedSourceVersion",
            "SourceVersion.latestSupported()", "SourceVersion.RELEASE_8", "void init(", ".getOptions()",
            "boolean process(", ".processingOver()", ".getElementsAnnotatedWith(", ".getRootElements()",
            ".getKind()", ".getSimpleName()", ".getEnclosedElements()", ".asType()", "VariableElement",
            ".getMessager()", "Diagnostic.Kind.ERROR", "Diagnostic.Kind.WARNING", ".getFiler()",
            ".createSourceFile(", "FilerException", "RetentionPolicy.SOURCE", "Javac.compile(", "-Agreeting=");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall07", args, EXPECTED, API);
    }
}
