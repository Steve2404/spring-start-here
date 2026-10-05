package vorkurs04_annotations.drills.r06_access;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 6 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall06, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : pin false IllegalAccessException",
            "D02 : true 42 true",
            "D03 : true ok",
            "D04 : tom IllegalAccessException",
            "D05 : 99 7",
            "D06 : IllegalAccessException true Safe true ouvert",
            "D07 : IllegalAccessException",
            "D08 : false InaccessibleObjectException",
            "D09 : java.base true false false",
            "D10 : private static final | true");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Javac.compile(", "Javac.load(", ".getDeclaredField(", ".getField(", ".canAccess(", ".trySetAccessible()",
            ".setAccessible(true)", ".get(", ".set(", ".getDeclaredConstructor(", ".getConstructor(", ".newInstance(",
            ".getDeclaredMethod(", ".invoke(", "record ", "InaccessibleObjectException", ".getModule()", ".isExported(",
            ".isOpen(", ".isNamed()", "Modifier.toString(", "Modifier.isPrivate(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall06", args, EXPECTED, API);
    }
}
