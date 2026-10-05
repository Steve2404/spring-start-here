package vorkurs02_xml.drills.r07_sax.solution;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.drills.Data;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Corrige du drill 7 : l'API SAX (0.2.18), sur campus.xml.
 */
public class Recall07 {

    static SAXParser parser(boolean ns) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(ns);
        return f.newSAXParser();
    }

    /** Arreter un parseur SAX : lancer une exception depuis le handler. */
    static class Stop extends SAXException {
    }

    public static void main(String[] args) throws Exception {
        // D01 a D05 : un seul passage, plusieurs observations.
        int[] count = {0};
        String[] root = {null};
        String[] grade = {null};
        String[] c1 = {null};
        parser(true).parse(Data.campus().toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) {
                count[0]++;
                if (root[0] == null) {
                    root[0] = uri + " " + localName + " " + qName;
                }
                if (grade[0] == null && localName.equals("grade")) {
                    grade[0] = qName + " " + localName + " " + uri;
                }
                if ("C1".equals(atts.getValue("id"))) {
                    c1[0] = atts.getLength() + " " + atts.getQName(0) + " " + atts.getValue("credits");
                }
            }
        });
        System.out.println("D01 : " + count[0]);
        System.out.println("D02 : " + root[0]);
        System.out.println("D03 : " + grade[0]);
        // D04 : sans namespaces, uri et localName arrivent VIDES.
        String[] raw = {null};
        parser(false).parse(Data.campus().toFile(), new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) {
                if (raw[0] == null && qName.equals("g:grade")) {
                    raw[0] = "[" + uri + "] [" + localName + "] " + qName;
                }
            }
        });
        System.out.println("D04 : " + raw[0]);
        System.out.println("D05 : " + c1[0]);

        // D06 a D08 : accumuler le texte, garder un etat, mesurer la profondeur.
        List<String> titles = new ArrayList<>();
        Map<String, double[]> grades = new TreeMap<>();
        int[] depth = {0, 0};
        parser(true).parse(Data.campus().toFile(), new DefaultHandler() {
            final StringBuilder text = new StringBuilder();
            String course;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) {
                text.setLength(0);
                depth[0]++;
                depth[1] = Math.max(depth[1], depth[0]);
                if (localName.equals("course")) {
                    course = atts.getValue("id");
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                // On ACCUMULE : un texte peut arriver en plusieurs morceaux.
                text.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                depth[0]--;
                if (localName.equals("title")) {
                    titles.add(text.toString());
                } else if (localName.equals("grade")) {
                    double[] s = grades.computeIfAbsent(course, k -> new double[2]);
                    s[0] += Double.parseDouble(text.toString());
                    s[1]++;
                }
            }
        });
        System.out.println("D06 : " + titles);
        StringBuilder avg = new StringBuilder();
        grades.forEach((k, s) -> avg.append(avg.length() == 0 ? "" : " ").append(k).append('=')
                .append(String.format(Locale.ROOT, "%.2f", s[0] / s[1])));
        System.out.println("D07 : " + avg);
        System.out.println("D08 : " + depth[1]);

        // D09 : s'arreter des la premiere personne sans email.
        int[] seen = {0};
        String[] found = {null};
        try {
            parser(true).parse(Data.campus().toFile(), new DefaultHandler() {
                final StringBuilder text = new StringBuilder();
                boolean target;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) {
                    seen[0]++;
                    text.setLength(0);
                    target = localName.equals("person") && atts.getValue("email") == null;
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    text.append(ch, start, length);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (target) {
                        found[0] = text.toString();
                        throw new Stop();
                    }
                }
            });
        } catch (Stop expected) {
            // arret voulu
        }
        System.out.println("D09 : " + found[0] + " apres " + seen[0] + " elements");

        // D10 : une erreur fatale : ou le parseur s'en est apercu.
        try {
            parser(true).parse(new InputSource(new StringReader("<a><b></a>")), new DefaultHandler());
        } catch (SAXParseException e) {
            System.out.println("D10 : " + e.getLineNumber() + ":" + e.getColumnNumber());
        }
    }
}
