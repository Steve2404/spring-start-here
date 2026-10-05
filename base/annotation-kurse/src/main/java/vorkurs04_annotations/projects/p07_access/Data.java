package vorkurs04_annotations.projects.p07_access;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 7 (DONNEES, ne pas modifier) : les classes a auditer (SOURCES, une classe
 * publique par source) et la liste des operations a tenter.
 */
public final class Data {

    public static final Map<String, String> TARGETS = new LinkedHashMap<>();

    static {
        TARGETS.put("Vault", """
                public class Vault {
                    private String secret;
                    private final int code = 1234;
                    private final String owner;
                    private static final String VERSION = "1.0";
                    public static int counter = 7;

                    private Vault() { this("vide"); }
                    public Vault(String secret) { this.secret = secret; this.owner = "lea"; }

                    private String reveal() { return secret; }
                    public int code() { return code; }
                    public String owner() { return owner; }
                    protected static String hint(String p) { return p + "!"; }
                }
                """);
        TARGETS.put("Point", "public record Point(int x, int y) {}\n");
        TARGETS.put("Shape", "public abstract class Shape { public Shape() {} }\n");
        TARGETS.put("Thrower", "public class Thrower { public Thrower() { throw new IllegalStateException(\"boom\"); } }\n");
    }

    /**
     * Les operations, dans l'ordre :
     *   READ  Classe.champ            lire un champ
     *   WRITE Classe.champ valeur     ecrire un champ
     *   CALL  Classe.methode [arg]    appeler une methode (0 ou 1 argument String)
     *   CREATE Classe                 creer un objet avec le constructeur SANS argument
     * Les classes String (java.lang.String) et les classes de TARGETS sont visees.
     */
    public static final List<String> PROBES = List.of(
            "READ Vault.counter",
            "READ Vault.secret",
            "CALL Vault.reveal",
            "WRITE Vault.secret xyz",
            "CALL Vault.reveal",
            "WRITE Vault.owner tom",
            "CALL Vault.owner",
            "WRITE Vault.code 99",
            "READ Vault.code",
            "CALL Vault.code",
            "WRITE Vault.VERSION 2.0",
            "CALL Vault.hint ok",
            "READ Vault.missing",
            "CREATE Vault",
            "WRITE Point.x 5",
            "CREATE Shape",
            "CREATE Thrower",
            "READ String.value");

    private Data() {
    }
}
