package vorkurs04_annotations.projects.p06_schema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 6 (DONNEES, ne pas modifier). Les classes cibles de l'import sont des SOURCES
 * (une classe publique par source) : compile-les ensemble, charge-les avec Javac.load.
 * Remplace {{PKG}} par le paquet de TES annotations @Positive et @NotBlank.
 */
public final class Data {

    /** nom de classe -> source, dans l'ordre du rapport. */
    public static final Map<String, String> SCHEMAS = new LinkedHashMap<>();

    static {
        SCHEMAS.put("Order", """
                import {{PKG}}.*;
                import java.util.*;

                public class Order {
                    public String id;
                    public List<@Positive Integer> quantities;
                    public Map<String, Double> prices;
                    public String[] tags;
                    public List<List<Integer>> matrix;
                    public Optional<@NotBlank String> note;
                }
                """);
        SCHEMAS.put("Broken", """
                import java.util.*;

                public class Broken {
                    public List<? extends Number> numbers;
                    @SuppressWarnings("rawtypes") public List raw;
                    public Map<String, List<?>> nested;
                    public List<String>[] pages;
                    public Map.Entry<String, Integer> first;
                }
                """);
        SCHEMAS.put("Box", """
                import java.util.*;

                public class Box<T extends Comparable<T>> {
                    public T value;
                    public List<T> history;
                }
                """);
        SCHEMAS.put("IntBox", """
                import {{PKG}}.*;

                public class IntBox extends Box<@Positive Integer> {
                    public String label;
                }
                """);
        SCHEMAS.put("Ranking", """
                import java.util.*;

                public class Ranking {
                    @SafeVarargs
                    public static <E extends Comparable<E>> List<E> top(Map<String, ? super E> source, int limit, E... extra)
                            throws IllegalStateException {
                        return List.of(extra);
                    }
                    public List<String> words;
                    public List<Integer> counts;
                }
                """);
    }

    /** Les lignes a importer : "Classe | champ=valeur ; champ=valeur ..." (voir TODO.md pour le format des valeurs). */
    public static final List<String> ROWS = List.of(
            "Order | id=A1 ; quantities=1,2,3 ; prices=pen:1.5,ink:2 ; tags=x,y ; matrix=1,2/3 ; note=urgent",
            "Order | id=A2 ; quantities=4,0 ; prices= ; tags= ; matrix= ; note=   ",
            "Order | id=A3 ; quantities= ; note=",
            "IntBox | value=7 ; history=1,2 ; label=lot",
            "IntBox | value=-1 ; history= ; label=neg",
            "Broken | numbers=1,2");

    private Data() {
    }
}
