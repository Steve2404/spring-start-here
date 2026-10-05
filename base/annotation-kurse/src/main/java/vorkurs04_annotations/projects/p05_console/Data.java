package vorkurs04_annotations.projects.p05_console;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 5 (DONNEES, ne pas modifier). Les classes de commandes sont des SOURCES
 * (une classe publique par source) : compile-les ENSEMBLE avec Javac.compile, puis charge chaque
 * classe avec Javac.load, dans l'ordre de HANDLERS. Remplace {{PKG}} par le paquet de TON @Command.
 */
public final class Data {

    /** nom de la classe -> source ; l'ordre est l'ordre d'enregistrement. */
    public static final Map<String, String> HANDLERS = new LinkedHashMap<>();

    static {
        HANDLERS.put("FileCommands", """
                import {{PKG}}.*;
                import java.util.ArrayList;
                import java.util.List;

                public class FileCommands {
                    private final List<String> files = new ArrayList<>();
                    public static FileCommands create() { return new FileCommands(); }
                    @Command(name = "touch", description = "cree un fichier") public void touch(String name) { files.add(name); }
                    @Command(name = "ls", description = "liste les fichiers", priority = 5) public String ls() { return String.join(",", files); }
                    @Command(name = "count") public int count() { return files.size(); }
                    @Command(name = "rm", description = "supprime un fichier", adminOnly = true) public boolean rm(String name) { return files.remove(name); }
                }
                """);
        HANDLERS.put("MathCommands", """
                import {{PKG}}.*;

                public class MathCommands {
                    public static MathCommands create() { return new MathCommands(); }
                    @Command(name = "add") public static long add(long a, long b) { return a + b; }
                    @Command(name = "mul", priority = 5) public static long mul(long a, long b) { return a * b; }
                    @Command(name = "sum", description = "additionne tout") public static int sum(int... values) {
                        int total = 0;
                        for (int v : values) { total += v; }
                        return total;
                    }
                    @Command(name = "div") public static int div(int a, int b) { return a / b; }
                    @Command(name = "pct") public static double pct(double part, double total) { return 100 * part / total; }
                    @Command(name = "secret", adminOnly = true) private static String secret() { return "42"; }
                    @Command(name = "ping") public static boolean ping(boolean loud) { return !loud; }
                }
                """);
        HANDLERS.put("BrokenCommands", """
                import {{PKG}}.*;

                public class BrokenCommands {
                    public static BrokenCommands create() { return new BrokenCommands(); }
                    @Command(name = "add") public static int plus(int a, int b) { return a + b; }
                    @Command(name = " ") public void blank() {}
                    @Command(name = "dump") public void dump(Object any) {}
                    public void notACommand() {}
                }
                """);
    }

    /** Le script : "utilisateur commande arguments...", separes par des espaces. */
    public static final List<String> SCRIPT = List.of(
            "guest touch a.txt",
            "guest touch b.txt",
            "guest ls",
            "guest count",
            "guest rm a.txt",
            "admin rm a.txt",
            "admin rm a.txt",
            "guest ls",
            "guest mul 6 7",
            "guest add 2 40",
            "guest sum 1 2 3 4",
            "guest sum",
            "guest div 7 2",
            "guest div 7 0",
            "guest pct 1 8",
            "guest ping true",
            "guest mul 6 x",
            "guest touch",
            "guest dump x",
            "guest secret",
            "admin secret",
            "guest help");

    private Data() {
    }
}
