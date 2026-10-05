package vorkurs04_annotations.projects.p06_schema;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 6 (ne pas modifier). Il execute TON SchemaCheck.main, compare sa sortie a
 * EXPECTED, puis verifie la pratique de la reflection sur les generiques (0.4.11).
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "SCHEMA Order.id : String",
            "SCHEMA Order.matrix : List<List<Integer>>",
            "SCHEMA Order.note : Optional<@NotBlank String>",
            "SCHEMA Order.prices : Map<String, Double>",
            "SCHEMA Order.quantities : List<@Positive Integer>",
            "SCHEMA Order.tags : String[]",
            "REJET Broken.first : type imbrique dans Map.Entry<String, Integer>",
            "REJET Broken.nested : joker dans Map<String, List<?>>",
            "REJET Broken.numbers : joker dans List<? extends Number>",
            "REJET Broken.pages : tableau generique dans List<String>[]",
            "REJET Broken.raw : type brut dans List",
            "REJET Box.history : variable T de Box non resolue (borne Comparable<T>) dans List<T>",
            "REJET Box.value : variable T de Box non resolue (borne Comparable<T>) dans T",
            "SCHEMA IntBox.history : List<@Positive Integer>",
            "SCHEMA IntBox.label : String",
            "SCHEMA IntBox.value : @Positive Integer",
            "SCHEMA Ranking.counts : List<Integer>",
            "SCHEMA Ranking.words : List<String>",
            "IMPORT ligne 1 Order : id=A1 matrix=[[1, 2], [3]] note=Optional[urgent] prices={pen=1.5, ink=2.0} quantities=[1, 2, 3] tags=[x, y]",
            "IMPORT ligne 2 Order : REFUS note ne doit pas etre vide ; quantities[1] = 0 doit etre > 0",
            "IMPORT ligne 3 Order : id=A3 matrix=absent note=Optional.empty prices=absent quantities=[] tags=absent",
            "IMPORT ligne 4 IntBox : history=[1, 2] label=lot value=7",
            "IMPORT ligne 5 IntBox : REFUS value = -1 doit etre > 0",
            "IMPORT ligne 6 Broken : REFUS schema invalide",
            "SIGNATURE <E extends Comparable<E>> List<E> top(Map<String, ? super E>, int, E...) throws IllegalStateException",
            "EFFACEMENT words/counts : meme getType ? true ; meme getGenericType ? false",
            "EFFACEMENT top : [Map, int, Comparable[]]");
            // EXPECTED-END

    static final List<String> API = List.of(
            "ElementType.TYPE_USE", "Javac.compile(", "Javac.load(", "Data.SCHEMAS", "Data.ROWS",
            ".getFields()", ".getType()", ".getGenericType()", ".getAnnotatedType()",
            "ParameterizedType", ".getRawType()", ".getActualTypeArguments()", ".getOwnerType()",
            "TypeVariable", ".getBounds()", ".getGenericDeclaration()",
            "WildcardType", ".getUpperBounds()", ".getLowerBounds()",
            "GenericArrayType", ".getGenericComponentType()",
            ".getGenericSuperclass()", ".getAnnotatedSuperclass()", ".getTypeParameters()",
            "AnnotatedParameterizedType", ".getAnnotatedActualTypeArguments()", ".isAnnotationPresent(",
            ".getGenericReturnType()", ".getGenericParameterTypes()", ".getGenericExceptionTypes()", ".getParameterTypes()",
            // Le rendu des types est TON algorithme : pas de getTypeName() / toString() tout faits.
            "!getTypeName",
            // Crescendo : instancier et forcer l'acces (0.4.12), processors (0.4.13).
            "!.invoke(", "!newInstance", "!setAccessible", "!AbstractProcessor");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "SchemaCheck", args, EXPECTED, API);
    }
}
