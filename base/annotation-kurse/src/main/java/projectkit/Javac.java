package projectkit;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Un compilateur javac EN MEMOIRE, fourni (ne pas modifier, ne compte pas dans
 * l'analyse de ton code). C'est l'outil des projets "a la compilation" du
 * Vorkurs 0.4 : beaucoup de regles des annotations (@Override, @Target,
 * elements obligatoires, @Repeatable...) sont verifiees par javac, pas a
 * l'execution. Ton programme lui donne des sources (du texte), il te rend
 * ce que javac a dit.
 *
 * Les diagnostics sont rendus par leur CODE (ex.
 * "compiler.err.method.does.not.override.superclass"), stable et independant
 * de la langue de la machine, jamais par leur message traduit.
 */
public final class Javac {

    private Javac() {
    }

    /**
     * Un diagnostic de javac.
     *
     * @param kind "ERROR", "WARNING" ou "NOTE" (les avertissements obligatoires comptent comme WARNING)
     * @param file le nom de la source, tel que tu l'as donne (ex. "S01")
     * @param line la ligne dans CETTE source (1, 2, ...)
     * @param code le code javac (ex. "compiler.warn.has.been.deprecated")
     * @param message le texte, en anglais ; pour un message de Messager, c'est TON texte
     */
    public record Diag(String kind, String file, long line, String code, String message) {
    }

    /**
     * @param success   true si javac n'a signale aucune erreur
     * @param diags     les diagnostics, tries par source puis par ligne
     * @param classes   le dossier des .class produits (pour javap)
     * @param generated les sources GENEREES par un processor : nom du fichier -> contenu
     */
    public record Result(boolean success, List<Diag> diags, Path classes, Map<String, String> generated) {
    }

    /** Compile les sources (nom -> code) avec -Xlint:all ; ton propre code (target/classes) est sur le classpath. */
    public static Result compile(Map<String, String> sources) {
        return compile(sources, List.of(), null);
    }

    /**
     * Comme compile(sources), avec des options javac en plus (ex. "-Agreeting=Salut") et un
     * annotation processor (ou null). Sans processor, le traitement des annotations est coupe (-proc:none).
     */
    public static Result compile(Map<String, String> sources, List<String> extraOptions, Processor processor) {
        try {
            Path classes = Files.createTempDirectory("javac-classes");
            Path generatedDir = Files.createTempDirectory("javac-generated");
            List<JavaFileObject> files = new ArrayList<>();
            sources.forEach((name, code) -> files.add(new Source(name, code)));
            List<String> options = new ArrayList<>(List.of("-Xlint:all", "-encoding", "UTF-8",
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", classes.toString(), "-s", generatedDir.toString()));
            if (processor == null) {
                options.add("-proc:none");
            }
            options.addAll(extraOptions);
            JavaCompiler javac = javax.tools.ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
            JavaCompiler.CompilationTask task = javac.getTask(new StringWriter(), null, collector, options, null, files);
            if (processor != null) {
                task.setProcessors(List.of(processor));
            }
            boolean ok = task.call();
            List<Diag> diags = new ArrayList<>();
            for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
                diags.add(new Diag(kindOf(d.getKind()), fileOf(d.getSource()), Math.max(d.getLineNumber(), 0),
                        d.getCode(), d.getMessage(Locale.ENGLISH)));
            }
            diags.sort(Comparator.comparing(Diag::file).thenComparingLong(Diag::line));
            return new Result(ok, List.copyOf(diags), classes, readTree(generatedDir));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Charge une classe compilee par compile(...) pour l'inspecter avec la reflection (projets 4 et suivants).
     * Le chargeur parent est celui de ton projet : les annotations que TU as ecrites gardent leur identite,
     * donc getAnnotation(TonAnnotation.class) fonctionne sur la classe chargee.
     *
     * @param binaryName nom binaire : "Account", ou "Outer$Inner" pour une classe imbriquee
     */
    public static Class<?> load(Result result, String binaryName) {
        if (!result.success()) {
            throw new IllegalStateException("compilation echouee : " + result.diags());
        }
        try {
            ClassLoader loader = LOADERS.computeIfAbsent(result.classes(), dir -> {
                try {
                    return new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()}, Javac.class.getClassLoader());
                } catch (java.net.MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            });
            return Class.forName(binaryName, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("classe introuvable dans le resultat : " + binaryName, e);
        }
    }

    // Un seul chargeur par compilation : deux load() du meme resultat rendent les MEMES objets Class.
    private static final Map<Path, ClassLoader> LOADERS = new java.util.HashMap<>();

    /** La sortie de "javap -v -p" pour une classe compilee (nom binaire, ex. "demo.Order" ou "Order"). */
    public static String javap(Result result, String className) {
        Path classFile = result.classes().resolve(className.replace('.', '/') + ".class");
        StringWriter out = new StringWriter();
        java.util.spi.ToolProvider.findFirst("javap").orElseThrow()
                .run(new PrintWriter(out), new PrintWriter(out), "-v", "-p", classFile.toString());
        return out.toString();
    }

    /**
     * Lance javadoc sur les sources et rend la page HTML de la classe demandee (nom simple,
     * classe publique du paquet par defaut, ex. "Report").
     */
    public static String javadocPage(Map<String, String> sources, String className) {
        try {
            Path src = Files.createTempDirectory("javadoc-src");
            Path out = Files.createTempDirectory("javadoc-out");
            List<String> args = new ArrayList<>(List.of("-quiet", "-Xdoclint:none", "-encoding", "UTF-8",
                    "-classpath", System.getProperty("java.class.path"), "-d", out.toString(), "-package"));
            for (Map.Entry<String, String> e : sources.entrySet()) {
                Path file = src.resolve(e.getKey() + ".java");
                Files.writeString(file, e.getValue());
                args.add(file.toString());
            }
            StringWriter log = new StringWriter();
            java.util.spi.ToolProvider.findFirst("javadoc").orElseThrow()
                    .run(new PrintWriter(log), new PrintWriter(log), args.toArray(String[]::new));
            Path page = out.resolve(className + ".html");
            return Files.exists(page) ? Files.readString(page) : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String kindOf(Diagnostic.Kind kind) {
        return switch (kind) {
            case ERROR -> "ERROR";
            case WARNING, MANDATORY_WARNING -> "WARNING";
            default -> "NOTE";
        };
    }

    private static String fileOf(JavaFileObject source) {
        if (source == null) {
            return "";
        }
        String path = source.toUri().getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.endsWith(".java") ? name.substring(0, name.length() - 5) : name;
    }

    private static Map<String, String> readTree(Path dir) throws IOException {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.filter(Files::isRegularFile).toList()) {
                files.put(dir.relativize(p).toString().replace('\\', '/'), Files.readString(p));
            }
        }
        return files;
    }

    private static final class Source extends SimpleJavaFileObject {
        private final String code;

        Source(String name, String code) {
            super(URI.create("string:///" + name + ".java"), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
