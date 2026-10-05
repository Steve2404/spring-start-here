package vorkurs02_xml.drills.r01_syntax;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 1 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall01, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : OK KO 1:23",
            "D02 : il dit \"oui\" & part",
            "D03 : a < b && c > d",
            "D04 : true",
            "D05 : x y x\\ty",
            "D06 : 3",
            "D07 : ACME SA",
            "D08 : AB",
            "D09 : [<!-- note - ok -->, <?trace on?>, r]",
            "D10 : true false true true false true",
            "D11 : x]]&gt;y &amp; &lt;z>",
            "D12 : DECLARATION DOCTYPE COMMENT START_TAG CDATA PI END_TAG");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XmlKit.wellFormed(", "XmlKit.value(", "XmlKit.select(", "CDATA", "&quot;", "&amp;", "&#9;",
            "<!ENTITY", "&#x", "codePointAt(", "!javax.xml", "!org.w3c", "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall01", args, EXPECTED, API);
    }
}
