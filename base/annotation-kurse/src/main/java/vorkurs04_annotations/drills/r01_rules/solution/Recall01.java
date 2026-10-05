package vorkurs04_annotations.drills.r01_rules.solution;

import projectkit.Javac;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Corrige du drill 1 : chaque defi est une petite source que javac juge. On affiche le CODE du
 * premier diagnostic (ou OK), jamais le message.
 */
public class Recall01 {

    /** Le premier code (erreur ou avertissement) de javac pour cette source, ou OK. */
    static String verdict(String source) {
        return Javac.compile(Map.of("D", source)).diags().stream().findFirst().map(Javac.Diag::code).orElse("OK");
    }

    /** Tous les codes, dans l'ordre, separes par un espace (ou OK). */
    static String all(String source) {
        String codes = Javac.compile(Map.of("D", source)).diags().stream().map(Javac.Diag::code).collect(Collectors.joining(" "));
        return codes.isEmpty() ? "OK" : codes;
    }

    public static void main(String[] args) {
        // D01 : un element sans default est OBLIGATOIRE a l'usage.
        System.out.println("D01 : " + verdict("@interface A { String id(); } @A class X {}"));
        // D02 : types permis = primitifs, String, Class, enum, annotation, et tableaux de ceux-ci. Pas Object.
        System.out.println("D02 : " + verdict("@interface A { Object data(); }"));
        // D03 : un element ressemble a une methode, mais sans parametre.
        System.out.println("D03 : " + verdict("@interface A { String id(int n); }"));
        // D04 : null n'est jamais une valeur d'annotation, ni par defaut ni a l'usage.
        System.out.println("D04 : " + verdict("@interface A { String id() default null; }"));
        // D05 : une annotation n'herite de rien (pas de extends).
        System.out.println("D05 : " + verdict("@interface A extends java.lang.annotation.Annotation {}"));
        // D06 : un element ne peut pas etre du type de sa propre annotation.
        System.out.println("D06 : " + verdict("@interface A { A inner(); }"));
        // D07 : le raccourci @A("x") marche seulement si TOUS les autres elements ont un default.
        System.out.println("D07 : " + verdict("@interface A { String value(); int n(); } @A(\"x\") class X {}")
                + " " + verdict("@interface A { String value(); int n() default 0; } @A(\"x\") class X {}"));
        // D08 : un element tableau accepte une valeur seule ; une valeur doit etre une constante.
        System.out.println("D08 : " + verdict("@interface A { String[] tags(); } @A(tags = \"solo\") class X {}")
                + " " + verdict("@interface A { String id(); } class X { static String s = \"v\"; @A(id = s) void m() {} }"));
        // D09 : @Override verifie une redefinition ; une methode static ne se redefinit pas.
        System.out.println("D09 : " + verdict("class X { @Override public String tostring() { return \"\"; } }")
                + " " + verdict("class B { static void m() {} } class X extends B { @Override static void m() {} }"));
        // D10 : default et methodes d'Object ne comptent pas comme abstraites.
        System.out.println("D10 : " + verdict("@FunctionalInterface interface F { void a(); default void b() {} boolean equals(Object o); }")
                + " " + verdict("@FunctionalInterface interface F { void a(); void b(); }"));
        // D11 : @SafeVarargs seulement si la methode ne peut pas etre redefinie (static, final, private).
        System.out.println("D11 : " + verdict("class X { @SafeVarargs <T> void m(T... t) {} }")
                + " " + verdict("class X { @SafeVarargs final <T> void m(T... t) {} }")
                + " " + verdict("class X { @SafeVarargs private <T> void m(T... t) {} }"));
        // D12 : "deprecation" ne fait PAS taire un forRemoval : il faut "removal".
        String deprecated = "class B { @Deprecated(since = \"2\", forRemoval = true) static void old() {} }\n";
        System.out.println("D12 : " + all(deprecated + "class X { void u() { B.old(); } }")
                + " | " + all(deprecated + "class X { @SuppressWarnings(\"deprecation\") void u() { B.old(); } }")
                + " | " + all(deprecated + "class X { @SuppressWarnings(\"removal\") void u() { B.old(); } }"));
        // D13 : une annotation non repetable ecrite deux fois.
        System.out.println("D13 : " + verdict("@interface A {} @A @A class X {}"));
        // D14 : une annotation marqueur ne recoit aucune valeur.
        System.out.println("D14 : " + verdict("@interface M {} @M(\"x\") class X {}"));
    }
}
