package vorkurs02_xml.projects.p11_three;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 11 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON ThreeParsers, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "TOTAL DOM 10000 livres, 505690.00 EUR",
            "TOTAL SAX 10000 livres, 505690.00 EUR",
            "TOTAL STAX 10000 livres, 505690.00 EUR",
            "TOTAL identiques : true",
            "MEMOIRE DOM : 270003 noeuds en memoire | SAX, StAX : un produit a la fois",
            "ARRET STAX premier prix >= 99.00 : P00005 apres 16 elements",
            "TOP DOM 3 plus chers : [P07921, P17821, P27721]",
            "ERREUR DOM SAXParseException ligne 3",
            "ERREUR SAX SAXParseException ligne 3",
            "ERREUR STAX XMLStreamException ligne 3 apres 6 evenements deja lus",
            "CHOIX migration de configuration -> DOM",
            "CHOIX import nocturne de 40 Go -> SAX",
            "CHOIX premiere commande en erreur dans un flux -> STAX",
            "CHOIX rapport croise sur un petit fichier -> DOM",
            "CHOIX petit fichier lu une fois -> DOM",
            "CHOIX recherche avec navigation dans un gros fichier -> SAX");
            // EXPECTED-END

    static final List<String> API = List.of(
            "BigDecimal", "movePointRight(2)", "longValueExact()", "DocumentBuilderFactory", "getElementsByTagName(",
            "SAXParserFactory", "DefaultHandler", "XMLInputFactory", "getElementText()", "getFirstChild()", "getNextSibling()",
            "Comparator", "thenComparing(", "SAXParseException", "XMLStreamException", "getLocation()", "setErrorHandler(",
            "enum ", "!XmlKit", "!javax.xml.xpath", "!javax.xml.validation", "!double ");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "ThreeParsers", args, EXPECTED, API);
    }
}
