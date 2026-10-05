package vorkurs04_annotations.projects.p03_typeguard;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 3 (ne pas modifier). Il execute TON TypeGuard.main, compare sa sortie
 * a EXPECTED, puis verifie l'usage des meta-annotations et des annotations de type.
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "CONTENEUR C1 OK",
            "CONTENEUR C2 REFUSE l.2 : le conteneur n'a pas d'element value",
            "CONTENEUR C3 REFUSE l.2 : value n'est pas un tableau de l'annotation",
            "CONTENEUR C4 REFUSE l.2 : conteneur moins durable",
            "CONTENEUR C5 REFUSE l.2 : conteneur applicable a plus d'endroits",
            "CONTENEUR C6 REFUSE l.2 : conteneur non @Documented",
            "CONTENEUR C7 REFUSE l.2 : conteneur non @Inherited",
            "CONTENEUR C8 REFUSE l.2 : autre element sans valeur par defaut",
            "CONTENEUR C9 OK",
            "PLACEMENT P01 OK",
            "PLACEMENT P02 OK",
            "PLACEMENT P03 OK",
            "PLACEMENT P04 REFUSE l.3 : contexte interdit",
            "PLACEMENT P05 OK",
            "PLACEMENT P06 REFUSE l.3 : annotation devant un nom qualifie",
            "PLACEMENT P07 OK",
            "PLACEMENT P08 REFUSE l.3 : annotation devant un nom qualifie",
            "PLACEMENT P09 OK",
            "PLACEMENT P10 REFUSE l.3 : contexte interdit",
            "PLACEMENT P11 REFUSE l.3 : annotation sur .class",
            "PLACEMENT P12 OK",
            "PLACEMENT P13 OK",
            "PLACEMENT P14 OK",
            "PLACEMENT P15 REFUSE l.3 : pas une annotation de type",
            "PLACEMENT P16 OK",
            "CHAMP title : @NonEmpty sur type entier",
            "CHAMP lines : @NonEmpty sur element",
            "CHAMP pages : @NonEmpty sur type entier",
            "CHAMP grid : @NonEmpty sur element > element",
            "CHAMP index : @NonEmpty sur argument 0, @NonEmpty sur argument 1 > argument 0",
            "CHAMP code : @Trimmed sur declaration, @Trimmed sur type entier",
            "CHAMP codes : @Trimmed sur declaration, @Trimmed sur element",
            "CHAMP plain : @Column sur declaration",
            "JAVADOC Report : Public Tag Tag");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Repeatable(", "@Documented", "@Inherited", "ElementType.TYPE_USE", "ElementType.FIELD",
            "Data.CONTAINERS", "Data.PLACEMENTS", "Data.MODEL", "Data.REPORT",
            "Javac.compile(", "Javac.javap(", "Javac.javadocPage(", ".code()", ".line()",
            "!.message()",
            // Crescendo : la reflection arrive au projet 4.
            "!getAnnotation", "!isAnnotationPresent", "!getDeclared", "!getMethods(", "!.invoke(", "!Class.forName");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "TypeGuard", args, EXPECTED, API);
    }
}
