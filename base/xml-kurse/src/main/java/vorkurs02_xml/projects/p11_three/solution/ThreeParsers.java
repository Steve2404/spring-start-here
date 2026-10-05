package vorkurs02_xml.projects.p11_three.solution;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.projects.p11_three.Data;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Corrige du projet 11 : trois parseurs, une tache (0.2.20). Le meme calcul en DOM, SAX et StAX,
 * puis ce qui les distingue vraiment : memoire, controle, navigation, arret, erreurs.
 */
public class ThreeParsers {

    enum Parser { DOM, SAX, STAX }

    record Totals(int count, long cents) {
        @Override
        public String toString() {
            return count + " livres, " + BigDecimal.valueOf(cents, 2) + " EUR";
        }
    }

    public static void main(String[] args) throws Exception {
        Totals dom = domTotals();
        Totals sax = saxTotals();
        Totals stax = staxTotals();
        System.out.println("TOTAL DOM " + dom);
        System.out.println("TOTAL SAX " + sax);
        System.out.println("TOTAL STAX " + stax);
        System.out.println("TOTAL identiques : " + (dom.equals(sax) && sax.equals(stax)));
        System.out.println("MEMOIRE DOM : " + countNodes(parseDom()) + " noeuds en memoire | SAX, StAX : un produit a la fois");
        System.out.println("ARRET STAX premier prix >= 99.00 : " + staxFirstAtLeast(9_900));
        System.out.println("TOP DOM 3 plus chers : " + domTopExpensive(3));
        errors();
        for (Data.Needs n : Data.SCENARIOS) {
            System.out.println("CHOIX " + n.name() + " -> " + choose(n));
        }
    }

    static long cents(String price) {
        // BigDecimal : "12.34" -> 1234 exactement ; un double accumulerait des erreurs d'arrondi.
        return new BigDecimal(price.strip()).movePointRight(2).longValueExact();
    }

    // ---------- DOM : tout en memoire, navigation libre ----------

    static Document parseDom() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Data.catalog().toFile());
    }

    static Totals domTotals() throws Exception {
        NodeList products = parseDom().getElementsByTagName("product");
        int count = 0;
        long sum = 0;
        for (int i = 0; i < products.getLength(); i++) {
            Element p = (Element) products.item(i);
            if (p.getAttribute("category").equals("book")) {
                count++;
                sum += cents(p.getElementsByTagName("price").item(0).getTextContent());
            }
        }
        return new Totals(count, sum);
    }

    static int countNodes(Node n) {
        // Chaque element et chaque texte (blancs compris) est un OBJET en memoire ; les attributs en plus, non comptes ici.
        int total = 1;
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
            total += countNodes(c);
        }
        return total;
    }

    static List<String> domTopExpensive(int n) throws Exception {
        // Trier TOUT le document demande de tout avoir sous la main : le terrain de DOM.
        NodeList products = parseDom().getElementsByTagName("product");
        List<Element> all = new ArrayList<>();
        for (int i = 0; i < products.getLength(); i++) {
            all.add((Element) products.item(i));
        }
        return all.stream()
                .sorted(Comparator.comparingLong((Element p) -> cents(p.getElementsByTagName("price").item(0).getTextContent()))
                        .reversed().thenComparing(p -> p.getAttribute("sku")))
                .limit(n).map(p -> p.getAttribute("sku")).toList();
    }

    // ---------- SAX : le parseur pousse, on garde le contexte ----------

    static Totals saxTotals() throws Exception {
        int[] count = {0};
        long[] sum = {0};
        DefaultHandler handler = new DefaultHandler() {
            private boolean book;
            private final StringBuilder text = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                text.setLength(0);
                if (qName.equals("product")) {
                    book = "book".equals(attributes.getValue("category"));
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (qName.equals("price") && book) {
                    count[0]++;
                    sum[0] += cents(text.toString());
                }
            }
        };
        SAXParserFactory.newInstance().newSAXParser().parse(Data.catalog().toFile(), handler);
        return new Totals(count[0], sum[0]);
    }

    // ---------- StAX : notre boucle tire ----------

    static Totals staxTotals() throws Exception {
        int count = 0;
        long sum = 0;
        try (InputStream in = Files.newInputStream(Data.catalog())) {
            XMLStreamReader r = XMLInputFactory.newFactory().createXMLStreamReader(in);
            boolean book = false;
            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT) {
                    if (r.getLocalName().equals("product")) {
                        book = "book".equals(r.getAttributeValue(null, "category"));
                    } else if (r.getLocalName().equals("price") && book) {
                        count++;
                        sum += cents(r.getElementText());
                    }
                }
            }
            r.close();
        }
        return new Totals(count, sum);
    }

    static String staxFirstAtLeast(long minCents) throws Exception {
        // Le vrai avantage du pull : s'arreter des qu'on a la reponse, sans exception de controle.
        try (InputStream in = Files.newInputStream(Data.catalog())) {
            XMLStreamReader r = XMLInputFactory.newFactory().createXMLStreamReader(in);
            try {
                int elements = 0;
                String sku = null;
                while (r.hasNext()) {
                    if (r.next() == XMLStreamConstants.START_ELEMENT) {
                        elements++;
                        if (r.getLocalName().equals("product")) {
                            sku = r.getAttributeValue(null, "sku");
                        } else if (r.getLocalName().equals("price") && cents(r.getElementText()) >= minCents) {
                            return sku + " apres " + elements + " elements";
                        }
                    }
                }
                return "aucun";
            } finally {
                r.close();
            }
        }
    }

    // ---------- Les erreurs : meme document, trois API ----------

    static void errors() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler()); // sinon DOM ecrit aussi "[Fatal Error]" sur la console
        try {
            builder.parse(new InputSource(new StringReader(Data.BROKEN)));
        } catch (SAXParseException e) {
            System.out.println("ERREUR DOM " + e.getClass().getSimpleName() + " ligne " + e.getLineNumber());
        }
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new StringReader(Data.BROKEN)), new DefaultHandler());
        } catch (SAXParseException e) {
            System.out.println("ERREUR SAX " + e.getClass().getSimpleName() + " ligne " + e.getLineNumber());
        }
        XMLStreamReader r = XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(Data.BROKEN));
        int read = 0;
        try {
            while (r.hasNext()) {
                r.next();
                read++;
            }
        } catch (XMLStreamException e) {
            // StAX a deja rendu les evenements d'avant l'erreur : l'application a pu agir dessus.
            System.out.println("ERREUR STAX " + e.getClass().getSimpleName() + " ligne " + e.getLocation().getLineNumber()
                    + " apres " + read + " evenements deja lus");
        }
    }

    // ---------- Choisir ----------

    static Parser choose(Data.Needs n) {
        // La table de decision, dans l'ordre de priorite.
        if (n.modify()) {
            return Parser.DOM;
        }
        if (n.randomAccess() && !n.hugeFile()) {
            return Parser.DOM;
        }
        if (n.earlyStop()) {
            return Parser.STAX;
        }
        return n.hugeFile() ? Parser.SAX : Parser.DOM;
    }
}
