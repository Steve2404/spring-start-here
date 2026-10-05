package vorkurs02_xml.drills.r01_syntax.solution;

import xmlkit.XmlKit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Corrige du drill 1 : la syntaxe XML (0.2.2 -> 0.2.8). On ECRIT du XML, et le parseur dit ce qu'il relit.
 */
public class Recall01 {

    public static void main(String[] args) {
        // D01 : la declaration complete, dans le seul ordre permis.
        String d01 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><r/>";
        System.out.println("D01 : " + XmlKit.wellFormed(d01) + " " + XmlKit.wellFormed("<?xml standalone=\"yes\" version=\"1.0\"?><r/>"));
        // D02 : entre guillemets doubles, seul " doit devenir &quot; ; & devient toujours &amp;.
        String d02 = "<r t=\"il dit &quot;oui&quot; &amp; part\"/>";
        System.out.println("D02 : " + XmlKit.value(d02, "string(/r/@t)"));
        // D03 : < devient &lt; ; > peut rester brut.
        String d03 = "<c>a &lt; b &amp;&amp; c > d</c>";
        System.out.println("D03 : " + XmlKit.value(d03, "string(/c)"));
        // D04 : en CDATA, rien n'est reconnu : le meme texte, ecrit brut.
        String d04 = "<c><![CDATA[a < b && c > d]]></c>";
        System.out.println("D04 : " + XmlKit.value(d04, "string(/c)").equals(XmlKit.value(d03, "string(/c)")));
        // D05 : une tabulation LITTERALE dans un attribut devient un espace ; &#9; la garde.
        String d05 = "<r a=\"x\ty\" b=\"x&#9;y\"/>";
        System.out.println("D05 : " + show(XmlKit.value(d05, "string(/r/@a)")) + " " + show(XmlKit.value(d05, "string(/r/@b)")));
        // D06 : CR LF devient un seul LF a la lecture.
        System.out.println("D06 : " + XmlKit.value("<t>a\r\nb</t>", "string-length(/t)"));
        // D07 : une entite interne, utilisee dans le texte.
        String d07 = "<!DOCTYPE r [<!ENTITY nom \"ACME\">]><r>&nom; SA</r>";
        System.out.println("D07 : " + XmlKit.value(d07, "string(/r)"));
        // D08 : A (65) par reference decimale, B (0x42) par reference hexadecimale (x minuscule).
        System.out.println("D08 : " + XmlKit.value("<r>&#65;&#x42;</r>", "string(/r)"));
        // D09 : un commentaire et une PI AVANT la racine sont des enfants du document.
        String d09 = "<!-- note - ok --><?trace on?><r/>";
        System.out.println("D09 : " + XmlKit.select(d09, "/node()"));
        // D10 : les noms XML.
        System.out.println("D10 : " + List.of("prénom", "1er", "_a", "a-b", ".x", "xml-data").stream()
                .map(n -> String.valueOf(isXmlName(n))).collect(Collectors.joining(" ")));
        // D11 : l'echappement d'un texte de contenu.
        System.out.println("D11 : " + escapeText("x]]>y & <z>"));
        // D12 : le lookahead apres '<'.
        String doc = "<?xml version=\"1.0\"?><!DOCTYPE r><!--c--><r><![CDATA[x]]><?p d?></r>";
        StringBuilder kinds = new StringBuilder();
        for (int i = doc.indexOf('<'); i >= 0; i = doc.indexOf('<', i + 1)) {
            kinds.append(kinds.length() == 0 ? "" : " ").append(classify(doc, i));
        }
        System.out.println("D12 : " + kinds);
    }

    static boolean isXmlName(String name) {
        // Version simplifiee (lettres Unicode) : pas de chiffre, de '-' ni de '.' en tete.
        if (name.isEmpty()) {
            return false;
        }
        int first = name.codePointAt(0);
        if (!(Character.isLetter(first) || first == '_' || first == ':')) {
            return false;
        }
        return name.codePoints().skip(1).allMatch(c -> Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-' || c == '.');
    }

    static String escapeText(String text) {
        // & et < toujours ; > seulement s'il fermerait un "]]>".
        return text.replace("&", "&amp;").replace("<", "&lt;").replace("]]>", "]]&gt;");
    }

    static String classify(String doc, int lt) {
        // Les prefixes les plus longs d'abord.
        if (doc.startsWith("<!--", lt)) {
            return "COMMENT";
        }
        if (doc.startsWith("<![CDATA[", lt)) {
            return "CDATA";
        }
        if (doc.startsWith("<!DOCTYPE", lt)) {
            return "DOCTYPE";
        }
        if (doc.startsWith("<?", lt)) {
            return lt == 0 && doc.startsWith("<?xml ") ? "DECLARATION" : "PI";
        }
        return doc.startsWith("</", lt) ? "END_TAG" : "START_TAG";
    }

    static String show(String s) {
        return s.replace("\t", "\\t");
    }
}
