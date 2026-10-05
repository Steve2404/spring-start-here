package vorkurs04_annotations.projects.p04_inspector;

import java.util.List;

/**
 * Les donnees du projet 4 (DONNEES, ne pas modifier) : le modele a inspecter, sous forme de SOURCE.
 * Ton programme le compile avec Javac.compile, puis charge chaque classe avec Javac.load et
 * l'inspecte par reflection. Remplace {{PKG}} par le paquet de TES annotations.
 */
public final class Data {

    /** Le modele : une seule source (nom "Model") qui declare plusieurs classes. */
    public static final String MODEL = """
            import {{PKG}}.*;
            import java.util.*;

            @Audit
            @Tag("core")
            class Account {
                @Column(name = "acc_id", nullable = false) long id;
                @Column(name = "owner") @Sensitive String owner;
                List<@NonEmpty String> notes;
                @Trace void close() {}
                @Tag("api") @Tag("v2") String describe(@Sensitive String prefix, int width) { return prefix; }
            }

            class SavingsAccount extends Account {
                @Column(name = "rate") double rate;
            }

            @Tag("x") @Tag("y")
            interface Exportable {}

            @Tag("solo")
            class Loan implements Exportable {
                Map<@NonEmpty String, @Range(max = 10) Integer> limits;
                @NonEmpty String @Range(min = 1, max = 3) [] codes;
                <T extends @NonEmpty CharSequence> List<? extends @Range(max = 5) Number> pick(@NonEmpty T key, List<?> all) {
                    return null;
                }
            }
            """;

    /** Les classes a inspecter, dans l'ordre du rapport. */
    public static final List<String> CLASSES = List.of("Account", "SavingsAccount", "Exportable", "Loan");

    private Data() {
    }
}
