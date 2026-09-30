package vorkurs02_xml.drills.exercises;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * DRILL 6 - Valider (javax.xml.validation), serialiser (Transformer), securiser
 * ===========================================================================
 *
 * Mode d'emploi : voir Drill01_DomApi. Donnees : NOTE_XSD (deja ecrit
 * plus bas) : un element <note> de 10 caracteres max, avec un attribut
 * lang (defaut "fr"), sans namespace ; et Campus.dom() pour le TODO 11.
 *
 *
 * -- Les TODO (methode visee entre crochets) --
 *
 * TODO 1  : schemaFactory()               [SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)]
 * TODO 2  : compileSchema(xsd)            [newSchema(new StreamSource(new StringReader(xsd)))]
 * TODO 3  : isValid(schema, xml)          [newValidator().validate ; SAXException -> false]
 * TODO 4  : errorCount(schema, xml)       [setErrorHandler qui compte error()] <note x='1'>abcdefghijkl</note> -> 3.
 * TODO 5  : parseWithSchema(schema, xml)  [DocumentBuilderFactory.setSchema] <note>hi</note> -> lang == "fr".
 * TODO 6  : secureDomFactory()            [FEATURE_SECURE_PROCESSING + disallow-doctype-decl]
 * TODO 7  : blockExternal(factory)        [setProperty(ACCESS_EXTERNAL_DTD / _SCHEMA, "")] rend la factory.
 *           (Verifie : getProperty(ACCESS_EXTERNAL_SCHEMA) leve SAXNotRecognizedException sur une
 *           SchemaFactory ; main() teste donc le COMPORTEMENT : fixtures/ex17/main.xsd et son xs:include.)
 * TODO 8  : toXml(doc, indent)            [Transformer, OMIT_XML_DECLARATION, INDENT] <a><b>x</b></a>.
 * TODO 9  : toXmlWithEncoding(doc, enc)   [OutputKeys.ENCODING] la declaration annonce ISO-8859-1.
 * TODO 10 : copyWithTransformer(doc)      [transform vers un DOMResult] nouveau Document, meme contenu.
 * TODO 11 : xmlDeclarationInfo(doc)       [getXmlVersion, getXmlEncoding, getXmlStandalone] campus -> "1.0|UTF-8|false".
 * TODO 12 : escapeViaDom(text)            [createTextNode + toXml] "a<b&c" -> "<t>a&lt;b&amp;c</t>".
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 *   Schema s = sf.newSchema(File | Source);   Validator v = s.newValidator();
 *   v.setErrorHandler(h); v.validate(new StreamSource(file | reader));
 *   DocumentBuilderFactory.setSchema(s) : valide pendant le parsing (+ defauts XSD)
 *   Securite : XMLConstants.FEATURE_SECURE_PROCESSING  ACCESS_EXTERNAL_DTD  ACCESS_EXTERNAL_SCHEMA
 *              "http://apache.org/xml/features/disallow-doctype-decl"
 *   Transformer t = TransformerFactory.newInstance().newTransformer();
 *   t.setOutputProperty(OutputKeys.INDENT | OMIT_XML_DECLARATION | ENCODING | METHOD, v);
 *   t.transform(new DOMSource(node), new StreamResult(writer) | new DOMResult());
 *   Document : getXmlVersion()  getXmlEncoding()  getXmlStandalone()
 * ---------------------------------------------------------------------
 */
public class Drill06_ValidationTransformApi {

    public static SchemaFactory schemaFactory() {
        throw new UnsupportedOperationException("TODO 1 : implementer schemaFactory()");
    }

    public static Schema compileSchema(String xsd) throws SAXException {
        throw new UnsupportedOperationException("TODO 2 : implementer compileSchema()");
    }

    public static boolean isValid(Schema schema, String xml) throws IOException {
        throw new UnsupportedOperationException("TODO 3 : implementer isValid()");
    }

