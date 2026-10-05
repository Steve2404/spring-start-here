package vorkurs04_annotations.projects.p01_pluginstore;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 1 (ne pas modifier). Il execute TON PluginStore.main, compare sa
 * sortie a EXPECTED, puis verifie que tes sources declarent et utilisent les annotations visees.
 *
 * L'enonce et le tableau de bord sont dans TODO.md. Lance avec l'argument "solution" pour voir
 * la solution passer. A lancer depuis le dossier base/annotation-kurse (dossier de travail).
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "S01 ACCEPTE",
            "S02 REFUSE : l.6 faux @Override",
            "S03 REFUSE : l.3 element obligatoire manquant",
            "S04 REFUSE : l.3 valeur non constante",
            "S05 ACCEPTE",
            "S06 REFUSE : l.3 @FunctionalInterface invalide",
            "S07 ACCEPTE avec 2 avertissement(s) : l.7 API depreciee ; l.8 API retiree bientot",
            "S08 ACCEPTE",
            "S09 REFUSE : l.8 @SafeVarargs mal place",
            "S10 REFUSE : l.5 annotation repetee",
            "S11 REFUSE : l.4 element inexistant",
            "S12 REFUSE : l.3 mauvais type de valeur",
            "S13 ACCEPTE",
            "S14 ACCEPTE avec 2 avertissement(s) : l.7 varargs generique non sur ; l.8 tableau generique cree a l'appel",
            "S15 REFUSE : l.6 @Override sur une methode static",
            "S16 REFUSE : l.4 type d'element interdit",
            "S17 REFUSE : l.4 valeur non constante",
            "S18 ACCEPTE",
            "BILAN : 7 accepte(s) dont 2 avec avertissements, 11 refuse(s)",
            "BILAN : causes de refus : valeur non constante x2, @FunctionalInterface invalide x1, @Override sur une methode static x1, @SafeVarargs mal place x1, annotation repetee x1, element inexistant x1, element obligatoire manquant x1, faux @Override x1, mauvais type de valeur x1, type d'element interdit x1",
            "BILAN : a migrer avant la v3 : S07",
            "EXECUTION : Plugin Demo a demarre via legacyInit() ; 2 elements via listOf",
            "EXECUTION : HEY");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@interface", "default", "String[]", "Class<?>[]", "enum ", "value()",
            "@FunctionalInterface", "@Override", "@Deprecated(", "since", "forRemoval",
            "@SuppressWarnings(", "@SafeVarargs", "Javac.compile(", ".diags()", ".code()", ".line()", ".kind()",
            "getPackageName()",
            // On classe par CODE javac, jamais par le message (traduit, et qui change d'une version a l'autre).
            "!.message()",
            // Crescendo : @Target / @Retention (0.4.4-0.4.5), meta-annotations (0.4.6) et reflection (0.4.8+)
            // viennent plus tard.
            "!@Target", "!@Retention", "!@Repeatable", "!@Inherited", "!@Documented",
            "!getAnnotation", "!isAnnotationPresent", "!getDeclared", "!getMethods(", "!.invoke(", "!Class.forName");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "PluginStore", args, EXPECTED, API);
    }
}
