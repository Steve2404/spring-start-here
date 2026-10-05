package vorkurs02_xml.drills.r08_stax;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 8 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall08, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : true campus",
            "D02 : 2 urn:campus:grades 2026",
            "D03 : 25",
            "D04 : [C1, C2, C3]",
            "D05 : [Algorithmique, Bases de donnees, Reseaux]",
            "D06 : 3",
            "D07 : 22",
            "D08 : IllegalStateException true",
            "D09 : commentaire [15.5, 9, 12, 18, 7.5]",
            "D10 : S2 en C1");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XMLInputFactory.newFactory()", "SUPPORT_DTD", "createXMLStreamReader(", "getEventType()", "nextTag()",
            "getNamespaceCount()", "getNamespaceURI(", "getAttributeValue(null", "hasNext()",
            "XMLStreamConstants.START_ELEMENT", "getElementText()", "getLocation()", "IllegalStateException",
            "isWhiteSpace()", "createXMLEventReader(", "peek()", "nextEvent()", "QName", "asCharacters()",
            "close()", "!XmlKit", "!DocumentBuilder", "!SAXParser");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall08", args, EXPECTED, API);
    }
}
