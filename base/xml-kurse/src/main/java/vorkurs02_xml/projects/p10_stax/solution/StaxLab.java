package vorkurs02_xml.projects.p10_stax.solution;

import vorkurs02_xml.projects.p10_stax.Data;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Corrige du projet 10 : le quai de chargement (0.2.19). StAX tire les evenements : c'est
 * l'application qui avance le curseur, decide de sauter un bloc ou de s'arreter.
 */
public class StaxLab {

    static final String NS = "urn:logistics";

    record Item(String sku, int qty, double kg) {
    }

    record Shipment(String id, String destination, int line, List<Item> items) {
        double totalKg() {
            return items.stream().mapToDouble(i -> i.qty() * i.kg()).sum();
        }
    }

    public static void main(String[] args) throws Exception {
        events();
        names();
        for (Shipment s : readAll()) {
            System.out.println("EXPEDITION " + s.id() + " " + s.destination() + " ligne " + s.line() + " : " + s.items().size()
                    + " article(s), " + String.format(Locale.ROOT, "%.2f", s.totalKg()) + " kg");
        }
        System.out.println("PLUS LOURDE QUE 5 kg : " + firstHeavierThan(5));
        System.out.println("PLUS LOURDE QUE 100 kg : " + firstHeavierThan(100));
        traps();
        destinations();
        dtd();
    }

