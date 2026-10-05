package vorkurs02_xml.drills.r02_dtd.solution;

import xmlkit.XmlKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du drill 2 : DTD, bien forme contre valide (0.2.9 -> 0.2.11).
 */
public class Recall02 {

    static String v(String doc) {
        return XmlKit.validateDtd(doc, Map.of());
    }

    public static void main(String[] args) {
        // D01 : une sequence impose l'ordre.
        String dtd = "<!DOCTYPE r [<!ELEMENT r (a, b?)><!ELEMENT a EMPTY><!ELEMENT b EMPTY>]>";
        System.out.println("D01 : " + v(dtd + "<r><a/></r>") + " | " + v(dtd + "<r><b/><a/></r>"));
        // D02 : bien forme sans DTD : la validite n'a pas de sens, un parseur validant le dit invalide.
        System.out.println("D02 : " + XmlKit.wellFormed("<r/>") + " | " + v("<r/>"));
        // D03 : mal forme : la DTD ne peut rien sauver.
        System.out.println("D03 : " + v(dtd + "<r><a></r>"));
        // D04 : #REQUIRED manquant.
        String att = "<!DOCTYPE r [<!ELEMENT r EMPTY><!ATTLIST r id CDATA #REQUIRED lang (fr|de) \"fr\" v CDATA #FIXED \"2\">]>";
        System.out.println("D04 : " + v(att + "<r/>") + " | " + v(att + "<r id=\"x\"/>"));
        // D05 : les valeurs par defaut et fixees sont AJOUTEES par le parseur (DTD interne).
        System.out.println("D05 : " + XmlKit.value(att + "<r id=\"x\"/>", "concat(/r/@lang, '-', /r/@v)"));
        // D06 : hors liste, ou #FIXED different.
        System.out.println("D06 : " + v(att + "<r id=\"x\" lang=\"en\"/>") + " | " + v(att + "<r id=\"x\" v=\"3\"/>"));
        // D07 : ID unique dans TOUT le document ; IDREF doit viser un ID existant.
        String ids = "<!DOCTYPE r [<!ELEMENT r (p*)><!ELEMENT p EMPTY><!ATTLIST p id ID #REQUIRED ref IDREF #IMPLIED>]>";
        System.out.println("D07 : " + v(ids + "<r><p id=\"a\"/><p id=\"b\" ref=\"a\"/></r>") + " | "
                + v(ids + "<r><p id=\"a\"/><p id=\"a\" ref=\"z\"/></r>"));
        // D08 : un modele mixte s'ecrit (#PCDATA | b)* : texte et b dans n'importe quel ordre.
        String mixed = "<!DOCTYPE r [<!ELEMENT r (#PCDATA | b)*><!ELEMENT b (#PCDATA)>]>";
        System.out.println("D08 : " + v(mixed + "<r>x<b>y</b>z<b/></r>"));
        // D09 : EMPTY n'accepte RIEN, pas meme un blanc.
        System.out.println("D09 : " + v(dtd + "<r><a> </a></r>"));
        // D10 : le nom du DOCTYPE doit etre celui de la racine : une regle de VALIDITE.
        String other = "<!DOCTYPE x [<!ELEMENT r EMPTY>]><r/>";
        System.out.println("D10 : " + XmlKit.wellFormed(other) + " | " + v(other));
        // D11 : une DTD externe, plus un sous-ensemble interne qui la complete (le premier ATTLIST gagne).
        String ext = "<!ELEMENT r EMPTY><!ATTLIST r lang (fr|de) \"fr\">";
        String both = "<!DOCTYPE r SYSTEM \"r.dtd\" [<!ATTLIST r lang (fr|de) \"de\">]><r/>";
        System.out.println("D11 : " + XmlKit.validateDtd(both, Map.of("r.dtd", ext)) + " lang=" + XmlKit.value(both, "string(/r/@lang)"));
        // D12 : un modele de contenu traduit en regex sur "nom;nom;".
        System.out.println("D12 : " + toRegex("(a, (b | c)*, d?)"));
    }

    static String toRegex(String model) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("[(),|?*+]|[^\\s(),|?*+]+").matcher(model);
        while (m.find()) {
            tokens.add(m.group());
        }
        return particle(tokens, new int[1]);
    }

    private static String particle(List<String> t, int[] pos) {
        String token = t.get(pos[0]++);
        String base;
        if (token.equals("(")) {
            List<String> parts = new ArrayList<>();
            parts.add(particle(t, pos));
            String sep = "";
            while (!t.get(pos[0]).equals(")")) {
                sep = t.get(pos[0]++);
                parts.add(particle(t, pos));
            }
            pos[0]++;
            base = "(?:" + String.join(sep.equals("|") ? "|" : "", parts) + ")";
        } else {
            base = token.equals("#PCDATA") ? "" : "(?:" + token + ";)";
        }
        if (pos[0] < t.size() && "?*+".contains(t.get(pos[0]))) {
            base += t.get(pos[0]++);
        }
        return base;
    }
}
