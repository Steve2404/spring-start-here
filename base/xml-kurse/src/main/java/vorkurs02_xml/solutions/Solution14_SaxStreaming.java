package vorkurs02_xml.solutions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Corrige de l'exercice 14. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise14_SaxStreaming.
 */
public class Solution14_SaxStreaming {

    public static final String NS = "urn:sensors";

    public record Reading(String sensor, double celsius, String note) {
    }

    public record FirstAlert(Optional<String> message, int elementsSeen) {
    }

    public static SAXParser newParser() throws Exception {
        // Namespace-aware pour recevoir uri + localName ; secure processing et pas de DOCTYPE
        // (ce flux n'en a pas besoin) : configuration de la FACTORY avant newSAXParser().
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser parser = factory.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        return parser;
    }

    public static class ReadingsHandler extends DefaultHandler {
        public final List<Reading> readings = new ArrayList<>();
        public final List<String> alerts = new ArrayList<>();
        public int maxDepth;
        public int firstNoteChunks;

        private final StringBuilder text = new StringBuilder();
        private int depth;
        private int chunks;
        private String sensor;
        private String unit;
        private double celsius;
        private String note;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // SAX ne garde RIEN : c'est a nous de memoriser le contexte (capteur, unite) et de
            // vider le tampon de texte a chaque nouvel element.
            depth++;
            maxDepth = Math.max(maxDepth, depth);
            text.setLength(0);
            chunks = 0;
            if (!NS.equals(uri)) {
                return;
            }
            switch (localName) {
                case "reading" -> {
                    sensor = attributes.getValue("sensor");
                    note = "";
                }
                // Attribut sans prefixe : pas de namespace, getValue("unit") suffit.
                case "temp" -> unit = attributes.getValue("unit");
                default -> {
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            // Un element peut arriver en PLUSIEURS morceaux (une reference, une fin de tampon...) :
            // on ACCUMULE, on ne remplace jamais.
            text.append(ch, start, length);
            chunks++;
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            // C'est seulement a la FIN d'un element que son texte est complet.
            depth--;
            if (!NS.equals(uri)) {
                return;
            }
            switch (localName) {
                case "temp" -> {
                    double value = Double.parseDouble(text.toString().strip());
                    celsius = "F".equals(unit) ? (value - 32) * 5 / 9 : value;
                }
                case "note" -> {
                    note = text.toString();
                    if (readings.isEmpty() && firstNoteChunks == 0) {
                        firstNoteChunks = chunks;
                    }
                }
                case "reading" -> readings.add(new Reading(sensor, Math.round(celsius * 10) / 10.0, note));
                case "msg" -> alerts.add(text.toString().strip());
                default -> {
                }
            }
        }
    }

    public static ReadingsHandler parseReadings(Path xml) throws Exception {
        // Le handler EST le resultat : apres parse(), on lit ses champs.
        ReadingsHandler handler = new ReadingsHandler();
        newParser().parse(xml.toFile(), handler);
        return handler;
    }

    public static Map<String, Double> averageBySensor(List<Reading> readings) {
        // Agregation classique en memoire sur des objets deja petits : le streaming a fait son travail.
        return readings.stream().collect(Collectors.groupingBy(Reading::sensor, TreeMap::new,
                Collectors.collectingAndThen(Collectors.averagingDouble(Reading::celsius),
                        avg -> Math.round(avg * 100) / 100.0)));
    }

    /** Exception "de controle" : sert uniquement a arreter le parseur une fois la reponse trouvee. */
    public static class StopParsingException extends SAXException {
        public StopParsingException() {
            super("arret volontaire");
        }
    }

    public static FirstAlert firstAlert(Path xml) throws Exception {
        // SAX pousse les evenements jusqu'a la fin : le seul moyen de s'arreter tot est de LANCER
        // une exception depuis le handler, puis de l'attraper autour de parse().
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
                    throw new StopParsingException();
                }
            }
        };
        try {
            newParser().parse(xml.toFile(), handler);
        } catch (StopParsingException expected) {
            // arret volontaire : found[0] est rempli
        }
        return new FirstAlert(Optional.ofNullable(found[0]), seen[0]);
    }
}
