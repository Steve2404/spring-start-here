package vorkurs02_xml.projects.p13_lab.solution;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.projects.p13_lab.Data;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Corrige du projet 13 : le laboratoire d'import (0.2.22). Une chaine de couches, chacune avec
 * l'outil qui lui convient, qui s'arrete a la premiere couche en echec.
 */
public class LabPipeline {

    static final String LAB = "urn:campus:lab";

    enum Phase { WELL_FORMED, NAMESPACE, SCHEMA, BUSINESS, DONE }

    /** Une violation de schema : la ligne et le code stable (le debut du message, avant ':'). */
    record Violation(int line, String code) {
        @Override
        public String toString() {
            return line + " " + code;
        }
    }

    record Result(String file, Phase phase, String detail) {
    }

    public static void main(String[] args) throws Exception {
        Schema schema = loadSchema();
        List<Result> results = new ArrayList<>();
        for (String f : Data.FILES) {
            Result r = importFile(Data.file(f), schema);
            results.add(r);
            System.out.println(r.file() + " : " + r.phase() + " " + r.detail());
        }
        long ok = results.stream().filter(r -> r.phase() == Phase.DONE).count();
        System.out.println("BILAN : " + ok + " importes sur " + results.size());

        for (Result r : results) {
            if (r.phase() == Phase.SCHEMA) {
                System.out.println("TOUTES " + r.file() + " : " + allViolations(Data.file(r.file()), schema));
            }
        }
        xpathTypes(loadDocument(Data.file("01-ok.xml")));
    }

    // ---------- La chaine ----------

    static Result importFile(Path xml, Schema schema) throws Exception {
        // Chaque couche suppose les precedentes reussies : on s'arrete a la premiere en echec.
        String name = xml.getFileName().toString();
        Integer fatal = wellFormedError(xml);
        if (fatal != null) {
            return new Result(name, Phase.WELL_FORMED, "l." + fatal);
        }
        Integer badRoot = namespaceError(xml);
        if (badRoot != null) {
            return new Result(name, Phase.NAMESPACE, "l." + badRoot);
        }
        Violation v = firstViolation(xml, schema);
        if (v != null) {
            return new Result(name, Phase.SCHEMA, "l." + v.line() + " " + v.code());
        }
        Document doc = loadDocument(xml);
        List<String> business = businessViolations(doc);
        if (!business.isEmpty()) {
            return new Result(name, Phase.BUSINESS, business.toString());
        }
        XPath x = xpath();
        int devices = ((Double) x.evaluate("count(//l:device)", doc, XPathConstants.NUMBER)).intValue();
        int quantity = ((Double) x.evaluate("sum(//l:device/l:quantity)", doc, XPathConstants.NUMBER)).intValue();
        return new Result(name, Phase.DONE, "devices=" + devices + " quantity=" + quantity);
    }

    static Integer wellFormedError(Path xml) throws Exception {
        // Couche 1 : un passage SAX securise, rapide, sans memoire ; le DOCTYPE est refuse ici.
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser parser = f.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            parser.parse(xml.toFile(), new DefaultHandler());
            return null;
        } catch (SAXParseException e) {
            return e.getLineNumber();
        }
    }

    static Integer namespaceError(Path xml) throws Exception {
        // Couche 2 : on ne lit QUE la racine. Un mauvais namespace donnerait sinon un cvc-elt.1.a cryptique.
        XMLInputFactory f = XMLInputFactory.newFactory();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try (InputStream in = Files.newInputStream(xml)) {
            XMLStreamReader r = f.createXMLStreamReader(in);
            try {
                r.nextTag();
                boolean ok = LAB.equals(r.getNamespaceURI()) && r.getLocalName().equals("order");
                return ok ? null : r.getLocation().getLineNumber();
            } finally {
                r.close();
            }
        }
    }

    static Schema loadSchema() throws SAXException {
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        // Un Schema compile est reutilisable : on le charge UNE fois pour tout le lot.
        return f.newSchema(Data.file("lab-order.xsd").toFile());
    }

    static String code(SAXParseException e) {
        // Le message est traduit, mais il commence toujours par le code stable : "cvc-...: ...".
        String m = e.getMessage();
        return m.substring(0, m.indexOf(':'));
    }

    static Violation firstViolation(Path xml, Schema schema) throws Exception {
        // Couche 3 : la PREMIERE violation suffit au rapport ; l'ErrorHandler la note puis relance pour s'arreter.
        Violation[] first = {null};
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                first[0] = new Violation(e.getLineNumber(), code(e));
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        try {
            validator.validate(new StreamSource(xml.toFile()));
        } catch (SAXParseException e) {
            // first[0] est rempli
        }
        return first[0];
    }

    static List<Violation> allViolations(Path xml, Schema schema) throws Exception {
        // Pour CORRIGER un fichier, on veut toutes les erreurs : l'ErrorHandler collecte sans relancer.
        List<Violation> all = new ArrayList<>();
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) {
                all.add(new Violation(e.getLineNumber(), code(e)));
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        validator.validate(new StreamSource(xml.toFile()));
        return all;
    }

    static Document loadDocument(Path xml) throws Exception {
        // Couche 4 : seulement maintenant, l'arbre DOM, sur un fichier deja sain et valide.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder b = f.newDocumentBuilder();
        b.setErrorHandler(new DefaultHandler());
        return b.parse(xml.toFile());
    }

    static XPath xpath() {
        // XPath ne lit pas les xmlns du document : c'est NOUS qui lions le prefixe l.
        XPath x = XPathFactory.newInstance().newXPath();
        x.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return "l".equals(prefix) ? LAB : XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String uri) {
                return LAB.equals(uri) ? "l" : null;
            }

            @Override
            public Iterator<String> getPrefixes(String uri) {
                return List.of("l").iterator();
            }
        });
        return x;
    }

    static List<String> businessViolations(Document doc) throws Exception {
        // Couche 5 : ce que le XSD ne sait pas dire.
        XPath x = xpath();
        List<String> out = new ArrayList<>();
        if ((Boolean) x.evaluate("sum(//l:device/l:quantity) > 50", doc, XPathConstants.BOOLEAN)) {
            out.add("QUANTITY_LIMIT");
        }
        if ((Boolean) x.evaluate("count(//l:device[@serial = preceding::l:device/@serial]) > 0", doc, XPathConstants.BOOLEAN)) {
            out.add("DUPLICATE_SERIAL");
        }
        // Des dates AAAA-MM-JJ se comparent comme des nombres une fois les tirets retires.
        if ((Boolean) x.evaluate("number(translate(/l:order/l:delivery, '-', '')) < number(translate(/l:order/@date, '-', ''))",
                doc, XPathConstants.BOOLEAN)) {
            out.add("DELIVERY_BEFORE_ORDER");
        }
        return out;
    }

    static void xpathTypes(Document doc) throws Exception {
        // Une expression, quatre types de retour : le type DEMANDE decide de la conversion.
        XPath x = xpath();
        NodeList serials = (NodeList) x.evaluate("//l:device/@serial", doc, XPathConstants.NODESET);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < serials.getLength(); i++) {
            values.add(serials.item(i).getNodeValue());
        }
        Double total = (Double) x.evaluate("sum(//l:quantity)", doc, XPathConstants.NUMBER);
        String first = x.evaluate("//l:device[1]/l:type", doc);
        Boolean late = (Boolean) x.evaluate("/l:order/l:delivery > '2026-09-05'", doc, XPathConstants.BOOLEAN);
        System.out.println("XPATH 01-ok.xml : NODESET " + values + " | NUMBER " + total + " | STRING " + first + " | BOOLEAN " + late);
    }
}
