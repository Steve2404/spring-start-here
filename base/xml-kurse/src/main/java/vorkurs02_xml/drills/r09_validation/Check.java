package vorkurs02_xml.drills.r09_validation;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 9 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall09, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : refuse ligne 1",
            "D02 : campus",
            "D03 : 10=refuse 100=50",
            "D04 : SAXParseException cvc-datatype-valid.1.2.1",
            "D05 : [cvc-datatype-valid.1.2.1, cvc-type.3.1.3, cvc-datatype-valid.1.2.1, cvc-type.3.1.3]",
            "D06 : 3.0 Bases de donnees true",
            "D07 : [S1, S3, S4]",
            "D08 : [18, 6, 0]",
            "D09 : false true");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XMLConstants.FEATURE_SECURE_PROCESSING", "disallow-doctype-decl", "XMLConstants.ACCESS_EXTERNAL_DTD",
            "XMLConstants.ACCESS_EXTERNAL_SCHEMA", "jdk.xml.entityExpansionLimit", "SchemaFactory", "newSchema(",
            "newValidator()", "setErrorHandler(", "ErrorHandler", "StreamSource", "XPathFactory",
            "NamespaceContext", "XPathConstants.NUMBER", "XPathConstants.BOOLEAN", "XPathConstants.NODESET",
            "compile(", "XPathExpression", "!XmlKit");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall09", args, EXPECTED, API);
    }
}
