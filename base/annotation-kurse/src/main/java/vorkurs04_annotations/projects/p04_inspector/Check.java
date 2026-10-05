package vorkurs04_annotations.projects.p04_inspector;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 4 (ne pas modifier). Il execute TON Inspector.main, compare sa sortie
 * a EXPECTED, puis verifie que tu as pratique toute l'API de lecture des annotations (0.4.8).
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "CLASSE Account : declarees [Audit, Tag] ; visibles [Audit, Tag]",
            "TAGS Account : par type [core] ; getAnnotation(Tag) core ; conteneur absent",
            "CHAMP Account.id : long ; @Column(name=acc_id, nullable=false)",
            "CHAMP Account.notes : List<@NonEmpty String> ; aucune",
            "CHAMP Account.owner : String ; @Column(name=owner, nullable=true) @Sensitive",
            "METHODE Account.close : retour void ; visibles [] ; tags []",
            "METHODE Account.describe : retour String ; visibles [Tags] ; tags [api, v2]",
            "PARAM Account.describe#0 arg0 (nom absent) : String ; declarees [Sensitive]",
            "PARAM Account.describe#1 arg1 (nom absent) : int ; declarees []",
            "CLASSE SavingsAccount : declarees [] ; visibles [Audit]",
            "TAGS SavingsAccount : par type [] ; getAnnotation(Tag) absent ; conteneur absent",
            "CHAMP SavingsAccount.rate : double ; @Column(name=rate, nullable=true)",
            "CLASSE Exportable : declarees [Tags] ; visibles [Tags]",
            "TAGS Exportable : par type [x, y] ; getAnnotation(Tag) absent ; conteneur 2 tag(s)",
            "CLASSE Loan : declarees [Tag] ; visibles [Tag]",
            "TAGS Loan : par type [solo] ; getAnnotation(Tag) solo ; conteneur absent",
            "CHAMP Loan.codes : @NonEmpty String @Range(1..3) [] ; aucune",
            "CHAMP Loan.limits : Map<@NonEmpty String, @Range(0..10) Integer> ; aucune",
            "METHODE Loan.pick : retour List<? extends @Range(0..5) Number> ; visibles [] ; tags []",
            "TYPEPARAM Loan.pick : T extends @NonEmpty CharSequence",
            "PARAM Loan.pick#0 arg0 (nom absent) : @NonEmpty T ; declarees []",
            "PARAM Loan.pick#1 arg1 (nom absent) : List<?> ; declarees []",
            "RETENTION : Trace sur close visible a l'execution ? false ; dans le .class ? true");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Inherited", "@Repeatable(", "Javac.compile(", "Javac.load(", "Javac.javap(",
            ".getAnnotation(", ".getDeclaredAnnotation(", ".getAnnotations()", ".getDeclaredAnnotations()",
            ".getAnnotationsByType(", ".isAnnotationPresent(", ".annotationType()",
            ".getDeclaredFields()", ".getDeclaredMethods()", ".isSynthetic()", ".getParameters()", ".isNamePresent()",
            ".getAnnotatedType()", ".getAnnotatedReturnType()", ".getTypeParameters()", ".getAnnotatedBounds()",
            "AnnotatedParameterizedType", ".getAnnotatedActualTypeArguments()",
            "AnnotatedArrayType", ".getAnnotatedGenericComponentType()",
            "AnnotatedWildcardType", ".getAnnotatedUpperBounds()", ".getAnnotatedLowerBounds()",
            "AnnotatedTypeVariable", "Comparator",
            // Crescendo : invoke / instanciation (0.4.10, 0.4.12), signatures generiques (0.4.11),
            // processors (0.4.13) viennent plus tard.
            "!.invoke(", "!setAccessible", "!trySetAccessible", "!newInstance",
            "!getGenericType", "!getGenericReturnType", "!getGenericParameterTypes", "!AbstractProcessor");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Inspector", args, EXPECTED, API);
    }
}
