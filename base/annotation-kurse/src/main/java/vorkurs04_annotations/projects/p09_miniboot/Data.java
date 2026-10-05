package vorkurs04_annotations.projects.p09_miniboot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les donnees du projet 9 (DONNEES, ne pas modifier).
 *
 * SOURCES : toutes les classes candidates (une classe publique par source), a compiler ENSEMBLE une
 * fois. APPS : des applications ; chacune choisit les classes qu'elle scanne et sa configuration.
 * Remplace {{PKG}} par le paquet de TES annotations.
 */
public final class Data {

    public static final Map<String, String> SOURCES = new LinkedHashMap<>();

    static {
        String imp = "import {{PKG}}.*;\n";
        SOURCES.put("Clock", imp + """
                @MiniComponent
                public class Clock {
                    public String now() { return "08:00"; }
                }
                """);
        SOURCES.put("Repository", imp + """
                @MiniComponent
                public class Repository {
                    @Inject private Clock clock;
                    public String save(String item) { return item + "@" + clock.now(); }
                }
                """);
        SOURCES.put("Mailer", "public interface Mailer { String send(String to); }\n");
        SOURCES.put("SmtpMailer", imp + """
                @MiniComponent
                public class SmtpMailer implements Mailer {
                    public String send(String to) { return "mail->" + to; }
                }
                """);
        SOURCES.put("OrderService", imp + """
                @MiniComponent
                public class OrderService {
                    @Inject private Repository repository;
                    @Inject private Mailer mailer;
                    @Inject private ShopSettings settings;
                    @OnStart public void warmUp() {
                        System.out.println("  warmUp : " + repository.save("order-1") + " " + mailer.send(settings.owner())
                                + " port " + settings.port());
                    }
                }
                """);
        SOURCES.put("ShopSettings", imp + """
                @MiniConfig(prefix = "shop")
                public class ShopSettings {
                    @ConfigValue(key = "port") private int port;
                    @ConfigValue(key = "owner", required = false, defaultValue = "admin") private String owner;
                    @ConfigValue(key = "debug", required = false, defaultValue = "false") private boolean debug;
                    @ConfigValue(key = "timeout") private long timeout;
                    @AfterLoad void check() {
                        if (port < 1 || port > 65535) { throw new IllegalStateException("port hors limites : " + port); }
                    }
                    public int port() { return port; }
                    public String owner() { return owner; }
                }
                """);
        SOURCES.put("Audit", imp + """
                @MiniComponent
                public class Audit {
                    @Inject private OrderService orders;
                    @OnStart public void open() { System.out.println("  open : audit pret"); }
                }
                """);
        SOURCES.put("Ping", imp + """
                @MiniComponent
                public class Ping {
                    @Inject private Pong pong;
                }
                """);
        SOURCES.put("Pong", imp + """
                @MiniComponent
                public class Pong {
                    @Inject private Ping ping;
                }
                """);
        SOURCES.put("BadStart", imp + """
                @MiniComponent
                public class BadStart {
                    @OnStart public String start(int level) { return "x"; }
                }
                """);
        SOURCES.put("Fragile", imp + """
                @MiniComponent
                public class Fragile {
                    public Fragile() { throw new IllegalStateException("disque plein"); }
                }
                """);
        SOURCES.put("StrictSettings", imp + """
                @MiniConfig(prefix = "strict")
                public class StrictSettings {
                    @ConfigValue(key = "retries") private int retries;
                    @ConfigValue(key = "enabled") private boolean enabled;
                    @ConfigValue(key = "name") private final String name = "fixe";
                    @ConfigValue(key = "level") private static int level;
                }
                """);
        SOURCES.put("AbstractJob", imp + """
                @MiniComponent
                public abstract class AbstractJob {}
                """);
    }

    /** Une application : son nom, les classes scannees (dans cet ordre), et sa configuration. */
    public record App(String name, List<String> scan, Map<String, String> properties) {
    }

    public static final List<App> APPS = List.of(
            new App("boutique",
                    List.of("Audit", "Clock", "Mailer", "OrderService", "Repository", "ShopSettings", "SmtpMailer"),
                    Map.of("shop.port", "8080", "shop.timeout", "30")),
            new App("port-invalide",
                    List.of("Clock", "Repository", "ShopSettings"),
                    Map.of("shop.port", "70000", "shop.timeout", "5", "shop.owner", "lea")),
            new App("cycle",
                    List.of("Clock", "Ping", "Pong"),
                    Map.of()),
            new App("sans-mailer",
                    List.of("Clock", "OrderService", "Repository", "ShopSettings"),
                    Map.of("shop.port", "80", "shop.timeout", "1")),
            new App("mauvaise-config",
                    List.of("StrictSettings"),
                    Map.of("strict.retries", "trois", "strict.enabled", "yes")),
            new App("structure",
                    List.of("AbstractJob", "BadStart", "Clock"),
                    Map.of()),
            new App("fragile",
                    List.of("Clock", "Fragile"),
                    Map.of()));

    private Data() {
    }
}
