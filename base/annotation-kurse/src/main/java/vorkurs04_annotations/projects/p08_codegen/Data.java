package vorkurs04_annotations.projects.p08_codegen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 8 (DONNEES, ne pas modifier) : trois LOTS de sources a compiler avec TON
 * processor. Chaque lot est compile en une fois. Remplace {{PKG}} par le paquet de TON @Builder.
 */
public final class Data {

    /** Un lot : son nom, les options javac a passer, et ses sources (nom -> code). */
    public record Batch(String name, List<String> options, Map<String, String> sources) {
    }

    public static final List<Batch> BATCHES;

    static {
        Map<String, String> ok = new LinkedHashMap<>();
        ok.put("Person", """
                import {{PKG}}.Builder;
                import java.util.List;

                @Builder
                public record Person(String name, int age, List<String> tags) {}
                """);
        ok.put("Point", """
                import {{PKG}}.Builder;

                @Builder
                public record Point(int x, int y) {}
                """);
        ok.put("Empty", """
                import {{PKG}}.Builder;

                @Builder
                public record Empty() {}
                """);
        ok.put("Plain", "public record Plain(int value) {}\n");
        ok.put("UsePerson", """
                public class UsePerson {
                    public static Person demo() {
                        return new PersonBuilder().name("Ines").age(41).tags(java.util.List.of("ops")).build();
                    }
                }
                """);

        Map<String, String> bad = new LinkedHashMap<>();
        bad.put("Shape", """
                import {{PKG}}.Builder;

                @Builder
                public abstract class Shape {}
                """);
        bad.put("Task", """
                import {{PKG}}.Builder;

                @Builder
                public record Task(String title, boolean build) {}
                """);

        Map<String, String> maker = new LinkedHashMap<>();
        maker.put("Point", ok.get("Point"));

        BATCHES = List.of(
                new Batch("ok", List.of(), ok),
                new Batch("bad", List.of(), bad),
                new Batch("maker", List.of("-Abuilder.suffix=Maker"), maker));
    }

    private Data() {
    }
}
