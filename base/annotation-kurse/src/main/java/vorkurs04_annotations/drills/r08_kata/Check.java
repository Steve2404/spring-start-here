package vorkurs04_annotations.drills.r08_kata;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du kata 8 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall08, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : compiler.err.invalid.annotation.member.type | compiler.err.annotation.type.not.applicable",
            "D02 : 2 true admin,ops",
            "D03 : true 0 false",
            "D04 : retries<=3x timeout<=100ms",
            "D05 : false x",
            "D06 : restart restart:panne",
            "D07 : retries 5 trop | timeout 50 ok",
            "D08 : java.lang.String java.util.List<java.lang.Integer> | left:A right:B",
            "D09 : true Role",
            "D10 : child:x false");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Repeatable(", "@Inherited", "RetentionPolicy.RUNTIME", "default \"x\"", "record ", "Javac.compile(",
            ".getAnnotationsByType(", ".getAnnotation(", ".isAnnotationPresent(", ".getDeclaredAnnotations()",
            ".getDeclaredFields()", ".getDefaultValue()", ".getDeclaredMethods()", ".invoke(", ".set(",
            "ParameterizedType", ".getActualTypeArguments()", ".getRecordComponents()", ".annotationType()",
            ".equals(", "!.setAccessible(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall08", args, EXPECTED, API);
    }
}
