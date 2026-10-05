package vorkurs04_annotations.projects.p02_compliance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 2 (DONNEES, ne pas modifier).
 *
 * Toutes les sources commencent par "import {{PKG}}.*;" : remplace {{PKG}} par le paquet de TES
 * annotations (ComplianceDesk.class.getPackageName()).
 */
public final class Data {

    /** Un contexte ou l'on peut tenter de poser une annotation ; {A} sera remplace par "@NomDeLAnnotation". */
    public record Context(String name, String line) {
    }

    /**
     * Les 10 contextes, dans l'ordre. Chaque ligne est une declaration COMPLETE (elle tient seule sur sa ligne).
     * Ta sonde = la ligne d'import, puis ces 10 lignes : le contexte n de la liste (0, 1, ...) est donc a la ligne n + 2.
     */
    public static final List<Context> CONTEXTS = List.of(
            new Context("type", "{A} class CtxType {}"),
            new Context("annotation", "{A} @interface CtxAnnotation {}"),
            new Context("record", "record CtxRecord({A} int value) {}"),
            new Context("typeparam", "class CtxTypeParam<{A} T> {}"),
            new Context("field", "class CtxField { {A} int value; }"),
            new Context("constructor", "class CtxConstructor { {A} CtxConstructor() {} }"),
            new Context("method", "class CtxMethod { {A} void run() {} }"),
            new Context("parameter", "class CtxParameter { void run({A} int p) {} }"),
            new Context("local", "class CtxLocal { void run() { {A} int x = 0; } }"),
            new Context("typeuse", "class CtxTypeUse { java.util.List<{A} String> values; }"));

    /** Les annotations declarees par d'autres equipes (id -> source). Leur nom est celui de la derniere ligne. */
    public static final Map<String, String> DECLARATIONS = new LinkedHashMap<>();

    static {
        DECLARATIONS.put("D1", """
                import java.lang.annotation.*;
                @Target({ElementType.FIELD, ElementType.FIELD})
                @interface Twice {}
                """);
        DECLARATIONS.put("D2", """
                import java.lang.annotation.*;
                @Target({})
                @interface Nowhere {}
                """);
        DECLARATIONS.put("D3", """
                import java.lang.annotation.*;
                @Retention(RetentionPolicy.SOURCE)
                @Target(ElementType.TYPE_USE)
                @interface Checked {}
                """);
        DECLARATIONS.put("D4", """
                import java.lang.annotation.*;
                @Retention(RetentionPolicy.RUNTIME)
                @interface Plain {}
                """);
    }

    /** La classe d'exemple dont tu analyses le .class avec javap (classe "Sample", paquet par defaut). */
    public static final String SAMPLE = """
            import {{PKG}}.*;

            @Audited @Generated
            class Sample {
                @Sensitive String secret;
                @Factory Sample() {}
                @Audited @Trace void run(@Sensitive String key) { @Local int x = 1; }
                @Factory static Sample create() { return new Sample(); }
                @NotNull String name() { return "sample"; }
            }
            """;

    private Data() {
    }
}
