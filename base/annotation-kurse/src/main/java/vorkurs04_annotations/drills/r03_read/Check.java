package vorkurs04_annotations.drills.r03_read;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 3 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall03, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : true true null",
            "D02 : false",
            "D03 : id true",
            "D04 : Col true",
            "D05 : null 2 2",
            "D06 : solo null",
            "D07 : 1 0 true",
            "D08 : ctor",
            "D09 : false 1",
            "D10 : 0 true",
            "D11 : 1 1",
            "D12 : true false",
            "D13 : 1 0",
            "D14 : true true false");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Inherited", "@Repeatable(", ".isAnnotationPresent(", ".getDeclaredAnnotation(", ".getAnnotation(",
            ".annotationType()", "Proxy.isProxyClass(", ".getAnnotationsByType(", ".getParameterAnnotations()",
            ".getParameters()", ".getDeclaredConstructor(", ".getAnnotations()", ".getDeclaredAnnotations()",
            "AnnotatedParameterizedType", ".getAnnotatedActualTypeArguments()", "AnnotatedArrayType",
            ".getAnnotatedGenericComponentType()", ".getRecordComponents()", ".equals(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall03", args, EXPECTED, API);
    }
}
