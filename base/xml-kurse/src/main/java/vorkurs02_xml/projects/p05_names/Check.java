package vorkurs02_xml.projects.p05_names;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 5 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Names, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "N1 {urn:shop:invoice}invoice @id @{http://www.w3.org/2001/XMLSchema-instance}schemaLocation @{http://www.w3.org/XML/1998/namespace}lang | parseur identique",
            "N2 {urn:shop:common}customer @{urn:shop:invoice}vip | parseur identique",
            "N3 {urn:shop:invoice}line @nr | parseur identique",
            "N4 {urn:shop:common}amount @{http://www.w3.org/2001/XMLSchema-instance}type | parseur identique",
            "N5 note | parseur identique",
            "N6 {urn:shop:tax}tax @rate | parseur identique",
            "N7 {urn:shop:money}total @{urn:shop:money}currency | parseur identique",
            "SCHEMAS urn:shop:invoice -> invoice.xsd ; urn:shop:money -> money.xsd",
            "XSI {urn:shop:common}amount : {urn:shop:money}Euro",
            "PORTEE note : inv=urn:shop:invoice money=urn:shop:money xml=http://www.w3.org/XML/1998/namespace xsi=http://www.w3.org/2001/XMLSchema-instance",
            "PORTEE {urn:shop:tax}tax : (defaut)=urn:shop:common inv=urn:shop:tax money=urn:shop:money xml=http://www.w3.org/XML/1998/namespace xsi=http://www.w3.org/2001/XMLSchema-instance",
            "B01 PREFIXE_NON_DECLARE a | XML 1.0 OK | namespaces KO 1:10",
            "B02 LIAISON_VIDE a | XML 1.0 OK | namespaces KO 2:21",
            "B03 PREFIXE_RESERVE xml | XML 1.0 OK | namespaces KO 1:34",
            "B04 PREFIXE_RESERVE xmlns | XML 1.0 OK | namespaces KO 1:27",
            "B05 ATTRIBUT_DOUBLE {urn:x}id | XML 1.0 OK | namespaces KO 1:55",
            "B06 QNAME_INVALIDE a:b:c | XML 1.0 OK | namespaces KO 1:5",
            "B07 OK | XML 1.0 OK | namespaces OK",
            "B08 OK | XML 1.0 OK | namespaces OK",
            "B09 OK | XML 1.0 OK | namespaces OK",
            "EGAL A B : oui (6 elements)",
            "EGAL A C : non, element 2 : {urn:shop:customer}customer {{urn:shop:customer}vip=true} \"Lea\" != {urn:shop:Customer}customer {{urn:shop:Customer}vip=true} \"Lea\"");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Deque", ".push(", ".pop(", "TreeMap", "XmlKit.value(", "XmlKit.wellFormed(", "XmlKit.wellFormedNs(", "record ",
            "!javax.xml", "!org.w3c", "!org.xml", "!XmlKit.validateXsd", "!XmlKit.select");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Names", args, EXPECTED, API);
    }
}
