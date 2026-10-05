package vorkurs04_annotations.projects.p03_typeguard;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Les donnees du projet 3 (DONNEES, ne pas modifier). Les sources qui commencent par
 * "import {{PKG}}.*;" utilisent TES annotations : remplace {{PKG}} par ton paquet.
 */
public final class Data {

    /** Des couples annotation + conteneur proposes par d'autres equipes (id -> source autonome). */
    public static final Map<String, String> CONTAINERS = new LinkedHashMap<>();

    /** Des usages d'annotations (id -> source) : TES @Tag, @NonEmpty, @Column, @Trimmed. */
    public static final Map<String, String> PLACEMENTS = new LinkedHashMap<>();

    static {
        String head = "import java.lang.annotation.*;\n";
        CONTAINERS.put("C1", head + """
                @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { Label[] value(); }
                """);
        CONTAINERS.put("C2", head + """
                @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { Label[] items(); }
                """);
        CONTAINERS.put("C3", head + """
                @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { String[] value(); }
                """);
        CONTAINERS.put("C4", head + """
                @Retention(RetentionPolicy.RUNTIME) @Repeatable(Labels.class) @interface Label { String value(); }
                @Retention(RetentionPolicy.CLASS) @interface Labels { Label[] value(); }
                """);
        CONTAINERS.put("C5", head + """
                @Target(ElementType.TYPE) @Repeatable(Labels.class) @interface Label { String value(); }
                @Target({ElementType.TYPE, ElementType.METHOD}) @interface Labels { Label[] value(); }
                """);
        CONTAINERS.put("C6", head + """
                @Documented @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { Label[] value(); }
                """);
        CONTAINERS.put("C7", head + """
                @Inherited @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { Label[] value(); }
                """);
        CONTAINERS.put("C8", head + """
                @Repeatable(Labels.class) @interface Label { String value(); }
                @interface Labels { Label[] value(); int max(); }
                """);
        CONTAINERS.put("C9", head + """
                @Target({ElementType.TYPE, ElementType.METHOD}) @Repeatable(Labels.class) @interface Label { String value(); }
                @Target(ElementType.TYPE) @interface Labels { Label[] value(); int max() default 10; }
                """);

        String use = "import {{PKG}}.*;\nimport java.util.*;\n";
        PLACEMENTS.put("P01", use + "@Tag(\"core\") @Tag(\"fast\") class A {}");
        PLACEMENTS.put("P02", use + "@Tags({@Tag(\"core\"), @Tag(\"fast\")}) class A {}");
        PLACEMENTS.put("P03", use + "@Tags({@Tag(\"core\")}) @Tag(\"fast\") class A {}");
        PLACEMENTS.put("P04", use + "class A { @Tag(\"x\") int count; }");
        PLACEMENTS.put("P05", use + "class A { List<@NonEmpty String> names; }");
        PLACEMENTS.put("P06", use + "class A { @NonEmpty java.lang.String name; }");
        PLACEMENTS.put("P07", use + "class A { java.lang.@NonEmpty String name; }");
        PLACEMENTS.put("P08", use + "class A { @NonEmpty Map.Entry<String, String> entry; }");
        PLACEMENTS.put("P09", use + "class A { Map.@NonEmpty Entry<String, String> entry; }");
        PLACEMENTS.put("P10", use + "class A { void run() { @NonEmpty var text = \"x\"; } }");
        PLACEMENTS.put("P11", use + "class A { Object type = @NonEmpty String.class; }");
        PLACEMENTS.put("P12", use + "class A { void run() throws @NonEmpty Exception {} }");
        PLACEMENTS.put("P13", use + "class A { boolean test(Object o) { return o instanceof @NonEmpty String; } }");
        PLACEMENTS.put("P14", use + "class A { @Column List<String> rows; }");
        PLACEMENTS.put("P15", use + "class A { List<@Column String> rows; }");
        PLACEMENTS.put("P16", use + "class A { void run(@NonEmpty A this) {} }");
    }

    /** La classe dont tu analyses les annotations de TYPE dans le .class (classe "Model", paquet par defaut). */
    public static final String MODEL = """
            import {{PKG}}.*;
            import java.util.*;

            class Model {
                @NonEmpty String title;
                @NonEmpty String[] lines;
                String @NonEmpty [] pages;
                @NonEmpty int[][] grid;
                Map<@NonEmpty String, List<@NonEmpty Integer>> index;
                @Trimmed String code;
                @Trimmed String[] codes;
                @Column String plain;
            }
            """;

    /** La classe documentee par javadoc (classe publique "Report", paquet par defaut). */
    public static final String REPORT = """
            import {{PKG}}.*;

            /** Un rapport public. */
            @Public @Internal @Tag("finance") @Tag("monthly")
            public class Report {
            }
            """;

    private Data() {
    }
}
