package vorkurs02_xml.drills.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Corrige du drill 6. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill06_ValidationTransformApi.
 */
public class SolutionDrill06_ValidationTransformApi {

    public static SchemaFactory schemaFactory() {
        // Le "langage" de schema se choisit par son URI : ici W3C XML Schema (XSD).
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    public static Schema compileSchema(String xsd) throws SAXException {
        return schemaFactory().newSchema(new StreamSource(new StringReader(xsd)));
    }

    public static boolean isValid(Schema schema, String xml) throws IOException {
        // Sans ErrorHandler, le Validator LANCE une SAXException a la 1re erreur.
        try {
            schema.newValidator().validate(new StreamSource(new StringReader(xml)));
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    public static int errorCount(Schema schema, String xml) throws Exception {
        // Avec un ErrorHandler qui ne relance pas, error() est appele pour CHAQUE violation.
        int[] count = {0};
        Validator v = schema.newValidator();
        v.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) {
                count[0]++;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        v.validate(new StreamSource(new StringReader(xml)));
        return count[0];
    }

    public static Document parseWithSchema(Schema schema, String xml) throws Exception {
        // setSchema sur la factory : validation PENDANT le parsing, et les defauts XSD entrent dans le DOM.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setSchema(schema);
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    public static DocumentBuilderFactory secureDomFactory() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return f;
    }

    public static SchemaFactory blockExternal(SchemaFactory factory) throws SAXException {
        // "" = liste vide de protocoles autorises : ni file, ni http.
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    public static String toXml(Document doc, boolean indent) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }

    public static String toXmlWithEncoding(Document doc, String encoding) throws Exception {
        // ENCODING change la declaration ET l'encodage des octets (ici dans un Writer, seulement la declaration).
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, encoding);
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }

    public static Document copyWithTransformer(Document doc) throws Exception {
        // Transformation identite vers un DOMResult : une copie complete dans un NOUVEAU Document.
        DOMResult result = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), result);
        return (Document) result.getNode();
    }

    public static String xmlDeclarationInfo(Document doc) {
        // Ce que la declaration <?xml ...?> du fichier annoncait (lu par le parseur).
        return doc.getXmlVersion() + "|" + doc.getXmlEncoding() + "|" + doc.getXmlStandalone();
    }

    public static String escapeViaDom(String text) throws Exception {
        // On ne concatene jamais du XML a la main : le serialiseur echappe < et & tout seul.
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element t = doc.createElement("t");
        t.appendChild(doc.createTextNode(text));
        doc.appendChild(t);
        return toXml(doc, false);
    }
}
