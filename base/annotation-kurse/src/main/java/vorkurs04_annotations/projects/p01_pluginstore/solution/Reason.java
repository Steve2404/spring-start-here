package vorkurs04_annotations.projects.p01_pluginstore.solution;

import java.util.Arrays;
import java.util.Optional;

/**
 * La traduction "code javac -> raison metier". Une enum avec un champ : chaque constante
 * connait son code ; la recherche se fait une fois, a un seul endroit.
 */
public enum Reason {
    FALSE_OVERRIDE("compiler.err.method.does.not.override.superclass", "faux @Override"),
    STATIC_OVERRIDE("compiler.err.static.methods.cannot.be.annotated.with.override", "@Override sur une methode static"),
    MISSING_ELEMENT("compiler.err.annotation.missing.default.value", "element obligatoire manquant"),
    NOT_CONSTANT("compiler.err.attribute.value.must.be.constant", "valeur non constante"),
    WRONG_VALUE_TYPE("compiler.err.prob.found.req", "mauvais type de valeur"),
    UNKNOWN_ELEMENT("compiler.err.cant.resolve.location.args", "element inexistant"),
    BAD_FUNCTIONAL("compiler.err.bad.functional.intf.anno.1", "@FunctionalInterface invalide"),
    BAD_SAFEVARARGS("compiler.err.varargs.invalid.trustme.anno", "@SafeVarargs mal place"),
    REPEATED("compiler.err.duplicate.annotation.missing.container", "annotation repetee"),
    BAD_MEMBER_TYPE("compiler.err.invalid.annotation.member.type", "type d'element interdit"),
    DEPRECATED("compiler.warn.has.been.deprecated", "API depreciee"),
    FOR_REMOVAL("compiler.warn.has.been.deprecated.for.removal", "API retiree bientot"),
    UNSAFE_VARARGS("compiler.warn.unchecked.varargs.non.reifiable.type", "varargs generique non sur"),
    GENERIC_ARRAY("compiler.warn.unchecked.generic.array.creation", "tableau generique cree a l'appel");

    private final String code;
    private final String label;

    Reason(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Optional<Reason> of(String code) {
        return Arrays.stream(values()).filter(r -> r.code.equals(code)).findFirst();
    }

    /** Le libelle d'un code, ou "code inconnu <code>" : un code imprevu ne doit jamais faire planter la boutique. */
    public static String labelOf(String code) {
        return of(code).map(Reason::label).orElse("code inconnu " + code);
    }
}
