package vorkurs02_xml.drills.r09_validation.solution;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.drills.Data;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Corrige du drill 9 : configuration sure, validation et XPath par l'API Java (0.2.21 -> 0.2.22).
 */
public class Recall09 {

    static DocumentBuilder secure() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder b = f.newDocumentBuilder();
        b.setErrorHandler(new DefaultHandler());
        return b;
    }

    static DocumentBuilder withLimit(int limit) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setAttribute("jdk.xml.entityExpansionLimit", String.valueOf(limit));
        DocumentBuilder b = f.newDocumentBuilder();
        b.setErrorHandler(new DefaultHandler());
        return b;
    }

    static String code(SAXParseException e) {
        // Le message est traduit ; son debut, le code, ne l'est pas.
        return e.getMessage().substring(0, e.getMessage().indexOf(':'));
    }

    public static void main(String[] args) throws Exception {
        // D01 : un DOCTYPE refuse d'emblee.
        try {
            secure().parse(new InputSource(new StringReader("<!DOCTYPE r [<!ENTITY e \"x\">]><r>&e;</r>")));
            System.out.println("D01 : accepte");
        } catch (SAXParseException e) {
            System.out.println("D01 : refuse ligne " + e.getLineNumber());
        }
        // D02 : le fichier du campus passe.
        Document campus = secure().parse(Data.campus().toFile());
        System.out.println("D02 : " + campus.getDocumentElement().getLocalName());

        // D03 : la limite d'expansions.
        String lol = "<!DOCTYPE r [<!ENTITY a \"ha\"><!ENTITY b \"&a;&a;&a;&a;&a;\"><!ENTITY c \"&b;&b;&b;&b;&b;\">]><r>&c;</r>";
        StringBuilder d03 = new StringBuilder();
        for (int limit : new int[]{10, 100}) {
            try {
                Document d = withLimit(limit).parse(new InputSource(new StringReader(lol)));
                d03.append(" ").append(limit).append("=").append(d.getDocumentElement().getTextContent().length());
            } catch (SAXParseException e) {
                d03.append(" ").append(limit).append("=refuse");
            }
        }
        System.out.println("D03 :" + d03);

        // D04 : un schema compile UNE fois, reutilisable.
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Schema schema = sf.newSchema(new StreamSource(new StringReader(
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"r\"><xs:complexType><xs:sequence>"
                        + "<xs:element name=\"n\" type=\"xs:int\" maxOccurs=\"3\"/></xs:sequence></xs:complexType></xs:element></xs:schema>")));
        String bad = "<r><n>x</n><n>2</n><n>y</n></r>";
        // Validator par defaut : la PREMIERE erreur leve une exception.
        try {
            schema.newValidator().validate(new StreamSource(new StringReader(bad)));
            System.out.println("D04 : valide");
        } catch (SAXParseException e) {
            System.out.println("D04 : " + e.getClass().getSimpleName() + " " + code(e));
        }
        // D05 : un ErrorHandler qui COLLECTE toutes les erreurs.
        List<String> all = new ArrayList<>();
        Validator v = schema.newValidator();
        v.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) {
                all.add(code(e));
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        v.validate(new StreamSource(new StringReader(bad)));
        System.out.println("D05 : " + all);

        // D06 a D09 : XPath par l'API, avec NOS prefixes.
        XPath x = XPathFactory.newInstance().newXPath();
        Map<String, String> ns = Map.of("c", "urn:campus", "g", "urn:campus:grades");
        x.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return ns.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String uri) {
                return null;
            }
        });
        Double n = (Double) x.evaluate("count(//c:course)", campus, XPathConstants.NUMBER);
        String title = x.evaluate("//c:course[@id='C2']/c:title", campus);
        Boolean closed = (Boolean) x.evaluate("boolean(//c:course[@status='closed'])", campus, XPathConstants.BOOLEAN);
        System.out.println("D06 : " + n + " " + title + " " + closed);
        NodeList withMail = (NodeList) x.evaluate("//c:person[@email]", campus, XPathConstants.NODESET);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < withMail.getLength(); i++) {
            ids.add(((Element) withMail.item(i)).getAttribute("id"));
        }
        System.out.println("D07 : " + ids);
        // D08 : une expression compilee, evaluee RELATIVEMENT a chaque cours.
        XPathExpression credits = x.compile("@credits * count(c:student)");
        NodeList courses = (NodeList) x.evaluate("//c:course", campus, XPathConstants.NODESET);
        List<String> weights = new ArrayList<>();
        for (int i = 0; i < courses.getLength(); i++) {
            weights.add(credits.evaluate(courses.item(i)));
        }
        System.out.println("D08 : " + weights);
        // D09 : en XPath 1.0, < et > convertissent en NOMBRES : deux textes non numeriques -> false.
        System.out.println("D09 : " + x.evaluate("'b' > 'a'", campus, XPathConstants.BOOLEAN) + " "
                + x.evaluate("'b' != 'a'", campus, XPathConstants.BOOLEAN));
    }
}
