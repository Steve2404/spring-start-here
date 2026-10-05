package vorkurs02_xml.projects.p10_stax;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 10 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON StaxLab, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "DEBUT START_DOCUMENT START_ELEMENT:manifest CHARACTERS COMMENT CHARACTERS START_ELEMENT:shipment",
            "COMPTES {CHARACTERS=27, COMMENT=2, END_DOCUMENT=1, END_ELEMENT=17, START_ELEMENT=17}",
            "RACINE {urn:logistics}manifest prefixe=[] declarations=1 defaut=urn:logistics attributs=1 date=2026-09-29",
            "EXPEDITION S1 Lyon ligne 4 : 2 article(s), 3.25 kg",
            "EXPEDITION S2 Paris ligne 12 : 1 article(s), 8.00 kg",
            "EXPEDITION S3 Nice ligne 19 : 0 article(s), 0.00 kg",
            "PLUS LOURDE QUE 5 kg : S2 (apres 2 expedition(s))",
            "PLUS LOURDE QUE 100 kg : aucune (3 expeditions lues)",
            "PIEGES getLocalName sur du texte : IllegalStateException | getElementText sur contenu mixte : XMLStreamException",
            "EVENEMENTS apres la racine : blanc | destinations [Lyon, Paris, Nice]",
            "DTD SUPPORT_DTD=true : [valeur secret] en 2 morceau(x)",
            "DTD SUPPORT_DTD=false : XMLStreamException");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XMLInputFactory.newFactory()", "SUPPORT_DTD", "IS_SUPPORTING_EXTERNAL_ENTITIES",
            "createXMLStreamReader(", "getEventType()", "hasNext()", "next()", "XMLStreamConstants", "close()",
            "getName()", "getPrefix()", "getNamespaceCount()", "getAttributeCount()", "getAttributeValue(null",
            "nextTag()", "getElementText()", "getLocation()", "IllegalStateException", "XMLStreamException",
            "createXMLEventReader(", "nextEvent()", "peek()", "StartElement", "getAttributeByName(", "QName",
            "!XmlKit", "!DocumentBuilder", "!SAXParser", "!javax.xml.xpath", "!javax.xml.validation");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "StaxLab", args, EXPECTED, API);
    }
}