    public static int errorCount(Schema schema, String xml) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer errorCount()");
    }

    public static Document parseWithSchema(Schema schema, String xml) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer parseWithSchema()");
    }

    public static DocumentBuilderFactory secureDomFactory() throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer secureDomFactory()");
    }

    public static SchemaFactory blockExternal(SchemaFactory factory) throws SAXException {
        throw new UnsupportedOperationException("TODO 7 : implementer blockExternal()");
    }

    public static String toXml(Document doc, boolean indent) throws Exception {
        throw new UnsupportedOperationException("TODO 8 : implementer toXml()");
    }

    public static String toXmlWithEncoding(Document doc, String encoding) throws Exception {
        throw new UnsupportedOperationException("TODO 9 : implementer toXmlWithEncoding()");
    }

    public static Document copyWithTransformer(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 10 : implementer copyWithTransformer()");
    }

    public static String xmlDeclarationInfo(Document doc) {
        throw new UnsupportedOperationException("TODO 11 : implementer xmlDeclarationInfo()");
    }

    public static String escapeViaDom(String text) throws Exception {
        throw new UnsupportedOperationException("TODO 12 : implementer escapeViaDom()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("TODO 1 : schemaFactory() gere W3C XML Schema",
                schemaFactory().isSchemaLanguageSupported(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        Schema schema = compileSchema(NOTE_XSD);
        ExerciseChecker.check("TODO 2 : compileSchema rend un Schema", schema != null);
        ExerciseChecker.check("TODO 3 : isValid(<note>ok</note>) && !isValid(<note>trop long texte</note>)",
                isValid(schema, "<note>ok</note>") && !isValid(schema, "<note>trop long texte</note>"));
        ExerciseChecker.check("TODO 4 : errorCount(<note x='1'>abcdefghijkl</note>) == 3",
                errorCount(schema, "<note x='1'>abcdefghijkl</note>") == 3);
        ExerciseChecker.check("TODO 5 : parseWithSchema -> lang par defaut == fr",
                parseWithSchema(schema, "<note>hi</note>").getDocumentElement().getAttribute("lang").equals("fr"));
        DocumentBuilderFactory secure = secureDomFactory();
        ExerciseChecker.check("TODO 6 : secureDomFactory refuse un DOCTYPE", refuses(secure, "<!DOCTYPE a><a/>") && !refuses(secure, "<a/>"));
        SchemaFactory blocked = blockExternal(schemaFactory());
        ExerciseChecker.check("TODO 7 : blockExternal -> un xs:include est refuse (la factory normale l'accepte)",
                compiles(schemaFactory(), "ex17/main.xsd") && !compiles(blocked, "ex17/main.xsd"));
        Document small = parse("<a><b>x</b></a>");
        ExerciseChecker.check("TODO 8 : toXml(indent=false) == <a><b>x</b></a>", toXml(small, false).equals("<a><b>x</b></a>"));
        ExerciseChecker.check("TODO 8 : toXml(indent=true) indente <b>", toXml(small, true).contains("\n    <b>x</b>"));
        ExerciseChecker.check("TODO 9 : toXmlWithEncoding(ISO-8859-1)",
                toXmlWithEncoding(small, "ISO-8859-1").startsWith("<?xml version=\"1.0\" encoding=\"ISO-8859-1\""));
        Document campus = Campus.dom();
        Document copy = copyWithTransformer(campus);
        ExerciseChecker.check("TODO 10 : copyWithTransformer -> autre Document, contenu egal",
                copy != campus && copy.getDocumentElement().isEqualNode(campus.getDocumentElement()));
        ExerciseChecker.check("TODO 11 : xmlDeclarationInfo(campus) == 1.0|UTF-8|false", xmlDeclarationInfo(campus).equals("1.0|UTF-8|false"));
        ExerciseChecker.check("TODO 12 : escapeViaDom(a<b&c) == <t>a&lt;b&amp;c</t>", escapeViaDom("a<b&c").equals("<t>a&lt;b&amp;c</t>"));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit (ne pas modifier)
    // ------------------------------------------------------------------

    static final String NOTE_XSD = """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="note">
                <xs:complexType>
                  <xs:simpleContent>
                    <xs:extension base="Short">
                      <xs:attribute name="lang" type="xs:string" default="fr"/>
                    </xs:extension>
                  </xs:simpleContent>
                </xs:complexType>
              </xs:element>
              <xs:simpleType name="Short">
                <xs:restriction base="xs:string"><xs:maxLength value="10"/></xs:restriction>
              </xs:simpleType>
            </xs:schema>
            """;

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    /** Verifie un COMPORTEMENT : getProperty(ACCESS_EXTERNAL_SCHEMA) leve SAXNotRecognizedException sur ce JDK. */
    private static boolean compiles(SchemaFactory factory, String fixture) {
        try {
            factory.newSchema(vorkurs02_xml.Fixtures.path(fixture).toFile());
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    private static boolean refuses(DocumentBuilderFactory factory, String xml) {
        try {
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
            builder.parse(new InputSource(new StringReader(xml)));
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
