package vorkurs02_xml.drills.r07_sax;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 7 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall07, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : 25",
            "D02 : urn:campus campus campus",
            "D03 : g:grade grade urn:campus:grades",
            "D04 : [] [] g:grade",
            "D05 : 3 id 6",
            "D06 : [Algorithmique, Bases de donnees, Reseaux]",
            "D07 : C1=12.17 C2=12.75",
            "D08 : 4",
            "D09 : Ben apres 23 elements",
            "D10 : 1:9");
            // EXPECTED-END

    static final List<String> API = List.of(
            "SAXParserFactory.newInstance()", "setNamespaceAware(", "newSAXParser()", "DefaultHandler",
            "startElement(", "characters(", "endElement(", "Attributes", "getLength()", "getQName(", "getValue(",
            "setLength(0)", "extends SAXException", "SAXParseException", "getColumnNumber()", "InputSource",
            "!XmlKit", "!DocumentBuilder", "!XMLInputFactory");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall07", args, EXPECTED, API);
    }
}
