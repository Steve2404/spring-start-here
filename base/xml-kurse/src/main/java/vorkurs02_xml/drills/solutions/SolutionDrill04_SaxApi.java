package vorkurs02_xml.drills.solutions;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Corrige du drill 4. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill04_SaxApi.
 */
public class SolutionDrill04_SaxApi {

    public static final String GRADES = "urn:campus:grades";

    public static SAXParser newNamespaceAwareParser() throws Exception {
        // Sans setNamespaceAware(true), uri et localName arrivent VIDES dans startElement.
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newSAXParser();
    }

    public static int countElements(SAXParser parser, Path xml) throws Exception {
        int[] count = {0};
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                count[0]++;
            }
        });
        return count[0];
    }

    public static String firstGradeNames(SAXParser parser, Path xml) throws Exception {
        // Les 3 noms d'un element : qName (ecrit), localName (sans prefixe), uri (identite).
        StringBuilder out = new StringBuilder();
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (out.length() == 0 && GRADES.equals(uri)) {
                    out.append(qName).append('|').append(localName).append('|').append(uri);
                }
            }
        });
        return out.toString();
    }

    public static List<String> courseIds(SAXParser parser, Path xml) throws Exception {
        List<String> ids = new ArrayList<>();
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (localName.equals("course")) {
                    ids.add(attributes.getValue("id"));
                }
            }
        });
        return ids;
    }

    public static int rootAttributeCount(SAXParser parser, Path xml) throws Exception {
        // En mode namespace-aware, les declarations xmlns ne sont PAS des attributs pour SAX.
        int[] count = {-1};
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (count[0] < 0) {
                    count[0] = attributes.getLength();
                }
            }
        });
        return count[0];
    }

    public static List<String> attributeNames(SAXParser parser, Path xml, String courseId) throws Exception {
        // Parcours par index : getLength() puis getLocalName(i).
        List<String> names = new ArrayList<>();
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (localName.equals("course") && courseId.equals(attributes.getValue("id"))) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        names.add(attributes.getLocalName(i));
                    }
                }
            }
        });
        return names;
    }

    public static List<String> personNames(SAXParser parser, Path xml) throws Exception {
        // Accumuler dans characters, lire a endElement : un texte peut arriver en plusieurs morceaux.
        List<String> names = new ArrayList<>();
        parser.parse(xml.toFile(), new DefaultHandler() {
            private final StringBuilder text = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                text.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (localName.equals("person")) {
                    names.add(text.toString());
                }
            }
        });
        return names;
    }

    public static int lineOfFirstGrade(SAXParser parser, Path xml) throws Exception {
        // Le parseur donne son Locator AVANT le 1er evenement : on le garde pour demander la ligne.
        int[] line = {-1};
        parser.parse(xml.toFile(), new DefaultHandler() {
            private Locator locator;

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (line[0] < 0 && GRADES.equals(uri)) {
                    line[0] = locator.getLineNumber();
                }
            }
        });
        return line[0];
    }

    public static List<String> documentEvents(SAXParser parser, Path xml) throws Exception {
        List<String> events = new ArrayList<>();
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startDocument() {
                events.add("startDocument");
            }

            @Override
            public void endDocument() {
                events.add("endDocument");
            }
        });
        return events;
    }

    public static List<String> prefixMappings(SAXParser parser, Path xml) throws Exception {
        // Chaque xmlns est annonce par startPrefixMapping AVANT le startElement qui le porte.
        List<String> mappings = new ArrayList<>();
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void startPrefixMapping(String prefix, String uri) {
                mappings.add(prefix + "=" + uri);
            }
        });
        return mappings;
    }

    public static int ignorableWhitespaceCalls(SAXParser parser, Path xml) throws Exception {
        // Sans DTD, le parseur ne SAIT PAS quel blanc est ignorable : tout arrive dans characters().
        int[] calls = {0};
        parser.parse(xml.toFile(), new DefaultHandler() {
            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) {
                calls[0]++;
            }
        });
        return calls[0];
    }

    public static double averageGrade(SAXParser parser, Path xml) throws Exception {
        double[] sum = {0};
        int[] count = {0};
        parser.parse(xml.toFile(), new DefaultHandler() {
            private final StringBuilder text = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                text.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (GRADES.equals(uri) && localName.equals("grade")) {
                    sum[0] += Double.parseDouble(text.toString());
                    count[0]++;
                }
            }
        });
        return sum[0] / count[0];
    }

    /** Exception de controle pour interrompre le parseur. */
    public static class Stop extends SAXException {
        public Stop() {
            super("stop");
        }
    }

    public static List<String> firstElements(SAXParser parser, Path xml, int n) throws Exception {
        // SAX ne s'arrete jamais tout seul : on lance une exception a nous et on l'attrape.
        List<String> names = new ArrayList<>();
        try {
            parser.parse(xml.toFile(), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    names.add(localName);
                    if (names.size() == n) {
                        throw new Stop();
                    }
                }
            });
        } catch (Stop expected) {
            // arret volontaire
        }
        return names;
    }
}
