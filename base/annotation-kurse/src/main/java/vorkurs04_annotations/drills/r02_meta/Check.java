package vorkurs04_annotations.drills.r02_meta;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 2 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall02, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : OK compiler.err.annotation.type.not.applicable.to.type",
            "D02 : compiler.err.annotation.type.not.applicable",
            "D03 : compiler.err.repeated.annotation.target",
            "D04 : compiler.err.annotation.type.not.applicable OK",
            "D05 : OK",
            "D06 : compiler.err.annotation.type.not.applicable OK",
            "D07 : true true false",
            "D08 : false",
            "D09 : compiler.err.invalid.repeatable.annotation.no.value",
            "D10 : compiler.err.invalid.repeatable.annotation.retention",
            "D11 : compiler.err.invalid.repeatable.annotation.incompatible.target",
            "D12 : compiler.err.invalid.repeatable.annotation.not.documented compiler.err.invalid.repeatable.annotation.not.inherited",
            "D13 : compiler.err.cant.type.annotate.scoping.1 OK",
            "D14 : compiler.err.annotation.type.not.applicable compiler.err.no.annotations.on.dot.class",
            "D15 : true false",
            "D16 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable",
            "D17 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Javac.compile(", "Javac.javap(", "Javac.javadocPage(", "@Target(", "@Retention(",
            "ElementType.TYPE_USE", "ElementType.ANNOTATION_TYPE", "ElementType.LOCAL_VARIABLE", "ElementType.PACKAGE", "ElementType.MODULE",
            "ElementType.RECORD_COMPONENT", "package-info", "RetentionPolicy.SOURCE",
            "RetentionPolicy.CLASS", "@Repeatable(", "@Documented", "@Inherited", "!.message()");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall02", args, EXPECTED, API);
    }
}
