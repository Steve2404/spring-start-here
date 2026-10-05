package vorkurs02_xml.drills.r05_xpath.solution;

import vorkurs02_xml.drills.Data;
import xmlkit.XmlKit;

import java.util.Map;

/**
 * Corrige du drill 5 : ecrire du XPath (0.2.16), sur campus.xml.
 */
public class Recall05 {

    static final String DOC = Data.campusText();
    // XPath ne lit pas les xmlns du document : on lie NOS prefixes aux URI.
    static final Map<String, String> NS = Map.of("c", "urn:campus", "g", "urn:campus:grades");

    static String v(String expr) {
        return XmlKit.value(DOC, expr, NS);
    }

    static String s(String expr) {
        return String.join(", ", XmlKit.select(DOC, expr, NS));
    }

    public static void main(String[] args) {
        System.out.println("D01 : " + s("//c:title/text()"));
        System.out.println("D02 : " + v("count(/c:campus/c:course[1]/c:student)"));
        // [2] porte sur le step : le 2e etudiant de CHAQUE cours.
        System.out.println("D03 : " + s("//c:course/c:student[2]/@ref"));
        // Les parentheses d'abord : le 2e de TOUT le document.
        System.out.println("D04 : " + v("(//c:student)[2]/@ref"));
        System.out.println("D05 : " + v("//c:course[not(c:student)]/@id"));
        // '//' apres le cours descend jusqu'aux notes de ses etudiants.
        System.out.println("D06 : " + v("sum(//c:course[@id='C1']//g:grade)"));
        System.out.println("D07 : " + v("sum(//g:grade) div count(//g:grade)"));
        // parent:: (ou ..) remonte d'un niveau.
        System.out.println("D08 : " + v("//c:student[@ref='S4']/parent::c:course/c:teacher"));
        System.out.println("D09 : " + v("//c:course[last()]/@id"));
        System.out.println("D10 : " + s("//c:person[not(@email)]/text()"));
        // Une jointure : la personne dont l'id est le ref de l'etudiant qui a la meilleure note.
        System.out.println("D11 : " + v("//c:person[@id = //c:student[not(g:grade < //g:grade)]/@ref]"));
        System.out.println("D12 : " + s("//c:course[@id='C1']/following-sibling::c:course/@id"));
        System.out.println("D13 : " + v("count((//g:grade)[1]/ancestor::*)"));
        System.out.println("D14 : " + v("substring-before(//c:person[@id='S1']/@email, '@')") + " "
                + v("concat(/c:campus/@name, ' (', /c:campus/@year, ')')"));
    }
}
