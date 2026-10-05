package vorkurs02_xml.projects.p07_xpath;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 7 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Queries, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "Q1 \"Alice\", \"Bob\", \"Chloe\"",
            "Q2 \"Bob\", \"Eva\", \"  Gina   Lopez \"",
            "Q3 \"Bob\"",
            "Q4 24500",
            "Q5 \"Farid\", \"Hugo\"",
            "Q6 Dev",
            "Q7 Gina Lopez",
            "Q8 @employee=e2, @employee=e7",
            "Q9 0",
            "Q10 8 4",
            "Q11 \"Alice\", \"Dan\", \"Farid\", \"Hugo\"",
            "M01 6 : Alice | Bob | Chloe | Farid | Gina Lopez | Hugo | parseur identique",
            "M02 4 : Alice | Dan | Farid | Hugo | parseur identique",
            "M03 1 : Bob4000 | parseur identique",
            "M04 4 : Chloe4200 | Eva4800 | Gina Lopez 3900 | Hugo5200 | parseur identique",
            "M05 4 : Alice6000 | Dan5500 | Farid5000 | Hugo5200 | parseur identique",
            "M06 2 : Farid5000 | Gina Lopez 3900 | parseur identique",
            "M07 1 : Bob | parseur identique",
            "M08 1 : Alice6000 Bob4000 Chloe4200 Dan5500 Eva4800 | parseur identique",
            "M09 2 : Chloe | Eva | parseur identique",
            "M10 1 : 5200 | parseur identique",
            "M11 1 : Eva4800 | parseur identique",
            "M12 0 :  | parseur identique");
            // EXPECTED-END

    static final List<String> API = List.of(
            "XmlKit.select(", "XmlKit.value(", "sum(", "count(", "normalize-space(", "not(", "ancestor::",
            "Map.of(\"h\"", "LinkedHashSet", "record ",
            "!javax.xml", "!org.w3c", "!org.xml", "!XmlKit.validate", "!XmlKit.wellFormed");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Queries", args, EXPECTED, API);
    }
}
