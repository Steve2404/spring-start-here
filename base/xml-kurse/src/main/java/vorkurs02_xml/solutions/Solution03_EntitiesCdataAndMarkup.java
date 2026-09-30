package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige de l'exercice 3. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise03_EntitiesCdataAndMarkup.
 */
public class Solution03_EntitiesCdataAndMarkup {

    public enum Markup { DECLARATION, COMMENT, CDATA, DOCTYPE, PI, END_TAG, START_TAG, INVALID }

    public static Markup classifyMarkup(String doc, int lt) {
        // Lookahead du parseur (0.2.8 S7) : on teste les prefixes LES PLUS LONGS d'abord,
        // sinon "<!--" serait avale par une regle plus generale sur "<!".
        if (doc.startsWith("<!--", lt)) {
            return Markup.COMMENT;
        }
        if (doc.startsWith("<![CDATA[", lt)) {
            return Markup.CDATA;
        }
        if (doc.startsWith("<!DOCTYPE", lt)) {
            return Markup.DOCTYPE;
        }
        if (doc.startsWith("<?", lt)) {
            // La declaration n'existe qu'en position 0 ; ailleurs c'est une PI (a cible interdite).
            boolean decl = lt == 0 && doc.startsWith("<?xml", 0) && doc.length() > 5
                    && " \t\r\n".indexOf(doc.charAt(5)) >= 0;
            return decl ? Markup.DECLARATION : Markup.PI;
        }
        if (doc.startsWith("</", lt)) {
            return Markup.END_TAG;
        }
        if (lt + 1 < doc.length() && isNameStart(doc.charAt(lt + 1))) {
            return Markup.START_TAG;
        }
        return Markup.INVALID;
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_' || c == ':';
    }

    public static String toCdata(String text) {
        // Une section CDATA ne peut pas contenir "]]>" : on coupe ENTRE "]]" et ">" et on
        // rouvre une section. Le texte recolle par le parseur est identique a l'original.
        return "<![CDATA[" + text.replace("]]>", "]]]]><![CDATA[>") + "]]>";
    }

    public static boolean isValidPiTarget(String target) {
        // Un nom XML (version ASCII simplifiee), mais jamais "xml" quelle que soit la casse :
        // ce nom est reserve a la declaration. "xml-stylesheet" reste permis.
        return target.matches("[A-Za-z_:][A-Za-z0-9._:-]*") && !target.equalsIgnoreCase("xml");
    }

    private static final Pattern ENTITY = Pattern.compile(
            "<!ENTITY\\s+(%\\s+)?(\\S+)\\s+(?:([\"'])(.*?)\\3|(?:SYSTEM|PUBLIC)[^>]*)\\s*>", Pattern.DOTALL);

    public static Map<String, String> parseEntityDeclarations(String internalSubset) {
        // putIfAbsent applique la regle XML "la PREMIERE declaration gagne" ; les entites
        // parametres (%) et externes (SYSTEM/PUBLIC) ne produisent pas de texte ici : on les saute.
        Map<String, String> result = new LinkedHashMap<>();
        Matcher m = ENTITY.matcher(internalSubset);
        while (m.find()) {
            boolean parameter = m.group(1) != null;
            boolean internal = m.group(4) != null;
            if (!parameter && internal) {
                result.putIfAbsent(m.group(2), m.group(4));
            }
        }
        return result;
    }

    public static String replacementText(String literal) {
        // Des la DECLARATION, seules les references de caracteres sont remplacees ; les &nom;
        // restent tels quels et ne seront developpes qu'a l'utilisation.
        Matcher m = Pattern.compile("&#(x[0-9a-fA-F]+|[0-9]+);").matcher(literal);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String body = m.group(1);
            int cp = body.startsWith("x") ? Integer.parseInt(body.substring(1), 16) : Integer.parseInt(body);
            m.appendReplacement(out, Matcher.quoteReplacement(new String(Character.toChars(cp))));
        }
        m.appendTail(out);
        return out.toString();
    }

    public static String expand(Map<String, String> decls, String content, int maxExpansions) {
        // La recursion suit le chemin des entites ouvertes (pile) pour detecter un cycle, et un
        // compteur PARTAGE (int[1]) pour borner le total : c'est la defense anti "billion laughs".
        StringBuilder out = new StringBuilder();
        expandInto(out, decls, content, new ArrayDeque<>(), new int[1], maxExpansions);
        return out.toString();
    }

    private static void expandInto(StringBuilder out, Map<String, String> decls, String text,
                                   Deque<String> path, int[] count, int max) {
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != '&') {
                out.append(c);
                i++;
                continue;
            }
            int semi = text.indexOf(';', i);
            String name = text.substring(i + 1, semi);
            i = semi + 1;
            switch (name) {
                case "lt" -> out.append('<');
                case "gt" -> out.append('>');
                case "amp" -> out.append('&');
                case "apos" -> out.append('\'');
                case "quot" -> out.append('"');
                default -> {
                    if (name.startsWith("#")) {
                        out.append(replacementText("&" + name + ";"));
                        continue;
                    }
                    if (path.contains(name)) {
                        throw new IllegalStateException("Reference recursive : "
                                + String.join(" -> ", path) + " -> " + name);
                    }
                    if (++count[0] > max) {
                        throw new IllegalStateException("Limite de " + max + " expansions depassee");
                    }
                    String literal = decls.get(name);
                    if (literal == null) {
                        throw new IllegalArgumentException("Entite non declaree : " + name);
                    }
                    String replacement = replacementText(literal);
                    if (replacement.indexOf('<') >= 0) {
                        throw new IllegalStateException("L'entite " + name + " contient du balisage : " + replacement);
                    }
                    path.addLast(name);
                    expandInto(out, decls, replacement, path, count, max);
                    path.removeLast();
                }
            }
        }
    }

    public static String textContent(String elementContent, Map<String, String> decls) {
        // On rejoue getTextContent() : texte developpe, CDATA brut, commentaires et PI ignores.
        // classifyMarkup decide quoi faire a chaque '<', exactement comme le parseur.
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < elementContent.length()) {
            int lt = elementContent.indexOf('<', i);
            int stop = lt < 0 ? elementContent.length() : lt;
            out.append(expand(decls, elementContent.substring(i, stop), 64_000));
            if (lt < 0) {
                break;
            }
            switch (classifyMarkup(elementContent, lt)) {
                case COMMENT -> i = elementContent.indexOf("-->", lt) + 3;
                case PI -> i = elementContent.indexOf("?>", lt) + 2;
                case CDATA -> {
                    int end = elementContent.indexOf("]]>", lt);
                    out.append(elementContent, lt + "<![CDATA[".length(), end);
                    i = end + 3;
                }
                default -> throw new IllegalArgumentException("Balisage non gere a la position " + lt);
            }
        }
        return out.toString();
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
