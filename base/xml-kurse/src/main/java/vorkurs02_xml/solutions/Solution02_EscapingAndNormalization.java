package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Map;

/**
 * Corrige de l'exercice 2. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise02_EscapingAndNormalization.
 */
public class Solution02_EscapingAndNormalization {

    public static boolean isLegalXml10Char(int cp) {
        // Production Char de XML 1.0 : ni les controles < 0x20 (sauf TAB/LF/CR), ni les
        // surrogates isoles D800-DFFF, ni FFFE/FFFF. Le reste d'Unicode passe.
        return cp == 0x9 || cp == 0xA || cp == 0xD
                || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF);
    }

    public static String normalizeLineEnds(String raw) {
        // L'ORDRE compte : d'abord le couple CR LF, sinon "\r\n" deviendrait "\n\n".
        return raw.replace("\r\n", "\n").replace('\r', '\n');
    }

    public static String escapeText(String text) {
        // On parcourt des codepoints : un emoji (2 char) doit passer tel quel, et un caractere
        // interdit doit etre refuse AVANT d'ecrire un XML que plus personne ne pourra relire.
        StringBuilder out = new StringBuilder();
        int[] cps = text.codePoints().toArray();
        for (int i = 0; i < cps.length; i++) {
            int c = cps[i];
            checkLegal(c);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                // '>' seul est permis ; il ne devient &gt; que s'il fermerait un "]]>".
                case '>' -> out.append(i >= 2 && cps[i - 1] == ']' && cps[i - 2] == ']' ? "&gt;" : ">");
                // Un CR brut serait normalise en LF par le parseur : &#13; le protege.
                case '\r' -> out.append("&#13;");
                default -> out.appendCodePoint(c);
            }
        }
        return out.toString();
    }

    private static void checkLegal(int c) {
        if (!isLegalXml10Char(c)) {
            throw new IllegalArgumentException(String.format("Caractere interdit en XML 1.0 : U+%04X", c));
        }
    }

    public static String escapeAttribute(String value, char quote) {
        // Dans un attribut, les blancs LITTERAUX \t \n \r seront transformes en espaces par la
        // normalisation : pour les garder, il faut les ecrire en references de caracteres.
        StringBuilder out = new StringBuilder();
        value.codePoints().forEach(c -> {
            checkLegal(c);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '\t' -> out.append("&#9;");
                case '\n' -> out.append("&#10;");
                case '\r' -> out.append("&#13;");
                // Seul le guillemet qui DELIMITE la valeur est dangereux ; l'autre reste brut.
                case '"' -> out.append(quote == '"' ? "&quot;" : "\"");
                case '\'' -> out.append(quote == '\'' ? "&apos;" : "'");
                default -> out.appendCodePoint(c);
            }
        });
        return out.toString();
    }

    public static String decodeReferences(String source) {
        // UNE seule passe de gauche a droite : "&amp;lt;" donne "&lt;" et surtout pas "<"
        // (on ne re-decode jamais ce qu'on vient de produire).
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c != '&') {
                out.append(c);
                i++;
                continue;
            }
            int semi = source.indexOf(';', i);
            if (semi < 0) {
                throw new IllegalArgumentException("Reference sans ';' a la position " + i);
            }
            out.appendCodePoint(decodeOne(source.substring(i + 1, semi)));
            i = semi + 1;
        }
        return out.toString();
    }

    private static int decodeOne(String body) {
        // Une boite par reference : predefinie, decimale ou hexadecimale ; tout le reste est refuse.
        int cp;
        switch (body) {
            case "lt" -> cp = '<';
            case "gt" -> cp = '>';
            case "amp" -> cp = '&';
            case "apos" -> cp = '\'';
            case "quot" -> cp = '"';
            default -> {
                if (body.matches("#[0-9]{1,7}")) {
                    cp = Integer.parseInt(body.substring(1));
                } else if (body.matches("#x[0-9a-fA-F]{1,6}")) {
                    cp = Integer.parseInt(body.substring(2), 16);
                } else {
                    throw new IllegalArgumentException("Entite inconnue : " + body);
                }
            }
        }
        checkLegal(cp);
        return cp;
    }

    public static String normalizeAttributeValue(String rawSource) {
        // Recette du parseur pour un attribut CDATA : 1) fins de ligne, 2) chaque blanc LITTERAL
        // devient un espace, 3) SEULEMENT ensuite les references (leur resultat n'est pas touche).
        String lines = normalizeLineEnds(rawSource);
        String spaced = lines.replace('\t', ' ').replace('\n', ' ');
        return decodeReferences(spaced);
    }

    public static String toXmlElement(String name, Map<String, String> attributes, String text) {
        // On choisit, attribut par attribut, le guillemet qui evite d'echapper : '"' par defaut,
        // '\'' si la valeur contient des " mais pas de '.
        StringBuilder out = new StringBuilder("<").append(name);
        attributes.forEach((k, v) -> {
            char quote = v.indexOf('"') >= 0 && v.indexOf('\'') < 0 ? '\'' : '"';
            out.append(' ').append(k).append('=').append(quote)
                    .append(escapeAttribute(v, quote)).append(quote);
        });
        if (text == null) {
            return out.append("/>").toString();
        }
        return out.append('>').append(escapeText(text)).append("</").append(name).append('>').toString();
    }

    static Document jdkParse(String xml) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
