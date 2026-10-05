package vorkurs02_xml.drills.r05_xpath;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 5 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall05, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : \"Algorithmique\", \"Bases de donnees\", \"Reseaux\"",
            "D02 : 3",
            "D03 : @ref=S2, @ref=S4",
            "D04 : S2",
            "D05 : C3",
            "D06 : 36.5",
            "D07 : 12.4",
            "D08 : Martin",
            "D09 : C3",
            "D10 : \"Ben\"",
            "D11 : Ana",
            "D12 : @id=C2, @id=C3",
            "D13 : 3",
            "D14 : ana Campus Lyon (2026)");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Data.campusText()", "XmlKit.value(", "XmlKit.select(", "urn:campus:grades", "count(", "sum(", "div",
            "not(", "last()", "parent::", "following-sibling::", "ancestor::", "substring-before(", "concat(",
            "!javax.xml", "!org.w3c", "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall05", args, EXPECTED, API);
    }
}
