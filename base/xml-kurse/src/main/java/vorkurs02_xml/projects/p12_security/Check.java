package vorkurs02_xml.projects.p12_security;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 12 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON SafeImport, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "RISQUES clean.xml : []",
            "RISQUES legacy-internal.xml : [DOCTYPE, INTERNAL_ENTITY]",
            "RISQUES external-dtd.xml : [DOCTYPE, EXTERNAL_DTD]",
            "RISQUES param-entity.xml : [DOCTYPE, EXTERNAL_ENTITY, PARAMETER_ENTITY]",
            "RISQUES lol-small.xml : [DOCTYPE, INTERNAL_ENTITY]",
            "RISQUES lol-big.xml : [DOCTYPE, INTERNAL_ENTITY]",
            "STRICT clean.xml : DOM accepte (2 item) | SAX accepte | STAX accepte",
            "STRICT legacy-internal.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX refuse",
            "STRICT external-dtd.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX accepte",
            "STRICT param-entity.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX accepte",
            "STRICT lol-small.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX refuse",
            "STRICT lol-big.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX refuse",
            "ANCIEN legacy-internal.xml : accepte, note de 20 caracteres : Client : Acme & Fils",
            "ANCIEN lol-small.xml : refuse (limite d'expansions)",
            "ANCIEN lol-big.xml : refuse (limite d'expansions)",
            "IMPORT clean.xml : ACCEPTE 2 item, note=RAS",
            "IMPORT legacy-internal.xml : ACCEPTE 3 item, note=Client : Acme & Fils",
            "IMPORT external-dtd.xml : REFUSE [DOCTYPE, EXTERNAL_DTD]",
            "IMPORT param-entity.xml : REFUSE [DOCTYPE, EXTERNAL_ENTITY, PARAMETER_ENTITY]",
            "IMPORT lol-small.xml : REFUSE au parsing [DOCTYPE, INTERNAL_ENTITY]",
            "IMPORT lol-big.xml : REFUSE au parsing [DOCTYPE, INTERNAL_ENTITY]",
            "SCHEMA main.xsd : refuse (SAXParseException)");
            // EXPECTED-END

    static final List<String> API = List.of(
            "enum ", "EnumSet", "Pattern", "XMLConstants.FEATURE_SECURE_PROCESSING", "disallow-doctype-decl",
            "external-general-entities", "external-parameter-entities", "XMLConstants.ACCESS_EXTERNAL_DTD",
            "XMLConstants.ACCESS_EXTERNAL_SCHEMA", "setXIncludeAware(false)", "setExpandEntityReferences(false)",
            "SUPPORT_DTD", "IS_SUPPORTING_EXTERNAL_ENTITIES", "load-external-dtd", "jdk.xml.entityExpansionLimit",
            "containsAll(", "SchemaFactory",
            "!XmlKit", "!getMessage()", "!javax.xml.xpath");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "SafeImport", args, EXPECTED, API);
    }
}
