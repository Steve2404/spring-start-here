package vorkurs04_annotations.projects.p02_compliance;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 2 (ne pas modifier). Il execute TON ComplianceDesk.main, compare sa
 * sortie a EXPECTED, puis verifie que tes sources utilisent toutes les cibles et retentions visees.
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "MATRICE Audited : type annotation record method",
            "MATRICE Sensitive : record field parameter",
            "MATRICE Factory : record constructor method",
            "MATRICE Trace : record method",
            "MATRICE Generated : type annotation",
            "MATRICE Local : local",
            "MATRICE Meta : annotation",
            "MATRICE TypeTag : typeparam",
            "MATRICE NotNull : type annotation record typeparam field constructor parameter local typeuse",
            "MATRICE Anywhere : type annotation record field constructor method parameter local",
            "DECLARATION D1 Twice refusee l.2 : cible en double",
            "MATRICE Nowhere : aucun contexte",
            "MATRICE Checked : type annotation record typeparam field constructor parameter local typeuse",
            "MATRICE Plain : type annotation record field constructor method parameter local",
            "CLASSFILE Audited : RuntimeVisibleAnnotations x2",
            "CLASSFILE Factory : RuntimeInvisibleAnnotations x2",
            "CLASSFILE Generated : absente",
            "CLASSFILE Local : absente",
            "CLASSFILE NotNull : RuntimeVisibleTypeAnnotations x1",
            "CLASSFILE Sensitive : RuntimeVisibleAnnotations x1, RuntimeVisibleParameterAnnotations x1",
            "CLASSFILE Trace : RuntimeInvisibleAnnotations x1",
            "REFLECTION : lisibles a l'execution [Audited, NotNull, Sensitive]");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Target(", "@Retention(",
            "ElementType.TYPE", "ElementType.METHOD", "ElementType.FIELD", "ElementType.PARAMETER",
            "ElementType.CONSTRUCTOR", "ElementType.LOCAL_VARIABLE", "ElementType.ANNOTATION_TYPE",
            "ElementType.TYPE_PARAMETER", "ElementType.TYPE_USE",
            "RetentionPolicy.SOURCE", "RetentionPolicy.CLASS", "RetentionPolicy.RUNTIME",
            "Data.CONTEXTS", "Data.DECLARATIONS", "Data.SAMPLE",
            "Javac.compile(", "Javac.javap(", ".file()", ".line()", ".code()",
            "!.message()",
            // Crescendo : meta-annotations (0.4.6) et reflection (0.4.8+) viennent plus tard.
            "!@Repeatable", "!@Inherited", "!@Documented",
            "!getAnnotation", "!isAnnotationPresent", "!getDeclared", "!getMethods(", "!.invoke(", "!Class.forName");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "ComplianceDesk", args, EXPECTED, API);
    }
}
