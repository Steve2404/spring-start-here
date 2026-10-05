package vorkurs04_annotations.drills.r04_invoke;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 4 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall04, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : 42",
            "D02 : wouf",
            "D03 : int Integer",
            "D04 : 6 IllegalArgumentException",
            "D05 : int Integer",
            "D06 : null",
            "D07 : 3 IllegalArgumentException",
            "D08 : cause IllegalStateException",
            "D09 : IllegalArgumentException NullPointerException",
            "D10 : NoSuchMethodException",
            "D11 : 1 Animal",
            "D12 : true true 1",
            "D13 : true s");
            // EXPECTED-END

    static final List<String> API = List.of(
            ".getMethod(", ".getDeclaredMethod(", ".getDeclaredMethods()", ".invoke(null", ".invoke(",
            "(Object) new String[]", ".getReturnType()", "IllegalArgumentException", "InvocationTargetException",
            ".getCause()", "NoSuchMethodException", "NullPointerException", ".getDeclaringClass()",
            "Modifier.isStatic(", ".isVarArgs()", ".getParameterCount()", ".canAccess(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall04", args, EXPECTED, API);
    }
}
