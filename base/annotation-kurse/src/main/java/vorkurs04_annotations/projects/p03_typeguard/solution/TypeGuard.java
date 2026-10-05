package vorkurs04_annotations.projects.p03_typeguard.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p03_typeguard.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TypeGuard verifie trois choses sans reflection : les regles des conteneurs @Repeatable (javac),
 * les placements des annotations de type (javac), puis OU chaque annotation atterrit dans le .class
 * (javap : declaration ou partie precise du type) et ce que javadoc publie (@Documented).
 */
public class TypeGuard {

    static final Map<String, String> CONTAINER_RULES = Map.of(
            "compiler.err.invalid.repeatable.annotation.no.value", "le conteneur n'a pas d'element value",
            "compiler.err.invalid.repeatable.annotation.value.return", "value n'est pas un tableau de l'annotation",
            "compiler.err.invalid.repeatable.annotation.retention", "conteneur moins durable",
            "compiler.err.invalid.repeatable.annotation.incompatible.target", "conteneur applicable a plus d'endroits",
            "compiler.err.invalid.repeatable.annotation.not.documented", "conteneur non @Documented",
            "compiler.err.invalid.repeatable.annotation.not.inherited", "conteneur non @Inherited",
            "compiler.err.invalid.repeatable.annotation.elem.nondefault", "autre element sans valeur par defaut");

    static final Map<String, String> PLACEMENT_RULES = Map.of(
            "compiler.err.cant.type.annotate.scoping.1", "annotation devant un nom qualifie",
            "compiler.err.annotation.type.not.applicable", "contexte interdit",
            "compiler.err.annotation.type.not.applicable.to.type", "pas une annotation de type",
            "compiler.err.no.annotations.on.dot.class", "annotation sur .class");

    static String verdict(String kind, String id, String source, Map<String, String> rules) {
        // Meme schema qu'au projet 1 : on compile, on garde la 1re erreur, on la traduit par son CODE.
        List<Javac.Diag> errors = Javac.compile(Map.of(id, source)).diags().stream()
                .filter(d -> d.kind().equals("ERROR")).toList();
        if (errors.isEmpty()) {
            return kind + " " + id + " OK";
        }
        Javac.Diag first = errors.get(0);
        return kind + " " + id + " REFUSE l." + first.line() + " : " + rules.getOrDefault(first.code(), first.code());
    }

    /** Un pas de "location" javap -> un mot : ARRAY = element du tableau, TYPE_ARGUMENT(i) = argument i. */
    static String step(String javapStep) {
        if (javapStep.equals("ARRAY")) {
            return "element";
        }
        Matcher arg = Pattern.compile("TYPE_ARGUMENT\\((\\d+)\\)").matcher(javapStep);
        return arg.matches() ? "argument " + arg.group(1) : javapStep.toLowerCase();
    }

    static String path(String location) {
        // Pas de location = l'annotation porte sur le type ENTIER du champ (pour un tableau : le tableau lui-meme).
        if (location == null) {
            return "type entier";
        }
        List<String> steps = new ArrayList<>();
        for (String s : location.split(", ")) {
            steps.add(step(s));
        }
        return String.join(" > ", steps);
    }

    /**
     * javap -v : un champ s'annonce par une ligne indentee de 2 espaces qui finit par ';' (sans parenthese).
     * Dans RuntimeVisibleAnnotations, chaque nom qualifie est une annotation de DECLARATION ; dans
     * RuntimeVisibleTypeAnnotations, la ligne "0: #12(): FIELD, location=[...]" qui precede le nom donne la PARTIE du type.
     */
    static Map<String, List<String>> annotationsByField(String javap) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        Pattern fieldLine = Pattern.compile("^ {2}[^ ].*[ ](\\w+);$");
        Pattern typeEntry = Pattern.compile("\\d+: #\\d+\\(\\): FIELD(?:, location=\\[(.*)])?");
        String field = null;
        String section = null;
        String location = null;
        for (String raw : javap.lines().toList()) {
            Matcher f = fieldLine.matcher(raw);
            String line = raw.strip();
            if (f.matches() && !raw.contains("(")) {
                field = f.group(1);
                result.put(field, new ArrayList<>());
                section = null;
            } else if (raw.startsWith("  ") && !raw.startsWith("   ") && raw.contains("(")) {
                field = null;
            } else if (field != null && line.endsWith("Annotations:")) {
                section = line;
            } else if (field != null && section != null) {
                Matcher t = typeEntry.matcher(line);
                if (t.matches()) {
                    location = t.group(1);
                } else if (line.matches("[\\w$]+(\\.[\\w$]+)*") && !line.isEmpty()) {
                    String name = line.substring(line.lastIndexOf('.') + 1);
                    String where = section.startsWith("RuntimeVisibleTypeAnnotations") ? path(location) : "declaration";
                    result.get(field).add("@" + name + " sur " + where);
                    location = null;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String pkg = TypeGuard.class.getPackageName();

        Data.CONTAINERS.forEach((id, source) -> System.out.println(verdict("CONTENEUR", id, source, CONTAINER_RULES)));
        Data.PLACEMENTS.forEach((id, source) -> System.out.println(verdict("PLACEMENT", id, source.replace("{{PKG}}", pkg), PLACEMENT_RULES)));

        Javac.Result model = Javac.compile(Map.of("Model", Data.MODEL.replace("{{PKG}}", pkg)));
        annotationsByField(Javac.javap(model, "Model")).forEach((field, found) ->
                System.out.println("CHAMP " + field + " : " + (found.isEmpty() ? "rien" : String.join(", ", found))));

        // javadoc ne publie que les annotations @Documented ; un @Tag repete reste visible deux fois.
        String page = Javac.javadocPage(Map.of("Report", Data.REPORT.replace("{{PKG}}", pkg)), "Report");
        String shown = Pattern.compile("@(Public|Internal|Tag)\\b").matcher(page).results()
                .map(m -> m.group(1)).collect(Collectors.joining(" "));
        System.out.println("JAVADOC Report : " + shown);
    }
}
