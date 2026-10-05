package vorkurs02_xml.drills.r04_xsd;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 4 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall04, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : [] [cvc-datatype-valid.1.2.1, cvc-type.3.1.3]",
            "D02 : [] [cvc-pattern-valid, cvc-type.3.1.3]",
            "D03 : [] [cvc-maxExclusive-valid, cvc-type.3.1.3]",
            "D04 : [] [cvc-enumeration-valid, cvc-type.3.1.3]",
            "D05 : [] [cvc-complex-type.2.4.e]",
            "D06 : [] [cvc-complex-type.2.4.d]",
            "D07 : [] [cvc-complex-type.2.4.a]",
            "D08 : [cvc-complex-type.4] [cvc-complex-type.3.1]",
            "D09 : [] [cvc-datatype-valid.1.2.1, cvc-complex-type.2.2]",
            "D10 : [] [cvc-elt.1.a]",
            "D11 : [cvc-elt.1.a]");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Files.createTempFile(", "Files.writeString(", "XmlKit.validateXsd(", "xs:pattern", "xs:minInclusive",
            "xs:maxExclusive", "xs:enumeration", "xs:sequence", "maxOccurs", "xs:choice", "xs:all", "use=",
            "fixed=", "xs:simpleContent", "xs:extension", "targetNamespace", "elementFormDefault", "!javax.xml",
            "!org.w3c", "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall04", args, EXPECTED, API);
    }
}
