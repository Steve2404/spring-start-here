package vorkurs04_annotations.projects.p02_compliance.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p02_compliance.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Le guichet de conformite : il ne LIT pas @Target (ce serait de la reflection, projet 4) ; il le
 * MESURE en demandant a javac, contexte par contexte, ou chaque annotation est acceptee. Puis il
 * regarde le .class avec javap pour voir ce que @Retention a vraiment garde.
 */
public class ComplianceDesk {

    static final List<String> OURS = List.of("Audited", "Sensitive", "Factory", "Trace", "Generated",
            "Local", "Meta", "TypeTag", "NotNull", "Anywhere");

    /** Une sonde : l'import puis une ligne par contexte ; le contexte i est a la ligne i + 2. */
    static String probe(String pkg, String annotation) {
        StringBuilder src = new StringBuilder("import " + pkg + ".*;\n");
        for (Data.Context c : Data.CONTEXTS) {
            src.append(c.line().replace("{A}", "@" + annotation)).append('\n');
        }
        return src.toString();
    }

    /** Les contextes acceptes = tous, moins ceux dont la LIGNE porte une erreur de la sonde. */
    static List<String> acceptedContexts(Map<String, String> sources, String probeName) {
        Set<Long> refusedLines = Javac.compile(sources).diags().stream()
                .filter(d -> d.kind().equals("ERROR") && d.file().equals(probeName))
                .map(Javac.Diag::line)
                .collect(Collectors.toSet());
        List<String> accepted = new ArrayList<>();
        for (int i = 0; i < Data.CONTEXTS.size(); i++) {
            if (!refusedLines.contains((long) i + 2)) {
                accepted.add(Data.CONTEXTS.get(i).name());
            }
        }
        return accepted;
    }

    static String matrixLine(String annotation, List<String> accepted) {
        return "MATRICE " + annotation + " : " + (accepted.isEmpty() ? "aucun contexte" : String.join(" ", accepted));
    }

    /** Le nom (simple) de l'annotation declaree a la derniere ligne : "@interface Twice {}" -> Twice. */
    static String declaredName(String source) {
        String last = source.strip().lines().reduce((a, b) -> b).orElse("");
        return last.replace("@interface", "").replace("{}", "").strip();
    }

    /**
     * Lecture de javap -v : une ligne qui finit par "Annotations:" ouvre une section ; dans une section,
     * une ligne faite d'un seul nom qualifie (avec des points, sans espace) est une annotation.
     */
    static Map<String, Map<String, Integer>> annotationsInClassFile(String javap) {
        Map<String, Map<String, Integer>> found = new TreeMap<>();
        String section = null;
        for (String raw : javap.lines().toList()) {
            String line = raw.strip();
            if (line.endsWith("Annotations:")) {
                section = line.substring(0, line.length() - 1);
            } else if (section != null && line.matches("[\\w$]+(\\.[\\w$]+)+")) {
                String simple = line.substring(line.lastIndexOf('.') + 1);
                found.computeIfAbsent(simple, k -> new TreeMap<>()).merge(section, 1, Integer::sum);
            } else if (!line.isEmpty() && !line.matches("(\\d+: #.*|parameter \\d+:)")) {
                // Toute autre ligne (Code:, une methode, LineNumberTable...) ferme la section.
                section = null;
            }
        }
        return found;
    }

    public static void main(String[] args) {
        String pkg = ComplianceDesk.class.getPackageName();

        // 1. Nos annotations : la matrice mesuree.
        for (String annotation : OURS) {
            System.out.println(matrixLine(annotation, acceptedContexts(Map.of("Probe", probe(pkg, annotation)), "Probe")));
        }

        // 2. Les declarations des autres equipes : d'abord compiler la declaration seule.
        Map<String, String> declarations = Data.DECLARATIONS;
        for (Map.Entry<String, String> e : declarations.entrySet()) {
            List<Javac.Diag> errors = Javac.compile(Map.of(e.getKey(), e.getValue())).diags().stream()
                    .filter(d -> d.kind().equals("ERROR")).toList();
            String name = declaredName(e.getValue());
            if (!errors.isEmpty()) {
                Javac.Diag first = errors.get(0);
                String why = first.code().equals("compiler.err.repeated.annotation.target") ? "cible en double" : first.code();
                System.out.println("DECLARATION " + e.getKey() + " " + name + " refusee l." + first.line() + " : " + why);
                continue;
            }
            Map<String, String> sources = new LinkedHashMap<>();
            sources.put(e.getKey(), e.getValue());
            sources.put("Probe", probe(pkg, name));
            System.out.println(matrixLine(name, acceptedContexts(sources, "Probe")));
        }

        // 3. Ce que @Retention a garde dans le .class.
        Javac.Result sample = Javac.compile(Map.of("Sample", Data.SAMPLE.replace("{{PKG}}", pkg)));
        Map<String, Map<String, Integer>> inClass = annotationsInClassFile(Javac.javap(sample, "Sample"));
        Set<String> runtimeVisible = new TreeSet<>();
        // Seulement les annotations ECRITES dans Sample ("@Nom" suivi d'une fin de mot), par ordre alphabetique.
        for (String annotation : new TreeSet<>(OURS)) {
            if (!Pattern.compile("@" + annotation + "\\b").matcher(Data.SAMPLE).find()) {
                continue;
            }
            Map<String, Integer> where = inClass.get(annotation);
            if (where == null) {
                System.out.println("CLASSFILE " + annotation + " : absente");
                continue;
            }
            String text = where.entrySet().stream().map(w -> w.getKey() + " x" + w.getValue()).collect(Collectors.joining(", "));
            System.out.println("CLASSFILE " + annotation + " : " + text);
            if (where.keySet().stream().anyMatch(s -> s.startsWith("RuntimeVisible"))) {
                runtimeVisible.add(annotation);
            }
        }
        System.out.println("REFLECTION : lisibles a l'execution " + runtimeVisible);
    }
}
