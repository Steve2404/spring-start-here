package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Corrige de l'exercice 16. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise16_ThreeParsersOneTask.
 */
public class Solution16_ThreeParsersOneTask {

    public record Totals(int count, long cents) {
    }

    public record FirstMatch(String sku, int elementsRead) {
    }

    public enum Parser { DOM, SAX, STAX }

    public record Needs(boolean modify, boolean randomAccess, boolean hugeFile, boolean earlyStop) {
    }

    public static long cents(String price) {
        // BigDecimal : "12.34" -> 1234 exactement ; un double accumulerait des erreurs d'arrondi.
        return new BigDecimal(price.strip()).movePointRight(2).longValueExact();
    }

    public static Totals domTotals(Path xml) throws Exception {
        // DOM : tout l'arbre en memoire, puis on navigue librement. Simple, mais 30 000 produits
        // = des centaines de milliers de noeuds.
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
        NodeList products = doc.getElementsByTagName("product");
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

    public static Totals saxTotals(Path xml) throws Exception {
        // SAX : on retient la categorie au START de product, on accumule le texte de price,
        // on additionne au END de price. Memoire constante.
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
        SAXParserFactory.newInstance().newSAXParser().parse(xml.toFile(), handler);
        return new Totals(count[0], sum[0]);
    }

    public static Totals staxTotals(Path xml) throws Exception {
        // StAX : la meme logique, mais c'est NOTRE boucle qui avance ; getElementText lit le prix d'un coup.
        int count = 0;
        long sum = 0;
        try (InputStream in = Files.newInputStream(xml)) {
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

    public static FirstMatch staxFirstAtLeast(Path xml, long minCents) throws Exception {
        // Le vrai avantage du pull : on s'arrete des qu'on a la reponse, sans exception de controle.
        try (InputStream in = Files.newInputStream(xml)) {
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
                            return new FirstMatch(sku, elements);
                        }
                    }
                }
                return null;
            } finally {
                r.close();
            }
        }
    }

    public static List<String> domTopExpensive(Path xml, int n) throws Exception {
        // Trier TOUT le document par prix demande de tout avoir sous la main : c'est le terrain de DOM.
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
        NodeList products = doc.getElementsByTagName("product");
        List<Element> all = new ArrayList<>();
        for (int i = 0; i < products.getLength(); i++) {
            all.add((Element) products.item(i));
        }
        return all.stream()
                .sorted(Comparator.comparingLong((Element p) -> cents(p.getElementsByTagName("price").item(0).getTextContent()))
                        .reversed()
                        .thenComparing(p -> p.getAttribute("sku")))
                .limit(n)
                .map(p -> p.getAttribute("sku"))
                .toList();
    }

    public static Parser chooseParser(Needs needs) {
        // La table de decision du cours (0.2.20 S7), dans l'ordre de priorite.
        if (needs.modify()) {
            return Parser.DOM;
        }
        if (needs.randomAccess() && !needs.hugeFile()) {
            return Parser.DOM;
        }
        if (needs.earlyStop()) {
            return Parser.STAX;
        }
        return needs.hugeFile() ? Parser.SAX : Parser.DOM;
    }
}
