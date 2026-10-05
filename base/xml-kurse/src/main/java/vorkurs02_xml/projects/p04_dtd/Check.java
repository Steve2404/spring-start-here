package vorkurs02_xml.projects.p04_dtd;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 4 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON DtdCheck, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "MODELE catalog : (section+) -> [(?:(?:section;)+)]",
            "MODELE section : (heading, (item | bundle)*, note?, extra?) -> [(?:(?:heading;)(?:(?:item;)|(?:bundle;))*(?:note;)?(?:extra;)?)]",
            "MODELE heading : (#PCDATA) -> [(?:)]",
            "MODELE item : (name, price, (tag, tag?)?) -> [(?:(?:name;)(?:price;)(?:(?:tag;)(?:tag;)?)?)]",
            "MODELE bundle : (item, item+) -> [(?:(?:item;)(?:item;)+)]",
            "MODELE name : (#PCDATA) -> [(?:)]",
            "MODELE price : (#PCDATA) -> [(?:)]",
            "MODELE tag : (#PCDATA) -> [(?:)]",
            "MODELE note : (#PCDATA | b | i | br)* -> [(?:|(?:b;)|(?:i;)|(?:br;))*]",
            "MODELE b : (#PCDATA) -> [(?:)]",
            "MODELE i : (#PCDATA) -> [(?:)]",
            "MODELE br : EMPTY -> []",
            "MODELE extra : ANY -> [(?:[^;]+;)*]",
            "V01 VALIDE | parseur VALID | accord oui",
            "V02 INVALIDE 1 : bundle -> enfants | parseur INVALID 1 (ligne 6) | accord oui",
            "V03 INVALIDE 1 : item -> enfants | parseur INVALID 1 (ligne 6) | accord oui",
            "V04 INVALIDE 1 : section -> enfants | parseur INVALID 1 (ligne 8) | accord oui",
            "V05 INVALIDE 1 : item -> texte | parseur INVALID 1 (ligne 6) | accord oui",
            "V06 INVALIDE 2 : item -> enfants ; couleur -> non declare | parseur INVALID 2 (ligne 6) | accord oui",
            "V07 INVALIDE 4 : catalog@version=3 -> fixe 2 ; section@id -> manquant ; item@color -> non declare ; item@status=used -> hors liste | parseur INVALID 4 (ligne 3) | accord oui",
            "V08 INVALIDE 3 : item@sku=a1 -> ID double ; item@sku=9z -> ID invalide ; item@see=zzz -> IDREF sans cible | parseur INVALID 3 (ligne 7) | accord oui",
            "V09 VALIDE | parseur VALID | accord oui",
            "V10 INVALIDE 1 : note -> declare deux fois | parseur INVALID 1 (ligne 16) | accord oui",
            "V11 INVALIDE 1 : racine section != DOCTYPE catalog | parseur INVALID 1 (ligne 3) | accord oui",
            "V12 INVALIDE 1 : br -> texte | parseur INVALID 1 (ligne 4) | accord oui",
            "DEFAUTS V01 catalog : version=2 lang=fr | parseur : version=absent lang=absent",
            "DEFAUTS V09 item : sku=a1 color=red status=old | parseur : sku=a1 color=red status=old");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Pattern", "Matcher", "Pattern.matches(", "putIfAbsent(", "record ", "XmlKit.validateDtd(", "XmlKit.value(",
            "!javax.xml", "!org.w3c", "!org.xml", "!XmlKit.validateXsd", "!XmlKit.select", "!XmlKit.wellFormedNs");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "DtdCheck", args, EXPECTED, API);
    }
}
