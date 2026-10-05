package vorkurs02_xml.drills.r03_namespaces;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 3 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall03, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : urn:campus campus",
            "D02 : urn:campus:grades",
            "D03 : []",
            "D04 : [urn:x] []",
            "D05 : []",
            "D06 : urn:b",
            "D07 : true x:r r",
            "D08 : OK KO 1:7",
            "D09 : KO 1:25",
            "D10 : 3 0",
            "D11 : {urn:money}Euro {urn:d}Euro rate");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Data.campusText()", "XmlKit.value(", "namespace-uri(", "local-name(", "XmlKit.wellFormedNs(",
            "xmlns=\\\"\\\"", "Map.of(\"c\"", "!javax.xml", "!org.w3c", "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall03", args, EXPECTED, API);
    }
}
