package vorkurs02_xml.projects.p13_lab;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 13 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON LabPipeline, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "01-ok.xml : DONE devices=2 quantity=22",
            "02-ok-prefixed.xml : DONE devices=1 quantity=1",
            "03-not-wellformed.xml : WELL_FORMED l.4",
            "04-doctype.xml : WELL_FORMED l.2",
            "05-wrong-namespace.xml : NAMESPACE l.2",
            "06-no-namespace.xml : NAMESPACE l.2",
            "07-schema-enum.xml : SCHEMA l.3 cvc-enumeration-valid",
            "08-schema-missing.xml : SCHEMA l.5 cvc-complex-type.2.4.b",
            "09-business-quantity.xml : BUSINESS [QUANTITY_LIMIT]",
            "10-business-multi.xml : BUSINESS [DUPLICATE_SERIAL, DELIVERY_BEFORE_ORDER]",
            "11-schema-many.xml : SCHEMA l.2 cvc-pattern-valid",
            "BILAN : 2 importes sur 11",
            "TOUTES 07-schema-enum.xml : [3 cvc-enumeration-valid, 3 cvc-type.3.1.3]",
            "TOUTES 08-schema-missing.xml : [5 cvc-complex-type.2.4.b]",
            "TOUTES 11-schema-many.xml : [2 cvc-pattern-valid, 2 cvc-attribute.3, 3 cvc-pattern-valid, 3 cvc-attribute.3, 3 cvc-minInclusive-valid, 3 cvc-type.3.1.3, 4 cvc-datatype-valid.1.2.1, 4 cvc-type.3.1.3]",
            "XPATH 01-ok.xml : NODESET [SN-100, SN-101] | NUMBER 22.0 | STRING microscope | BOOLEAN false");
            // EXPECTED-END

    static final List<String> API = List.of(
            "SAXParserFactory", "disallow-doctype-decl", "SAXParseException", "XMLInputFactory", "nextTag()", "getLocation()",
            "SchemaFactory", "newSchema(", "newValidator()", "setErrorHandler(", "ErrorHandler", "StreamSource",
            "DocumentBuilderFactory", "XMLConstants.FEATURE_SECURE_PROCESSING", "XMLConstants.ACCESS_EXTERNAL_SCHEMA",
            "XPathFactory", "NamespaceContext", "setNamespaceContext(", "XPathConstants.BOOLEAN", "translate(", "preceding::",
            "XPathConstants.NUMBER", "XPathConstants.NODESET", "NodeList", "enum ", "record ",
            "!XmlKit");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "LabPipeline", args, EXPECTED, API);
    }
}
