package vorkurs02_xml.projects.p01_gate;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 1 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Gate, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 OK racine=catalogue elements=6 attributs=5 profondeur=3 | parseur OK",
            "D02 OK racine=personne elements=4 attributs=1 profondeur=2 | parseur OK",
            "D03 BAD_DECLARATION ligne 1 | parseur KO 1:23",
            "D04 MISPLACED_DECLARATION ligne 2 | parseur KO 2:6",
            "D05 BAD_NAME ligne 2 | parseur KO 2:4",
            "D06 BAD_NAME ligne 1 | parseur KO 1:12",
            "D07 MALFORMED_TAG ligne 1 | parseur KO 1:9",
            "D08 MALFORMED_TAG ligne 1 | parseur KO 1:6",
            "D09 MALFORMED_TAG ligne 2 | parseur KO 2:15",
            "D10 DUPLICATE_ATTRIBUTE ligne 1 | parseur KO 1:21",
            "D11 MISMATCHED_END_TAG ligne 2 | parseur KO 2:16",
            "D12 MISMATCHED_END_TAG ligne 1 | parseur KO 1:25",
            "D13 END_WITHOUT_START ligne 1 | parseur KO 1:10",
            "D14 UNCLOSED_ELEMENT ligne 1 | parseur KO 5:1",
            "D15 CONTENT_OUTSIDE_ROOT ligne 2 | parseur KO 2:1",
            "D16 MULTIPLE_ROOTS ligne 2 | parseur KO 2:2",
            "D17 NO_ROOT ligne 3 | parseur KO 3:1",
            "D18 OK racine=xmlConfig elements=2 attributs=1 profondeur=2 reserve=xmlConfig | parseur OK",
            "D19 BAD_DECLARATION ligne 1 | parseur KO 1:37",
            "D20 BAD_DECLARATION ligne 1 | parseur KO 1:23",
            "BILAN : 3 acceptes, 17 refuses, accord avec le parseur 20/20",
            "PLAN de D01 :",
            "  catalogue saison=ete",
            "    livre id=b1 langue=fr",
            "      titre : Dune",
            "      stock (vide)",
            "    livre id=b2 langue=en",
            "      titre : Emma",
            "RELATIONS de D01 :",
            "  catalogue : parent=- precedent=- suivant=- enfants=2",
            "  catalogue/livre[1] : parent=catalogue precedent=- suivant=livre enfants=2",
            "  catalogue/livre[1]/titre[1] : parent=livre precedent=- suivant=stock enfants=0",
            "  catalogue/livre[1]/stock[1] : parent=livre precedent=titre suivant=- enfants=0",
            "  catalogue/livre[2] : parent=catalogue precedent=livre suivant=- enfants=1",
            "  catalogue/livre[2]/titre[1] : parent=livre precedent=- suivant=- enfants=0",
            "CONVERSION <livre id=\"b1\" langue=\"fr\"><titre>Dune</titre><auteur>Herbert</auteur><annee>1965</annee></livre> | portier OK | parseur OK",
            "CONVERSION <livre id=\"b2\" langue=\"en\"><titre>Emma</titre><auteur>Austen</auteur><annee>1815</annee></livre> | portier OK | parseur OK",
            "CONVERSION <livre id=\"b3\" langue=\"fr\"><titre>Le Horla</titre><auteur>Maupassant</auteur><annee>1887</annee></livre> | portier OK | parseur OK");
            // EXPECTED-END

    static final List<String> API = List.of(
            "codePoints()", "Deque", ".push(", ".pop(", ".peek(", "record ", "enum ", "XmlKit.wellFormed(",
            "!Character.isWhitespace", "!equalsIgnoreCase", "!javax.xml", "!org.w3c", "!org.xml",
            "!XmlKit.wellFormedNs", "!XmlKit.value", "!XmlKit.select", "!XmlKit.validate");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Gate", args, EXPECTED, API);
    }
}
