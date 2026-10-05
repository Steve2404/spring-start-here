package vorkurs02_xml.projects.p09_sax.solution;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.projects.p09_sax.Data;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Corrige du projet 9 : la station de mesure en flux (0.2.18). SAX pousse des evenements ;
 * c'est le handler qui garde le contexte, accumule le texte et construit ses propres resultats.
 */
public class SaxLab {

    static final String NS = "urn:sensors";

    record Reading(String sensor, double celsius, String note) {
    }

    public static void main(String[] args) throws Exception {
        trace();
        names(true);
        names(false);
        readings();
        firstAlert();
        errors();
    }

    static SAXParser parser(boolean namespaceAware, boolean noDoctype) throws Exception {
        // Tout se configure sur la FABRIQUE, avant newSAXParser().
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        if (noDoctype) {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        }
        return factory.newSAXParser();
    }

    // ---------- Etape 1 : voir les evenements ----------

    static void trace() throws Exception {
        List<String> events = new ArrayList<>();
        DefaultHandler handler = new DefaultHandler() {
            boolean inFirst;
            boolean done;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (localName.equals("reading") && !done) {
                    inFirst = true;
                }
                if (inFirst) {
                    events.add("start " + localName);
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                // Un appel ne livre qu'un MORCEAU : ici, l'entite &amp; coupe le texte en trois.
                if (inFirst) {
                    events.add("texte [" + new String(ch, start, length) + "]");
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (inFirst) {
                    events.add("end " + localName);
                }
                if (localName.equals("reading")) {
                    inFirst = false;
                    done = true;
                }
            }
        };
        parser(true, true).parse(Data.file("readings.xml").toFile(), handler);
        System.out.println("TRACE " + String.join(" | ", events));
    }

    // ---------- Etape 2 : les noms et les attributs ----------

    static void names(boolean namespaceAware) throws Exception {
        String[] seen = new String[1];
        DefaultHandler handler = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (seen[0] == null) {
                    // Sans namespaces, uri et localName arrivent VIDES : seul qName est rempli.
                    seen[0] = "uri=[" + uri + "] local=[" + localName + "] qName=[" + qName + "] attributs=" + attributes.getLength()
                            + " " + attributes.getQName(0) + "=" + attributes.getValue("station");
                }
            }
        };
        parser(namespaceAware, true).parse(Data.file("readings.xml").toFile(), handler);
        System.out.println("RACINE " + (namespaceAware ? "avec" : "sans") + " namespaces : " + seen[0]);
    }

    // ---------- Etape 3 : agreger en flux ----------

    static class ReadingsHandler extends DefaultHandler {
        final List<Reading> readings = new ArrayList<>();
        final List<String> alerts = new ArrayList<>();
        int maxDepth;
        int elements;
        private final StringBuilder text = new StringBuilder();
        private int depth;
        private String sensor;
        private String unit;
        private double celsius;
        private String note;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // SAX ne garde rien : c'est a NOUS de memoriser le contexte et de vider le tampon.
            depth++;
            elements++;
            maxDepth = Math.max(maxDepth, depth);
            text.setLength(0);
            if (!NS.equals(uri)) {
                return;
            }
            switch (localName) {
                case "reading" -> {
                    sensor = attributes.getValue("sensor");
                    note = "";
                }
                // Un attribut sans prefixe n'a pas de namespace : getValue("unit") suffit.
                case "temp" -> unit = attributes.getValue("unit");
                default -> {
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            // On ACCUMULE : un texte peut arriver en plusieurs morceaux.
            text.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            // Le texte d'un element n'est complet qu'a SA fin.
            depth--;
            if (!NS.equals(uri)) {
                return;
            }
            switch (localName) {
                case "temp" -> {
                    double value = Double.parseDouble(text.toString().strip());
                    celsius = "F".equals(unit) ? (value - 32) * 5 / 9 : value;
                }
                case "note" -> note = text.toString();
                case "reading" -> readings.add(new Reading(sensor, celsius, note));
                case "msg" -> alerts.add(text.toString().strip());
                default -> {
                }
            }
        }
    }

    static void readings() throws Exception {
        // Le handler EST le resultat : apres parse(), on lit ses champs.
        ReadingsHandler h = new ReadingsHandler();
        parser(true, true).parse(Data.file("readings.xml").toFile(), h);
        System.out.println("LECTURES " + h.readings.size() + " elements=" + h.elements + " profondeur=" + h.maxDepth);
        Map<String, double[]> sums = new TreeMap<>();
        for (Reading r : h.readings) {
            double[] s = sums.computeIfAbsent(r.sensor(), k -> new double[2]);
            s[0] += r.celsius();
            s[1]++;
        }
        sums.forEach((sensor, s) -> System.out.println("MOYENNE " + sensor + " " + String.format(Locale.ROOT, "%.2f", s[0] / s[1]) + " C sur " + (int) s[1]));
        List<String> notes = h.readings.stream().map(Reading::note).filter(n -> !n.isEmpty()).toList();
        System.out.println("NOTES " + notes);
        System.out.println("ALERTES " + h.alerts);
    }

    // ---------- Etape 4 : s'arreter tot ----------

    /** Une exception de CONTROLE : le seul moyen d'arreter un parseur SAX qui pousse jusqu'au bout. */
    static class Stop extends SAXException {
        Stop() {
            super("arret volontaire");
        }
    }

    static void firstAlert() throws Exception {
        StringBuilder text = new StringBuilder();
        int[] seen = {0};
        String[] found = {null};
        DefaultHandler handler = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                seen[0]++;
                text.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (NS.equals(uri) && localName.equals("msg")) {
                    found[0] = text.toString().strip();
                    throw new Stop();
                }
            }
        };
        try {
            parser(true, true).parse(Data.file("readings.xml").toFile(), handler);
        } catch (Stop expected) {
            // arret voulu : found[0] est rempli
        }
        System.out.println("PREMIERE ALERTE " + found[0] + " apres " + seen[0] + " elements");
    }

    // ---------- Etape 5 : les erreurs ----------

    static void errors() throws Exception {
        for (String file : List.of("broken.xml", "with-doctype.xml", "readings.xml")) {
            try {
                parser(true, true).parse(Data.file(file).toFile(), new DefaultHandler());
                System.out.println("ERREUR " + file + " : aucune");
            } catch (SAXParseException e) {
                // getLineNumber / getColumnNumber : le point ou le parseur s'est APERCU du probleme.
                System.out.println("ERREUR " + file + " : ligne " + e.getLineNumber() + " colonne " + e.getColumnNumber());
            }
        }
    }
}
