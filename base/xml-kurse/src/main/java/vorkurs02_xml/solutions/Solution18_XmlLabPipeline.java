package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Corrige de l'exercice 18. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise18_XmlLabPipeline.
 */
public class Solution18_XmlLabPipeline {

    public static final String LAB = "urn:campus:lab";

    public enum Phase { WELL_FORMED, NAMESPACE, SCHEMA, BUSINESS, DONE }

    public record Violation(int line, String code) {
    }

    public record ImportResult(String file, Phase phase, boolean ok, int line, List<String> details) {
    }

    public static Optional<Integer> checkWellFormed(Path xml) throws Exception {
        // Couche 1 : un passage SAX securise, rapide et sans memoire. Un DOCTYPE est refuse ici,
        // avant qu'aucune couche suivante ne puisse etre piegee.
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser parser = f.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            parser.parse(xml.toFile(), new DefaultHandler());
            return Optional.empty();
        } catch (SAXParseException e) {
            return Optional.of(e.getLineNumber());
        }
    }

    public static Optional<Integer> checkNamespace(Path xml) throws Exception {
        // Couche 2 : on ne lit QUE la racine (pull + arret immediat). Un mauvais namespace donnerait
        // sinon une erreur XSD cryptique (cvc-elt.1.a) au lieu d'un diagnostic clair.
        XMLInputFactory f = XMLInputFactory.newFactory();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        try (InputStream in = Files.newInputStream(xml)) {
            XMLStreamReader r = f.createXMLStreamReader(in);
            try {
                r.nextTag();
                boolean ok = LAB.equals(r.getNamespaceURI()) && r.getLocalName().equals("order");
                return ok ? Optional.empty() : Optional.of(r.getLocation().getLineNumber());
            } finally {
                r.close();
            }
        }
    }

    public static Optional<Violation> schemaViolation(Path xml, Schema schema) throws Exception {
        // Couche 3 : on s'arrete a la PREMIERE violation (le rapport ne veut que la cause principale) :
        // l'ErrorHandler la memorise puis relance pour interrompre la validation.
        Violation[] first = {null};
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) throws SAXException {
                String m = e.getMessage();
                first[0] = new Violation(e.getLineNumber(), m.substring(0, m.indexOf(':')));
                throw e;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        try {
            validator.validate(new StreamSource(xml.toFile()));
        } catch (SAXParseException e) {
            // first[0] est rempli
        }
        return Optional.ofNullable(first[0]);
    }

    public static Document loadDocument(Path xml) throws Exception {
        // Couche 4 : seulement maintenant on construit le DOM, sur un fichier DEJA sain et valide.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = f.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler());
        return builder.parse(xml.toFile());
    }

    private static XPath labXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return "l".equals(prefix) ? LAB : XMLConstants.NULL_NS_URI;
            }

            public String getPrefix(String uri) {
                return LAB.equals(uri) ? "l" : null;
            }

            public Iterator<String> getPrefixes(String uri) {
                return List.of("l").iterator();
            }
        });
        return xpath;
    }

    public static List<String> businessViolations(Document doc) throws Exception {
        // Couche 5 : ce que le XSD ne sait pas exprimer. XPath 1.0 : sum() pour la quantite, un
        // predicat sur preceding:: pour les doublons, translate() pour comparer des dates AAAA-MM-JJ.
        XPath x = labXPath();
        List<String> violations = new ArrayList<>();
        if ((Boolean) x.evaluate("sum(//l:device/l:quantity) > 50", doc, XPathConstants.BOOLEAN)) {
            violations.add("QUANTITY_LIMIT");
        }
        if ((Boolean) x.evaluate("count(//l:device[@serial = preceding::l:device/@serial]) > 0", doc, XPathConstants.BOOLEAN)) {
            violations.add("DUPLICATE_SERIAL");
        }
        if ((Boolean) x.evaluate("number(translate(/l:order/l:delivery, '-', '')) < number(translate(/l:order/@date, '-', ''))",
                doc, XPathConstants.BOOLEAN)) {
            violations.add("DELIVERY_BEFORE_ORDER");
        }
        return violations;
    }

    public static ImportResult importFile(Path xml, Schema schema) throws Exception {
        // La chaine s'arrete a la PREMIERE couche en echec : chaque couche suppose les precedentes reussies.
        String name = xml.getFileName().toString();
        Optional<Integer> fatal = checkWellFormed(xml);
        if (fatal.isPresent()) {
            return new ImportResult(name, Phase.WELL_FORMED, false, fatal.get(), List.of());
        }
        Optional<Integer> badRoot = checkNamespace(xml);
        if (badRoot.isPresent()) {
            return new ImportResult(name, Phase.NAMESPACE, false, badRoot.get(), List.of());
        }
        Optional<Violation> invalid = schemaViolation(xml, schema);
        if (invalid.isPresent()) {
            return new ImportResult(name, Phase.SCHEMA, false, invalid.get().line(), List.of(invalid.get().code()));
        }
        Document doc = loadDocument(xml);
        List<String> business = businessViolations(doc);
        if (!business.isEmpty()) {
            return new ImportResult(name, Phase.BUSINESS, false, 0, business);
        }
        XPath x = labXPath();
        int devices = ((Double) x.evaluate("count(//l:device)", doc, XPathConstants.NUMBER)).intValue();
        int quantity = ((Double) x.evaluate("sum(//l:device/l:quantity)", doc, XPathConstants.NUMBER)).intValue();
        return new ImportResult(name, Phase.DONE, true, 0, List.of("devices=" + devices, "quantity=" + quantity));
    }

    public static String report(List<ImportResult> results) {
        // Une ligne par fichier ; la forme depend de la couche atteinte.
        return results.stream().map(r -> switch (r.phase()) {
            case DONE -> r.file() + " : OK " + r.details();
            case WELL_FORMED, NAMESPACE -> r.file() + " : " + r.phase() + " l." + r.line();
            case SCHEMA -> r.file() + " : SCHEMA l." + r.line() + " " + r.details().get(0);
            case BUSINESS -> r.file() + " : BUSINESS " + r.details();
        }).collect(Collectors.joining("\n"));
    }
}
