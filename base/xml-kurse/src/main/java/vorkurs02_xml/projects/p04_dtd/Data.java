package vorkurs02_xml.projects.p04_dtd;

import java.util.List;

/**
 * Les donnees du projet 4 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    /** Le contenu du fichier externe "catalog.dtd", partage par tous les documents. */
    public static final String CATALOG_DTD = """
            <!-- DTD externe du catalogue -->
            <!ELEMENT catalog (section+)>
            <!ATTLIST catalog version CDATA #FIXED "2" lang (fr|de|en) "fr">
            <!ELEMENT section (heading, (item | bundle)*, note?, extra?)>
            <!ATTLIST section id ID #REQUIRED>
            <!ELEMENT heading (#PCDATA)>
            <!ELEMENT item (name, price, (tag, tag?)?)>
            <!ATTLIST item
                      sku    ID         #REQUIRED
                      see    IDREF      #IMPLIED
                      status (new|old)  "new">
            <!ELEMENT bundle (item, item+)>
            <!ELEMENT name (#PCDATA)>
            <!ELEMENT price (#PCDATA)>
            <!ELEMENT tag (#PCDATA)>
            <!ELEMENT note (#PCDATA | b | i | br)*>
            <!ELEMENT b (#PCDATA)>
            <!ELEMENT i (#PCDATA)>
            <!ELEMENT br EMPTY>
            <!ELEMENT extra ANY>
            """;

    public record Submission(String id, String text) {
    }

    private static final String HEAD = "<?xml version=\"1.0\"?>\n<!DOCTYPE catalog SYSTEM \"catalog.dtd\">\n";

    public static final List<Submission> DOCUMENTS = List.of(
            new Submission("V01", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Papeterie</heading>
                        <item sku="a1"><name>Stylo</name><price>2</price></item>
                        <bundle>
                          <item sku="a2" see="a1"><name>Gomme</name><price>1</price><tag>eco</tag></item>
                          <item sku="a3"><name>Regle</name><price>3</price><tag>bois</tag><tag>30cm</tag></item>
                        </bundle>
                        <note>Livraison <b>gratuite</b> des <i>50</i> euros<br/></note>
                        <extra>libre <item sku="a4"><name>Bonus</name><price>0</price></item></extra>
                      </section>
                      <section id="s2"><heading>Vide</heading></section>
                    </catalog>"""),
            new Submission("V02", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Lot</heading>
                        <bundle><item sku="a1"><name>Seul</name><price>1</price></item></bundle>
                      </section>
                    </catalog>"""),
            new Submission("V03", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Tags</heading>
                        <item sku="a1"><name>Stylo</name><price>2</price><tag>a</tag><tag>b</tag><tag>c</tag></item>
                      </section>
                    </catalog>"""),
            new Submission("V04", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Ordre</heading>
                        <note>trop tot</note>
                        <item sku="a1"><name>Stylo</name><price>2</price></item>
                      </section>
                    </catalog>"""),
            new Submission("V05", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Texte</heading>
                        <item sku="a1">promo <name>Stylo</name><price>2</price></item>
                      </section>
                    </catalog>"""),
            new Submission("V06", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Inconnu</heading>
                        <item sku="a1"><name>Stylo</name><price>2</price><couleur>bleu</couleur></item>
                      </section>
                    </catalog>"""),
            new Submission("V07", HEAD + """
                    <catalog version="3">
                      <section>
                        <heading>Attributs</heading>
                        <item sku="a1" status="used" color="red"><name>Stylo</name><price>2</price></item>
                      </section>
                    </catalog>"""),
            new Submission("V08", HEAD + """
                    <catalog>
                      <section id="s1">
                        <heading>Identifiants</heading>
                        <item sku="a1"><name>Stylo</name><price>2</price></item>
                        <item sku="a1" see="zzz"><name>Gomme</name><price>1</price></item>
                        <item sku="9z"><name>Regle</name><price>3</price></item>
                      </section>
                    </catalog>"""),
            new Submission("V09", """
                    <?xml version="1.0"?>
                    <!DOCTYPE catalog SYSTEM "catalog.dtd" [
                      <!ATTLIST item status (new|old) "old">
                      <!ATTLIST item color CDATA #IMPLIED>
                    ]>
                    <catalog>
                      <section id="s1">
                        <heading>Interne</heading>
                        <item sku="a1" color="red"><name>Stylo</name><price>2</price></item>
                      </section>
                    </catalog>"""),
            new Submission("V10", """
                    <?xml version="1.0"?>
                    <!DOCTYPE catalog SYSTEM "catalog.dtd" [
                      <!ELEMENT note (#PCDATA)>
                    ]>
                    <catalog>
                      <section id="s1"><heading>Double</heading><note>simple</note></section>
                    </catalog>"""),
            new Submission("V11", HEAD + """
                    <section id="s1"><heading>Pas la bonne racine</heading></section>"""),
            new Submission("V12", HEAD + """
                    <catalog>
                      <section id="s1"><heading>Vide ?</heading><note>a<br>b</br></note></section>
                    </catalog>"""));
}
