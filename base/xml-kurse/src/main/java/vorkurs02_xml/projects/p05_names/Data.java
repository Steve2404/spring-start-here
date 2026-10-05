package vorkurs02_xml.projects.p05_names;

import java.util.List;

/**
 * Les donnees du projet 5 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    /** Une facture qui melange plusieurs vocabulaires (etapes 1 a 4). */
    public static final String INVOICE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <inv:invoice xmlns:inv="urn:shop:invoice" xmlns="urn:shop:common"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xmlns:money="urn:shop:money" id="F-1" xml:lang="fr"
                         xsi:schemaLocation="urn:shop:invoice invoice.xsd urn:shop:money money.xsd">
              <customer inv:vip="true">Lea</customer>
              <inv:line nr="1">
                <amount xsi:type="money:Euro">12.50</amount>
                <note xmlns="">sans namespace</note>
                <inv:tax xmlns:inv="urn:shop:tax" rate="20"/>
              </inv:line>
              <money:total xmlns:m2="urn:shop:money" m2:currency="EUR">12.50</money:total>
            </inv:invoice>
            """;

    public record Submission(String id, String text) {
    }

    /** Des documents a juger avec les regles des namespaces (etape 5). */
    public static final List<Submission> CASES = List.of(
            new Submission("B01", "<a:order><a:item/></a:order>"),
            new Submission("B02", "<a:order xmlns:a=\"urn:a\">\n  <a:item xmlns:a=\"\"/>\n</a:order>"),
            new Submission("B03", "<order xmlns:xml=\"urn:pas-le-bon\"/>"),
            new Submission("B04", "<order xmlns:xmlns=\"urn:x\"/>"),
            new Submission("B05", "<o xmlns:a=\"urn:x\" xmlns:b=\"urn:x\" a:id=\"1\" b:id=\"2\"/>"),
            new Submission("B06", "<a:b:c xmlns:a=\"urn:x\"/>"),
            new Submission("B07", "<o xmlns=\"\">vide</o>"),
            new Submission("B08", "<p:o xmlns:p=\"urn:p\"><p:i xmlns:p=\"urn:q\"/></p:o>"),
            new Submission("B09", "<o xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"/>"));

    /** La meme commande, envoyee par trois systemes (etape 6). */
    public static final String ORDER_A = """
            <inv:order xmlns:inv="urn:shop:invoice" xmlns:c="urn:shop:customer"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xmlns:m="urn:shop:money" inv:id="A-1">
              <c:customer c:vip="true">Lea</c:customer>
              <inv:line sku="X1"><inv:amount xsi:type="m:Euro">12.50</inv:amount></inv:line>
              <inv:line sku="X2"><inv:amount xsi:type="m:Euro">7.25</inv:amount></inv:line>
            </inv:order>
            """;

    public static final String ORDER_B = """
            <!-- la meme commande, ecrite par un autre systeme -->
            <order xmlns="urn:shop:invoice" xmlns:inv="urn:shop:invoice" inv:id="A-1">
              <k:customer xmlns:k="urn:shop:customer" k:vip="true">  Lea  </k:customer>
              <line sku="X1">
                <amount xmlns:s="http://www.w3.org/2001/XMLSchema-instance"
                        xmlns:euro="urn:shop:money" s:type="euro:Euro">12.50</amount>
              </line>
              <line sku="X2">
                <amount xmlns:s="http://www.w3.org/2001/XMLSchema-instance"
                        xmlns:euro="urn:shop:money" s:type="euro:Euro">7.25</amount>
              </line>
            </order>
            """;

    public static final String ORDER_C = """
            <inv:order xmlns:inv="urn:shop:invoice" xmlns:c="urn:shop:Customer"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xmlns:m="urn:shop:money" inv:id="A-1">
              <c:customer c:vip="true">Lea</c:customer>
              <inv:line sku="X1"><inv:amount xsi:type="m:Euro">12.50</inv:amount></inv:line>
              <inv:line sku="X2"><inv:amount xsi:type="m:Euro">7.25</inv:amount></inv:line>
            </inv:order>
            """;
}
