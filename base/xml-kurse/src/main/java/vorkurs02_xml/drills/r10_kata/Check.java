package vorkurs02_xml.drills.r10_kata;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 10 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall10, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : <\"&'",
            "D02 : 5",
            "D03 : INVALID 1 (ligne 1)",
            "D04 : [1 cvc-minInclusive-valid, 1 cvc-type.3.1.3]",
            "D05 : Dupont",
            "D06 : 3",
            "D07 : 9",
            "D08 : Chloe",
            "D09 : refuse",
            "D10 : <bilan cours=\"3\" etudiants=\"3\"/>");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XmlKit.value(", "XmlKit.validateDtd(", "XmlKit.validateXsd(", "namespace-uri()",
            "following-sibling::", "getElementsByTagNameNS(", "removeChild(", "SAXParserFactory",
            "XMLInputFactory", "SUPPORT_DTD", "createElement(", "Transformer");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall10", args, EXPECTED, API);
    }
}
