package vorkurs02_xml.projects.p09_sax;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 9 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON SaxLab, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "TRACE start reading | start temp | texte [21.5] | end temp | start note | texte [Tom ] | texte [&] | texte [ Jerry] | end note | end reading",
            "RACINE avec namespaces : uri=[urn:sensors] local=[readings] qName=[m:readings] attributs=1 station=Lyon",
            "RACINE sans namespaces : uri=[] local=[] qName=[m:readings] attributs=2 xmlns:m=Lyon",
            "LECTURES 103 elements=213 profondeur=3",
            "MOYENNE s1 22.50 C sur 2",
            "MOYENNE s2 21.50 C sur 1",
            "MOYENNE s3 14.50 C sur 100",
            "NOTES [Tom & Jerry, capteur <nettoye>]",
            "ALERTES [Surchauffe salle B, Batterie faible]",
            "PREMIERE ALERTE Surchauffe salle B apres 11 elements",
            "ERREUR broken.xml : ligne 5 colonne 12",
            "ERREUR with-doctype.xml : ligne 2 colonne 10",
            "ERREUR readings.xml : aucune");
            // EXPECTED-END

    static final List<String> API = List.of(
            "SAXParserFactory.newInstance()", "setNamespaceAware(", "setFeature(", "newSAXParser()", "extends DefaultHandler",
            "startElement(", "characters(", "endElement(", "Attributes", "getValue(", "getLength()", "getQName(",
            "StringBuilder", "setLength(0)", "extends SAXException", "SAXParseException", "getLineNumber()", "getColumnNumber()",
            "!XmlKit", "!DocumentBuilder", "!XMLInputFactory", "!javax.xml.xpath", "!javax.xml.validation");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "SaxLab", args, EXPECTED, API);
    }
}
