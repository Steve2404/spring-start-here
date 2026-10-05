package vorkurs02_xml.drills.r06_dom;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 6 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall06, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : #document campus Campus Lyon",
            "D02 : 3 5",
            "D03 : [C1, C2, C3]",
            "D04 : false [] true [closed]",
            "D05 : #text title",
            "D06 : 11 campus",
            "D07 : null Algorithmique Algorithmique",
            "D08 : 4 3 4",
            "D09 : Algo 2 people 0",
            "D10 : true true C3",
            "D11 : 4",
            "D12 : <course credits=\"6\" id=\"C3\" level=\"L2\" status=\"closed\" xmlns=\"urn:campus\"> <title>Reseaux</title> <teacher>Dupont</teacher> </course>");
            // EXPECTED-END

    static final List<String> API = List.of(
            "setNamespaceAware(true)", "getDocumentElement()", "getElementsByTagName(", "getElementsByTagNameNS(",
            "hasAttribute(", "getAttribute(", "getFirstChild()", "getNextSibling()", "getChildNodes()",
            "getParentNode()", "getNodeValue()", "getTextContent()", "createElement(", "createElementNS(",
            "appendChild(", "replaceChild(", "removeChild(", "cloneNode(true)", "importNode(", "newDocument()",
            "getAttributes()", "Transformer", "!XmlKit");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall06", args, EXPECTED, API);
    }
}
