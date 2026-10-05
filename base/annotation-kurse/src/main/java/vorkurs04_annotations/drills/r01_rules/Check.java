package vorkurs04_annotations.drills.r01_rules;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 1 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall01, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : compiler.err.annotation.missing.default.value",
            "D02 : compiler.err.invalid.annotation.member.type",
            "D03 : compiler.err.intf.annotation.members.cant.have.params",
            "D04 : compiler.err.attribute.value.must.be.constant",
            "D05 : compiler.err.cant.extend.intf.annotation",
            "D06 : compiler.err.cyclic.annotation.element",
            "D07 : compiler.err.annotation.missing.default.value OK",
            "D08 : OK compiler.err.attribute.value.must.be.constant",
            "D09 : compiler.err.method.does.not.override.superclass compiler.err.static.methods.cannot.be.annotated.with.override",
            "D10 : OK compiler.err.bad.functional.intf.anno.1",
            "D11 : compiler.err.varargs.invalid.trustme.anno OK OK",
            "D12 : compiler.warn.has.been.deprecated.for.removal | compiler.warn.has.been.deprecated.for.removal | OK",
            "D13 : compiler.err.duplicate.annotation.missing.container",
            "D14 : compiler.err.cant.resolve.location.args");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Javac.compile(", "re:(\\.code\\(\\)|::code\\b)##.code() ou Diag::code", "@interface", "default", "@Override", "@FunctionalInterface",
            "@SafeVarargs", "@Deprecated(", "forRemoval", "@SuppressWarnings(", "!.message()");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall01", args, EXPECTED, API);
    }
}