    static XMLInputFactory factory(boolean dtd) {
        // On configure la FABRIQUE avant de creer un lecteur.
        XMLInputFactory f = XMLInputFactory.newFactory();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, dtd);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return f;
    }

    // ---------- Etape 1 : les etats du curseur ----------

    static void events() throws Exception {
        Map<String, Integer> counts = new TreeMap<>();
        List<String> first = new ArrayList<>();
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLStreamReader r = factory(false).createXMLStreamReader(in);
            // Le lecteur commence SUR START_DOCUMENT : next() avance PUIS rend le nouvel etat.
            first.add(name(r.getEventType()));
            while (r.hasNext()) {
                int e = r.next();
                counts.merge(name(e), 1, Integer::sum);
                if (first.size() < 6) {
                    // getLocalName n'est permis que sur START_ELEMENT / END_ELEMENT.
                    first.add(name(e) + (r.isStartElement() || r.isEndElement() ? ":" + r.getLocalName() : ""));
                }
            }
            r.close();
        }
        System.out.println("DEBUT " + String.join(" ", first));
        System.out.println("COMPTES " + counts);
    }

    static String name(int event) {
        return switch (event) {
            case XMLStreamConstants.START_DOCUMENT -> "START_DOCUMENT";
            case XMLStreamConstants.START_ELEMENT -> "START_ELEMENT";
            case XMLStreamConstants.END_ELEMENT -> "END_ELEMENT";
            case XMLStreamConstants.CHARACTERS -> "CHARACTERS";
            case XMLStreamConstants.COMMENT -> "COMMENT";
            case XMLStreamConstants.SPACE -> "SPACE";
            case XMLStreamConstants.DTD -> "DTD";
            case XMLStreamConstants.END_DOCUMENT -> "END_DOCUMENT";
            default -> "AUTRE";
        };
    }

    // ---------- Etape 2 : namespaces et attributs ----------

    static void names() throws Exception {
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLStreamReader r = factory(false).createXMLStreamReader(in);
            r.nextTag(); // la racine
            System.out.println("RACINE " + r.getName() + " prefixe=[" + r.getPrefix() + "] declarations=" + r.getNamespaceCount()
                    + " defaut=" + r.getNamespaceURI(0) + " attributs=" + r.getAttributeCount() + " date=" + r.getAttributeValue(null, "date"));
            r.close();
        }
    }

    // ---------- Etape 3 : lire, sauter ----------

    static List<Shipment> readAll() throws Exception {
        List<Shipment> result = new ArrayList<>();
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLStreamReader r = factory(false).createXMLStreamReader(in);
            r.nextTag(); // la racine
            // nextTag saute blancs et commentaires jusqu'au prochain START ou END d'element.
            while (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
                if (NS.equals(r.getNamespaceURI()) && r.getLocalName().equals("shipment")) {
                    result.add(readShipment(r));
                } else {
                    skip(r); // un bloc inconnu (audit) : on le traverse sans le lire
                }
            }
            r.close();
        }
        return result;
    }

    static Shipment readShipment(XMLStreamReader r) throws XMLStreamException {
        int line = r.getLocation().getLineNumber();
        String id = r.getAttributeValue(null, "id");
        String destination = r.getAttributeValue(null, "destination");
        List<Item> items = new ArrayList<>();
        while (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
            String sku = r.getAttributeValue(null, "sku");
            int qty = 0;
            double kg = 0;
            while (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
                // getElementText lit un element TEXTE et laisse le curseur sur son END_ELEMENT.
                if (r.getLocalName().equals("qty")) {
                    qty = Integer.parseInt(r.getElementText().strip());
                } else {
                    String unit = r.getAttributeValue(null, "unit");
                    double value = Double.parseDouble(r.getElementText().strip());
                    kg = "g".equals(unit) ? value / 1000 : value;
                }
            }
            items.add(new Item(sku, qty, kg));
        }
        return new Shipment(id, destination, line, items);
    }

    static void skip(XMLStreamReader r) throws XMLStreamException {
        // On compte la profondeur : a 0, on est sur le END qui ferme l'element de depart.
        int depth = 1;
        while (depth > 0) {
            int e = r.next();
            if (e == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (e == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    // ---------- Etape 4 : s'arreter des qu'on a la reponse ----------

    static String firstHeavierThan(double limit) throws Exception {
        // PULL : c'est nous qui decidons de ne plus avancer ; pas besoin d'exception comme en SAX.
        int read = 0;
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLStreamReader r = factory(false).createXMLStreamReader(in);
            try {
                while (r.hasNext()) {
                    if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("shipment")) {
                        Shipment s = readShipment(r);
                        read++;
                        if (s.totalKg() > limit) {
                            return s.id() + " (apres " + read + " expedition(s))";
                        }
                    }
                }
                return "aucune (" + read + " expeditions lues)";
            } finally {
                r.close();
            }
        }
    }

    // ---------- Etape 5 : les pieges ----------

    static void traps() throws Exception {
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLStreamReader r = factory(false).createXMLStreamReader(in);
            r.nextTag();
            String wrongState;
            try {
                r.next(); // un texte blanc apres la balise racine
                r.getLocalName();
                wrongState = "aucune";
            } catch (IllegalStateException e) {
                wrongState = e.getClass().getSimpleName();
            }
            // Avancer jusqu'a la premiere entry (contenu mixte : du texte ET un element <b>).
            while (!(r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("entry"))) {
                // on avance
            }
            String mixed;
            try {
                mixed = r.getElementText();
            } catch (XMLStreamException e) {
                mixed = e.getClass().getSimpleName();
            }
            System.out.println("PIEGES getLocalName sur du texte : " + wrongState + " | getElementText sur contenu mixte : " + mixed);
            r.close();
        }
    }

    // ---------- Etape 6 : l'API evenements ----------

    static void destinations() throws Exception {
        List<String> found = new ArrayList<>();
        String afterRoot;
        try (InputStream in = Files.newInputStream(Data.file("manifest.xml"))) {
            XMLEventReader events = factory(false).createXMLEventReader(in);
            events.nextEvent(); // StartDocument
            events.nextEvent(); // la racine
            // peek() regarde l'evenement SUIVANT sans le consommer.
            XMLEvent peeked = events.peek();
            afterRoot = peeked.isCharacters() && peeked.asCharacters().isWhiteSpace() ? "blanc" : "autre";
            while (events.hasNext()) {
                XMLEvent e = events.nextEvent();
                if (e.isStartElement()) {
                    StartElement start = e.asStartElement();
                    // Un nom d'element est un QName : (URI, nom local). L'attribut, lui, est sans namespace.
                    if (start.getName().equals(new QName(NS, "shipment"))) {
                        found.add(start.getAttributeByName(new QName("destination")).getValue());
                    }
                }
            }
            events.close();
        }
        System.out.println("EVENEMENTS apres la racine : " + afterRoot + " | destinations " + found);
    }

    // ---------- Etape 7 : DTD et entites ----------

    static void dtd() throws Exception {
        for (boolean support : new boolean[]{true, false}) {
            StringBuilder text = new StringBuilder();
            int pieces = 0;
            String verdict;
            try (InputStream in = Files.newInputStream(Data.file("with-entity.xml"))) {
                XMLStreamReader r = factory(support).createXMLStreamReader(in);
                while (r.hasNext()) {
                    if (r.next() == XMLStreamConstants.CHARACTERS) {
                        text.append(r.getText());
                        pieces++;
                    }
                }
                r.close();
                verdict = "[" + text + "] en " + pieces + " morceau(x)";
            } catch (XMLStreamException e) {
                // Sans DTD, l'entite x n'est pas declaree : la lecture echoue au lieu de l'inventer.
                verdict = e.getClass().getSimpleName();
            }
            System.out.println("DTD SUPPORT_DTD=" + support + " : " + verdict);
        }
    }
}
