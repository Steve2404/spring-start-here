package vorkurs04_annotations.projects.p01_pluginstore.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p01_pluginstore.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * La boutique : elle compile chaque soumission contre NOTRE API (les annotations et BasePlugin de ce
 * paquet) et rend un verdict. Les annotations ne font rien toutes seules : c'est javac qui les lit
 * et applique leurs regles ; la boutique ne fait que traduire ce que javac dit.
 */
public class PluginStore {

    /** Le verdict d'une soumission : ses erreurs et ses avertissements, deja traduits. */
    record Verdict(String id, List<Javac.Diag> errors, List<Javac.Diag> warnings) {
        boolean accepted() {
            return errors.isEmpty();
        }
    }

    static Verdict review(String id, String source) {
        // Une soumission = un fichier ; on separe les diagnostics par gravite, dans l'ordre des lignes.
        List<Javac.Diag> diags = Javac.compile(Map.of(id, source)).diags();
        List<Javac.Diag> errors = diags.stream().filter(d -> d.kind().equals("ERROR")).toList();
        List<Javac.Diag> warnings = diags.stream().filter(d -> d.kind().equals("WARNING")).toList();
        return new Verdict(id, errors, warnings);
    }

    static String describe(List<Javac.Diag> diags) {
        return diags.stream().map(d -> "l." + d.line() + " " + Reason.labelOf(d.code())).collect(Collectors.joining(" ; "));
    }

    static String line(Verdict v) {
        if (!v.accepted()) {
            return v.id() + " REFUSE : " + describe(v.errors());
        }
        if (v.warnings().isEmpty()) {
            return v.id() + " ACCEPTE";
        }
        return v.id() + " ACCEPTE avec " + v.warnings().size() + " avertissement(s) : " + describe(v.warnings());
    }

    // Le paquet de la boutique compile lui-meme un appel a legacyInit() (pour les vieux plugins) :
    // on assume cet usage, donc on fait taire l'avertissement "removal" ICI seulement.
    @SuppressWarnings("removal")
    static void bootLegacy(BasePlugin plugin) {
        plugin.legacyInit();
    }

    public static void main(String[] args) {
        // Les soumissions importent notre paquet : on leur donne le NOM de notre paquet.
        Map<String, String> submissions = Data.submissions(PluginStore.class.getPackageName());
        List<Verdict> verdicts = new ArrayList<>();
        submissions.forEach((id, source) -> verdicts.add(review(id, source)));
        verdicts.forEach(v -> System.out.println(line(v)));

        long accepted = verdicts.stream().filter(Verdict::accepted).count();
        long withWarnings = verdicts.stream().filter(v -> v.accepted() && !v.warnings().isEmpty()).count();
        System.out.println("BILAN : " + accepted + " accepte(s) dont " + withWarnings + " avec avertissements, "
                + (verdicts.size() - accepted) + " refuse(s)");

        // Les causes de refus, de la plus frequente a la plus rare, puis par ordre alphabetique.
        Map<String, Long> causes = verdicts.stream().flatMap(v -> v.errors().stream())
                .collect(Collectors.groupingBy(d -> Reason.labelOf(d.code()), TreeMap::new, Collectors.counting()));
        String ranked = causes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(e -> e.getKey() + " x" + e.getValue())
                .collect(Collectors.joining(", "));
        System.out.println("BILAN : causes de refus : " + ranked);

        // Les plugins acceptes qui appellent une API qui va disparaitre : a migrer avant la v3.
        List<String> toMigrate = verdicts.stream()
                .filter(v -> v.accepted() && v.warnings().stream().anyMatch(d -> Reason.of(d.code()).equals(Optional.of(Reason.FOR_REMOVAL))))
                .map(Verdict::id)
                .toList();
        System.out.println("BILAN : a migrer avant la v3 : " + (toMigrate.isEmpty() ? "aucun" : String.join(", ", toMigrate)));

        // Les annotations ne s'executent pas : appeler la methode depreciee marche tres bien.
        BasePlugin demo = new BasePlugin() {
            @Override
            public String name() {
                return "Demo";
            }
        };
        bootLegacy(demo);
        System.out.println("EXECUTION : " + demo + " a demarre via legacyInit() ; " + BasePlugin.listOf("a", "b").size() + " elements via listOf");
        Action shout = input -> input.toUpperCase();
        System.out.println("EXECUTION : " + shout.twice().run("hey"));
    }
}
