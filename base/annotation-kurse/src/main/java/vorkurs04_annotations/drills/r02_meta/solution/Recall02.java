package vorkurs04_annotations.drills.r02_meta.solution;

import projectkit.Javac;

import java.util.Map;

/**
 * Corrige du drill 2 : cibles, retention, meta-annotations, annotations de type. Chaque source
 * commence par l'import de java.lang.annotation.* (I) ; javap et javadoc montrent ce qui reste.
 */
public class Recall02 {

    static final String I = "import java.lang.annotation.*;\n";

    static String verdict(String source) {
        return verdict(Map.of("D", I + source));
    }

    /** Plusieurs fichiers (cle = chemin sans .java) : utile pour package-info, qui doit avoir son propre fichier. */
    static String verdict(Map<String, String> sources) {
        return Javac.compile(sources).diags().stream().findFirst().map(Javac.Diag::code).orElse("OK");
    }

    public static void main(String[] args) {
        // D01 : sans @Target, toutes les DECLARATIONS... sauf les parametres de type (et les usages de type).
        System.out.println("D01 : " + verdict("@interface N {} @N class X { @N int f; void m(@N int p) { @N int l = 0; } }")
                + " " + verdict("@interface N {} class X<@N T> {}"));
        // D02 : une cible precise refuse les autres contextes.
        System.out.println("D02 : " + verdict("@Target(ElementType.FIELD) @interface F {} @F class X {}"));
        // D03 : une meme cible deux fois.
        System.out.println("D03 : " + verdict("@Target({ElementType.FIELD, ElementType.FIELD}) @interface F {}"));
        // D04 : TYPE_USE sur une methode void : il n'y a pas de type a annoter.
        System.out.println("D04 : " + verdict("@Target(ElementType.TYPE_USE) @interface U {} class X { @U void m() {} }")
                + " " + verdict("@Target(ElementType.TYPE_USE) @interface U {} @U class X { @U String m() { return \"\"; } }"));
        // D05 : sur un composant de record, une annotation METHOD est acceptee (elle va sur l'accesseur).
        System.out.println("D05 : " + verdict("@Target(ElementType.METHOD) @interface M {} record R(@M int a) {}"));
        // D06 : ANNOTATION_TYPE seulement sur des declarations d'annotation ; TYPE les couvre aussi.
        System.out.println("D06 : " + verdict("@Target(ElementType.ANNOTATION_TYPE) @interface Meta {} @Meta class X {}")
                + " " + verdict("@Target(ElementType.TYPE) @interface T {} @T @interface Y {}"));
        // D07 : ce que javap montre pour RUNTIME, CLASS (defaut) et SOURCE.
        Javac.Result r = Javac.compile(Map.of("X", I + "@Retention(RetentionPolicy.RUNTIME) @interface V {} @interface C {}"
                + " @Retention(RetentionPolicy.SOURCE) @interface S {} @V @C @S class X {}"));
        String javap = Javac.javap(r, "X");
        System.out.println("D07 : " + javap.contains("RuntimeVisibleAnnotations") + " " + javap.contains("RuntimeInvisibleAnnotations")
                + " " + javap.lines().anyMatch(line -> line.strip().equals("S")));
        // D08 : une annotation de variable locale n'est jamais ecrite, meme RUNTIME.
        Javac.Result local = Javac.compile(Map.of("Y", I + "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.LOCAL_VARIABLE)"
                + " @interface L {} class Y { void m() { @L int x = 1; } }"));
        System.out.println("D08 : " + Javac.javap(local, "Y").contains("RuntimeVisible"));
        // D09 -> D12 : les regles du conteneur @Repeatable.
        String tag = "@Repeatable(Tags.class) @interface Tag { String value(); }\n";
        System.out.println("D09 : " + verdict(tag + "@interface Tags { Tag[] items(); }"));
        System.out.println("D10 : " + verdict("@Retention(RetentionPolicy.RUNTIME) " + tag + "@Retention(RetentionPolicy.CLASS) @interface Tags { Tag[] value(); }"));
        System.out.println("D11 : " + verdict("@Target(ElementType.TYPE) " + tag + "@Target({ElementType.TYPE, ElementType.FIELD}) @interface Tags { Tag[] value(); }"));
        System.out.println("D12 : " + verdict("@Documented " + tag + "@interface Tags { Tag[] value(); }")
                + " " + verdict("@Inherited " + tag + "@interface Tags { Tag[] value(); }"));
        // D13 : une annotation de type se place devant le NOM SIMPLE.
        String nn = "@Target(ElementType.TYPE_USE) @interface NN {}\n";
        System.out.println("D13 : " + verdict(nn + "class X { @NN java.lang.String s; }") + " "
                + verdict(nn + "class X { java.lang.@NN String s; }"));
        // D14 : ni sur var, ni sur .class.
        System.out.println("D14 : " + verdict(nn + "class X { void m() { @NN var v = 1; } }") + " "
                + verdict(nn + "class X { Object c = @NN String.class; }"));
        // D15 : javadoc ne publie que les annotations @Documented.
        String page = Javac.javadocPage(Map.of("Doc", I + "@Documented @interface Pub {} @interface Priv {}\n/** Doc. */ @Pub @Priv public class Doc {}"), "Doc");
        System.out.println("D15 : " + page.contains("@Pub") + " " + page.contains("@Priv"));
        // D16 : PACKAGE = la declaration de paquet, qui n'existe qu'une fois : dans package-info.java.
        String pkg = "package p; " + I + "@Target(ElementType.PACKAGE) public @interface P {}";
        System.out.println("D16 : " + verdict(Map.of("p/P", pkg, "p/package-info", "@P package p;")) + " "
                + verdict(Map.of("p/P", pkg, "p/X", "package p; @P class X {}")) + " "
                + verdict(Map.of("p/T", "package p; " + I + "@Target(ElementType.TYPE) public @interface T {}",
                "p/package-info", "@T package p;")));
        // D17 : RECORD_COMPONENT ne vise que les composants ; MODULE que module-info.java.
        String rc = "@Target(ElementType.RECORD_COMPONENT) @interface C {}\n";
        System.out.println("D17 : " + verdict(rc + "record R(@C int a) {}") + " " + verdict(rc + "class X { @C int f; }") + " "
                + verdict("@Target(ElementType.MODULE) @interface Mo {} @Mo class X {}"));
    }
}
