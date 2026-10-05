package vorkurs02_xml.projects.p11_three;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Les donnees du projet 11 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    public static final int PRODUCTS = 30_000;

    private static Path catalog;

    /**
     * Le chemin d'un gros catalogue de 30 000 produits, genere une fois (toujours le meme contenu).
     * Chaque produit : &lt;product sku="P00001" category="book|music|game"&gt;&lt;name&gt;...&lt;/name&gt;&lt;price&gt;12.34&lt;/price&gt;&lt;/product&gt;.
     */
    public static synchronized Path catalog() {
        if (catalog == null) {
            try {
                catalog = Files.createTempFile("catalog-", ".xml");
                catalog.toFile().deleteOnExit();
                try (Writer w = Files.newBufferedWriter(catalog, StandardCharsets.UTF_8)) {
                    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<catalog>\n");
                    String[] categories = {"book", "music", "game"};
                    for (int i = 1; i <= PRODUCTS; i++) {
                        int cents = (i * 7919) % 9_900 + 100;
                        // Un prix sur 1000 est ecrit avec des blancs autour : a toi de les retirer.
                        String price = cents / 100 + "." + String.format("%02d", cents % 100);
                        if (i % 1000 == 0) {
                            price = "\n      " + price + "\n    ";
                        }
                        w.write(String.format("  <product sku=\"P%05d\" category=\"%s\">%n    <name>Article %d</name>%n    <price>%s</price>%n  </product>%n",
                                i, categories[i % 3], i, price));
                    }
                    w.write("</catalog>\n");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return catalog;
    }

    /** Un petit document casse a la ligne 3 (une balise fermante qui ne correspond pas). */
    public static final String BROKEN = "<catalog>\n  <product sku=\"X\">\n    <price>1.00</prix>\n  </product>\n</catalog>\n";

    /** Les besoins d'une application (etape 5). */
    public record Needs(String name, boolean modify, boolean randomAccess, boolean hugeFile, boolean earlyStop) {
    }

    public static final List<Needs> SCENARIOS = List.of(
            new Needs("migration de configuration", true, true, false, false),
            new Needs("import nocturne de 40 Go", false, false, true, false),
            new Needs("premiere commande en erreur dans un flux", false, false, true, true),
            new Needs("rapport croise sur un petit fichier", false, true, false, false),
            new Needs("petit fichier lu une fois", false, false, false, false),
            new Needs("recherche avec navigation dans un gros fichier", false, true, true, false));
}
