package vorkurs02_xml.drills.r02_dtd;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 2 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall02, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : VALID | INVALID 1 (ligne 1)",
            "D02 : OK | INVALID 2 (ligne 1)",
            "D03 : NOT_WELL_FORMED 1:80",
            "D04 : INVALID 1 (ligne 1) | VALID",
            "D05 : fr-2",
            "D06 : INVALID 1 (ligne 1) | INVALID 1 (ligne 1)",
            "D07 : VALID | INVALID 2 (ligne 1)",
            "D08 : VALID",
            "D09 : INVALID 1 (ligne 1)",
            "D10 : OK | INVALID 1 (ligne 1)",
            "D11 : VALID lang=de",
            "D12 : (?:(?:a;)(?:(?:b;)|(?:c;))*(?:d;)?)");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XmlKit.validateDtd(", "XmlKit.wellFormed(", "XmlKit.value(", "<!ELEMENT", "<!ATTLIST", "#REQUIRED",
            "#FIXED", "ID #REQUIRED", "IDREF", "#PCDATA", "EMPTY", "SYSTEM", "Pattern", "!javax.xml", "!org.w3c",
            "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall02", args, EXPECTED, API);
    }
}
