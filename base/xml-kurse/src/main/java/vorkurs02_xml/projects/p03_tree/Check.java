package vorkurs02_xml.projects.p03_tree;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 3 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Tree, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "ARBRE :",
            "  DOCUMENT /",
            "    COMMENT <!-- commande du jour -->",
            "    PI <?routage file=A?>",
            "    ELEMENT commande @id=c1 @client=Lea",
            "      TEXT \"\\n  \"",
            "      ELEMENT ligne @ref=p1",
            "        ELEMENT produit",
            "          TEXT \"Stylo\"",
            "        ELEMENT quantite",
            "          TEXT \"3\"",
            "        ELEMENT prix",
            "          TEXT \"1.50\"",
            "      TEXT \"\\n  \"",
            "      COMMENT <!-- promo -->",
            "      TEXT \"\\n  \"",
            "      ELEMENT ligne @ref=p2",
            "        ELEMENT produit",
            "          TEXT \"Cahier & agenda\"",
            "        ELEMENT quantite",
            "          TEXT \"1\"",
            "        ELEMENT prix",
            "          TEXT \"4.20\"",
            "      TEXT \"\\n  \"",
            "      ELEMENT note",
            "        TEXT \"Livrer \"",
            "        ELEMENT b",
            "          TEXT \"avant\"",
            "        TEXT \" midi\"",
            "      TEXT \"\\n\"",
            "Q1 3 : <!-- commande du jour --> · <?routage file=A?> · commande | parseur identique",
            "Q2 9 : \"\\n  \" · ligne · \"\\n  \" · <!-- promo --> · \"\\n  \" · ligne · \"\\n  \" · note · \"\\n\" | parseur identique",
            "Q3 3 : \"Livrer \" · b · \" midi\" | parseur identique",
            "Q4 3 : / · commande · ligne | parseur identique",
            "Q5 7 : \"\\n  \" · <!-- promo --> · \"\\n  \" · ligne · \"\\n  \" · note · \"\\n\" | parseur identique",
            "Q6 14 : \"\\n  \" · \"Stylo\" · \"3\" · \"1.50\" · \"\\n  \" · \"\\n  \" · \"Cahier & agenda\" · \"1\" · \"4.20\" · \"\\n  \" · \"Livrer \" · \"avant\" · \" midi\" · \"\\n\" | parseur identique",
            "Q7 1 : @ref=p1 | parseur identique",
            "COMPTES commande : noeuds enfants=9 elements enfants=3 attributs=2",
            "O01 VALIDE | metier OK total=4.50",
            "O02 INVALIDE 1 (ligne 10) [VC]",
            "O03 NON_BIEN_FORME KO 11:3 [WFC]",
            "O04 VALIDE | metier KO ligne 1 quantite=-2",
            "O05 BIEN_FORME sans DTD | metier OK total=4.50",
            "O06 INVALIDE 3 (ligne 9) [VC]",
            "O07 VALIDE | metier OK total=4.00",
            "DEFAUT O01 devise : arbre maison absent | parseur EUR",
            "DEFAUT O07 devise : arbre maison CHF | parseur CHF");
            // EXPECTED-END

    static final List<String> API = List.of(
            "enum ", "XmlKit.select(", "XmlKit.wellFormed(", "XmlKit.validateDtd(", "BigDecimal", "RoundingMode.HALF_UP",
            "XmlKit.value(", "!javax.xml", "!org.w3c", "!org.xml", "!XmlKit.validateXsd", "!XmlKit.wellFormedNs");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Tree", args, EXPECTED, API);
    }
}
