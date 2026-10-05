package vorkurs04_annotations.projects.p01_pluginstore;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Les donnees du projet 1 (DONNEES, ne pas modifier) : les soumissions recues par la boutique.
 * Ce sont des SOURCES Java (du texte), pas du code de ce projet : ta boutique les compile avec
 * projectkit.Javac.
 *
 * Chaque soumission commence par "import {{PKG}}.*;" : submissions(paquet) remplace {{PKG}} par
 * le paquet ou TU as ecrit les annotations et BasePlugin (ton main passe son propre paquet, ainsi
 * la meme donnee marche pour toi et pour la solution).
 */
public final class Data {

    private static final Map<String, String> RAW = new LinkedHashMap<>();

    static {
        RAW.put("S01", """
                import {{PKG}}.*;

                @Plugin(id = "hello", tags = {"demo", "texte"})
                @Since("1.0")
                class HelloPlugin extends BasePlugin implements Action {
                    @Override public String name() { return "Hello"; }
                    @Override public String run(String input) { return "Hello " + input; }
                }
                """);
        RAW.put("S02", """
                import {{PKG}}.*;

                @Plugin(id = "typo")
                class TypoPlugin extends BasePlugin {
                    @Override public String name() { return "Typo"; }
                    @Override public String tostring() { return "typo"; }
                }
                """);
        RAW.put("S03", """
                import {{PKG}}.*;

                @Plugin(version = 2)
                class NoIdPlugin extends BasePlugin {
                    @Override public String name() { return "NoId"; }
                }
                """);
        RAW.put("S04", """
                import {{PKG}}.*;

                @Plugin(id = NewIdPlugin.ID)
                class NewIdPlugin extends BasePlugin {
                    static final String ID = new String("new-id");
                    @Override public String name() { return "NewId"; }
                }
                """);
        RAW.put("S05", """
                import {{PKG}}.*;

                @Plugin(id = ConstPlugin.ID, version = ConstPlugin.MAJOR * 10, category = Category.GAME)
                class ConstPlugin extends BasePlugin {
                    static final String ID = "const-" + "id";
                    static final int MAJOR = 3;
                    @Override public String name() { return "Const"; }
                }
                """);
        RAW.put("S06", """
                import {{PKG}}.*;

                @FunctionalInterface
                interface Twice {
                    String first(String s);
                    String second(String s);
                }

                @Plugin(id = "twice")
                class TwicePlugin extends BasePlugin {
                    @Override public String name() { return "Twice"; }
                }
                """);
        RAW.put("S07", """
                import {{PKG}}.*;

                @Plugin(id = "old")
                class OldPlugin extends BasePlugin {
                    @Override public String name() { return "Old"; }
                    void boot() {
                        init();
                        legacyInit();
                    }
                }
                """);
        RAW.put("S08", """
                import {{PKG}}.*;

                @Plugin(id = "quiet")
                class QuietPlugin extends BasePlugin {
                    @Override public String name() { return "Quiet"; }
                    @SuppressWarnings({"deprecation", "removal"})
                    void boot() {
                        init();
                        legacyInit();
                    }
                }
                """);
        RAW.put("S09", """
                import {{PKG}}.*;
                import java.util.List;

                @Plugin(id = "bag")
                class BagPlugin extends BasePlugin {
                    @Override public String name() { return "Bag"; }
                    @SafeVarargs
                    public <T> List<T> bag(T... items) { return List.of(items); }
                }
                """);
        RAW.put("S10", """
                import {{PKG}}.*;

                @Plugin(id = "twice-since")
                @Since("1.0")
                @Since("2.0")
                class TwiceSincePlugin extends BasePlugin {
                    @Override public String name() { return "TwiceSince"; }
                }
                """);
        RAW.put("S11", """
                import {{PKG}}.*;

                @Plugin(id = "beta")
                @Experimental("beta")
                class BetaPlugin extends BasePlugin {
                    @Override public String name() { return "Beta"; }
                }
                """);
        RAW.put("S12", """
                import {{PKG}}.*;

                @Plugin(id = "count", version = "2")
                class CountPlugin extends BasePlugin {
                    @Override public String name() { return "Count"; }
                }
                """);
        RAW.put("S13", """
                import {{PKG}}.*;
                import java.util.List;

                @Plugin(id = "solo", tags = "solo")
                @Requires(String.class)
                @Experimental
                class SoloPlugin extends BasePlugin {
                    @Override public String name() { return "Solo"; }
                    List<String> names() { return BasePlugin.listOf("a", "b"); }
                }
                """);
        RAW.put("S14", """
                import {{PKG}}.*;
                import java.util.List;

                @Plugin(id = "pack")
                class PackPlugin extends BasePlugin {
                    @Override public String name() { return "Pack"; }
                    static <T> List<T> pack(T... items) { return List.of(items); }
                    List<List<String>> both() { return pack(List.of("a"), List.of("b")); }
                }
                """);
        RAW.put("S15", """
                import {{PKG}}.*;

                @Plugin(id = "static")
                class StaticPlugin extends BasePlugin {
                    @Override public String name() { return "Static"; }
                    @Override public static String version() { return "1"; }
                }
                """);
        RAW.put("S16", """
                import {{PKG}}.*;

                @interface Meta {
                    Object data();
                }

                @Plugin(id = "meta")
                class MetaPlugin extends BasePlugin {
                    @Override public String name() { return "Meta"; }
                }
                """);
        RAW.put("S17", """
                import {{PKG}}.*;

                @interface Note {
                    String text() default null;
                }

                @Plugin(id = "note")
                class NotePlugin extends BasePlugin {
                    @Override public String name() { return "Note"; }
                }
                """);
        RAW.put("S18", """
                import {{PKG}}.*;

                @Plugin(id = "full", version = 4, tags = {}, category = Category.THEME)
                @Since(value = "3.1")
                @Requires({String.class, Integer.class})
                class FullPlugin extends BasePlugin implements Action {
                    @Override public String name() { return "Full"; }
                    @Override public String run(String input) { return input.strip(); }
                }
                """);
    }

    /** Les soumissions, dans l'ordre d'arrivee : id -> source, avec {{PKG}} remplace par packageName. */
    public static Map<String, String> submissions(String packageName) {
        Map<String, String> result = new LinkedHashMap<>();
        RAW.forEach((id, source) -> result.put(id, source.replace("{{PKG}}", packageName)));
        return result;
    }

    private Data() {
    }
}
