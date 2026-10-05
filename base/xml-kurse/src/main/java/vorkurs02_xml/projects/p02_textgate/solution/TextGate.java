package vorkurs02_xml.projects.p02_textgate.solution;

import vorkurs02_xml.projects.p02_textgate.Data;
import xmlkit.XmlKit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du projet 2 : la passerelle de textes (0.2.6 -> 0.2.8). Ecrire du XML sans le casser,
 * puis relire chaque valeur EXACTEMENT comme le parseur, compare a XmlKit.value.
 */
public class TextGate {

    enum Markup { DECLARATION, COMMENT, CDATA, DOCTYPE, PI, END_TAG, START_TAG, INVALID }

    /** Un refus motive : une valeur qu'on ne peut pas ecrire, ou un texte qu'on ne peut pas lire. */
    static final class Refusal extends RuntimeException {
        Refusal(String reason) {
            super(reason);
        }
    }

    static final int MAX_EXPANSIONS = 100;

    public static void main(String[] args) {
        exportValues();
        normalize();
        references();
        entities();
        markup();
        cdata();
    }

    // ---------- Etape 1 : ecrire ----------

    static void exportValues() {
        for (int i = 0; i < Data.VALUES.size(); i++) {
            String id = String.format("E%02d", i + 1);
            String value = Data.VALUES.get(i);
            try {
                String text = escapeText(value);
                String attr = escapeAttribute(value);
                String xml = "<v a=\"" + attr + "\">" + text + "</v>";
                // Relire avec un vrai parseur est la seule preuve que l'echappement est juste.
                boolean textOk = XmlKit.value(xml, "string(/v)").equals(value);
                boolean attrOk = XmlKit.value(xml, "string(/v/@a)").equals(value);
                System.out.println(id + " texte=" + show(text) + " | attribut=" + show(attr) + " | relu=" + yes(textOk) + "," + yes(attrOk));
            } catch (Refusal r) {
                System.out.println(id + " REFUS " + r.getMessage());
            }
        }
    }

    static boolean isLegalXml10Char(int cp) {
        // Production Char de XML 1.0 : pas de controle < 0x20 sauf TAB/LF/CR, pas de surrogate isole, pas FFFE/FFFF.
        return cp == 0x9 || cp == 0xA || cp == 0xD || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD) || (cp >= 0x10000 && cp <= 0x10FFFF);
    }

    private static void checkLegal(int cp) {
        // On refuse AVANT d'ecrire : aucun echappement ne peut faire passer U+0001 en XML 1.0.
        if (!isLegalXml10Char(cp)) {
            throw new Refusal(String.format("U+%04X", cp));
        }
    }

    static String escapeText(String text) {
        // Par codepoints : l'emoji (2 char) passe tel quel.
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
                // Un CR brut serait normalise en LF a la lecture : &#13; le protege.
                case '\r' -> out.append("&#13;");
                default -> out.appendCodePoint(c);
            }
        }
        return out.toString();
    }

    static String escapeAttribute(String value) {
        // Entre guillemets doubles : seul '"' est dangereux, l'apostrophe reste brute.
        // Les blancs \t \n \r LITTERAUX deviendraient des espaces : on les ecrit en references.
        StringBuilder out = new StringBuilder();
        value.codePoints().forEach(c -> {
            checkLegal(c);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '"' -> out.append("&quot;");
                case '\t' -> out.append("&#9;");
                case '\n' -> out.append("&#10;");
                case '\r' -> out.append("&#13;");
                default -> out.appendCodePoint(c);
            }
        });
        return out.toString();
    }

    // ---------- Etape 2 : normaliser ----------

    static void normalize() {
        for (int i = 0; i < Data.RAW_ATTRIBUTES.size(); i++) {
            String raw = Data.RAW_ATTRIBUTES.get(i);
            String mine = normalizeAttribute(raw);
            String parser = XmlKit.value("<a v=\"" + raw + "\"/>", "string(/a/@v)");
            System.out.println(String.format("N%02d", i + 1) + " [" + show(raw) + "] -> [" + show(mine) + "] | parseur [" + show(parser) + "]");
        }
        for (int i = 0; i < Data.RAW_TEXTS.size(); i++) {
            String raw = Data.RAW_TEXTS.get(i);
            String mine = decodeReferences(normalizeLineEnds(raw));
            String parser = XmlKit.value("<t>" + raw + "</t>", "string(/t)");
            System.out.println(String.format("T%02d", i + 1) + " [" + show(raw) + "] -> [" + show(mine) + "] | parseur [" + show(parser) + "]");
        }
    }

    static String normalizeLineEnds(String raw) {
        // L'ORDRE compte : d'abord le couple CR LF, sinon "\r\n" deviendrait "\n\n".
        return raw.replace("\r\n", "\n").replace('\r', '\n');
    }

    static String normalizeAttribute(String raw) {
        // Recette du parseur pour un attribut CDATA : fins de ligne, puis chaque blanc LITTERAL -> espace,
        // et SEULEMENT ensuite les references : un &#9; produit une vraie tabulation, qui reste.
        // Piege : les espaces ne sont PAS fusionnes ni rognes (ce serait le cas d'un attribut non-CDATA).
        String spaced = normalizeLineEnds(raw).replace('\t', ' ').replace('\n', ' ');
        return decodeReferences(spaced);
    }

    // ---------- Etape 3 : references ----------

    static void references() {
        for (int i = 0; i < Data.REFERENCES.size(); i++) {
            String ref = Data.REFERENCES.get(i);
            String mine;
            try {
                mine = "[" + show(decodeReferences(ref)) + "]";
            } catch (Refusal r) {
                mine = "REFUS " + r.getMessage();
            }
            System.out.println(String.format("R%02d", i + 1) + " " + ref + " -> " + mine + " | parseur " + XmlKit.wellFormed("<r>" + ref + "</r>"));
        }
    }

    static String decodeReferences(String source) {
        // UNE seule passe : "&amp;lt;" donne "&lt;", jamais "<" (on ne relit pas ce qu'on vient d'ecrire).
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
                throw new Refusal("syntaxe");
            }
            out.appendCodePoint(decodeOne(source.substring(i + 1, semi)));
            i = semi + 1;
        }
        return out.toString();
    }

    private static int decodeOne(String body) {
        switch (body) {
            case "lt":
                return '<';
            case "gt":
                return '>';
            case "amp":
                return '&';
            case "apos":
                return '\'';
            case "quot":
                return '"';
            default:
                break;
        }
        int cp;
        // Le x est MINUSCULE : &#X41; est une erreur de syntaxe.
        if (body.matches("#[0-9]{1,7}")) {
            cp = Integer.parseInt(body.substring(1));
        } else if (body.matches("#x[0-9a-fA-F]{1,6}")) {
            cp = Integer.parseInt(body.substring(2), 16);
        } else if (body.startsWith("#")) {
            throw new Refusal("syntaxe");
        } else {
            // Les noms sont sensibles a la casse : &Amp; n'est pas &amp;. Et &eacute; est du HTML.
            throw new Refusal("entite inconnue " + body);
        }
        if (!isLegalXml10Char(cp)) {
            throw new Refusal("caractere interdit");
        }
        return cp;
    }

    // ---------- Etape 4 : entites ----------

    private static final Pattern ENTITY = Pattern.compile(
            "<!ENTITY\\s+(%\\s+)?(\\S+)\\s+(?:([\"'])(.*?)\\3|(?:SYSTEM|PUBLIC)[^>]*)\\s*>", Pattern.DOTALL);

    static void entities() {
        for (int i = 0; i < Data.ENTITY_DOCS.size(); i++) {
            String doc = Data.ENTITY_DOCS.get(i);
            String id = String.format("X%02d", i + 1);
            String subset = doc.substring(doc.indexOf('[') + 1, doc.indexOf("]>"));
            Map<String, String> decls = declarations(subset);
            int rootStart = doc.indexOf('<', doc.indexOf("]>"));
            String content = doc.substring(doc.indexOf('>', rootStart) + 1, doc.lastIndexOf("</"));
            String parser = XmlKit.wellFormed(doc);
            if (parser.equals("OK")) {
                String value = XmlKit.value(doc, "string(/*)");
                parser = value.length() > 60 ? "accepte, " + value.length() + " caracteres" : "[" + value + "]";
            }
            try {
                System.out.println(id + " [" + textContent(content, decls) + "] | parseur " + parser);
            } catch (Refusal r) {
                System.out.println(id + " REFUS " + r.getMessage() + " | parseur " + parser);
            }
        }
    }

    static Map<String, String> declarations(String subset) {
        // putIfAbsent = la regle XML "la PREMIERE declaration gagne". Les entites parametres (%)
        // et externes (SYSTEM / PUBLIC) ne donnent pas de texte ici : on les saute.
        Map<String, String> result = new LinkedHashMap<>();
        Matcher m = ENTITY.matcher(subset);
        while (m.find()) {
            if (m.group(1) == null && m.group(4) != null) {
                result.putIfAbsent(m.group(2), m.group(4));
            }
        }
        return result;
    }

    static String replacementText(String literal) {
        // Des la DECLARATION, seules les references de caracteres sont remplacees ; les &nom; attendent l'usage.
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

    static String textContent(String content, Map<String, String> decls) {
        // Le texte d'un element : texte developpe + CDATA brut ; commentaires et PI ne comptent pas.
        StringBuilder out = new StringBuilder();
        int[] count = new int[1]; // compteur PARTAGE par tout le document : c'est lui qui borne le total
        int i = 0;
        while (i < content.length()) {
            int lt = content.indexOf('<', i);
            int stop = lt < 0 ? content.length() : lt;
            expandInto(out, decls, content.substring(i, stop), new ArrayDeque<>(), count);
            if (lt < 0) {
                break;
            }
            switch (classify(content, lt)) {
                case COMMENT -> i = content.indexOf("-->", lt) + 3;
                case PI -> i = content.indexOf("?>", lt) + 2;
                case CDATA -> {
                    // Dans une CDATA, & et < ne sont plus reconnus : le texte passe tel quel.
                    int end = content.indexOf("]]>", lt);
                    out.append(content, lt + "<![CDATA[".length(), end);
                    i = end + 3;
                }
                default -> throw new Refusal("balisage inattendu");
            }
        }
        return out.toString();
    }

    private static void expandInto(StringBuilder out, Map<String, String> decls, String text, Deque<String> path, int[] count) {
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
            if (name.startsWith("#") || List.of("lt", "gt", "amp", "apos", "quot").contains(name)) {
                out.append(decodeReferences("&" + name + ";"));
                continue;
            }
            // Le chemin des entites OUVERTES detecte un cycle ; le compteur borne l'explosion (billion laughs).
            if (path.contains(name)) {
                throw new Refusal("RECURSION " + String.join(" -> ", path) + " -> " + name);
            }
            if (++count[0] > MAX_EXPANSIONS) {
                throw new Refusal("LIMITE " + MAX_EXPANSIONS + " expansions depassee");
            }
            String literal = decls.get(name);
            if (literal == null) {
                throw new Refusal("ENTITE INCONNUE " + name);
            }
            path.addLast(name);
            expandInto(out, decls, replacementText(literal), path, count);
            path.removeLast();
        }
    }

    // ---------- Etape 5 : balisage ----------

    static Markup classify(String doc, int lt) {
        // Lookahead (0.2.8 S7) : on teste les prefixes LES PLUS LONGS d'abord.
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
            // La declaration n'existe qu'a l'indice 0 ; ailleurs, <?xml ... serait une PI a cible interdite.
            boolean decl = lt == 0 && doc.startsWith("<?xml") && " \t\r\n".indexOf(doc.charAt(5)) >= 0;
            return decl ? Markup.DECLARATION : Markup.PI;
        }
        if (doc.startsWith("</", lt)) {
            return Markup.END_TAG;
        }
        return lt + 1 < doc.length() && isNameStart(doc.charAt(lt + 1)) ? Markup.START_TAG : Markup.INVALID;
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_' || c == ':';
    }

    /** L'indice juste apres le balisage qui commence en lt : on ne cherche pas de '<' DANS une CDATA ou un commentaire. */
    static int skip(String doc, int lt, Markup kind) {
        return switch (kind) {
            case COMMENT -> doc.indexOf("-->", lt) + 3;
            case CDATA -> doc.indexOf("]]>", lt) + 3;
            case PI, DECLARATION -> doc.indexOf("?>", lt) + 2;
            default -> doc.indexOf('>', lt) + 1;
        };
    }

    static void markup() {
        List<String> kinds = new ArrayList<>();
        int i = 0;
        String doc = Data.MARKUP_DOC;
        while ((i = doc.indexOf('<', i)) >= 0) {
            Markup kind = classify(doc, i);
            kinds.add(kind.name());
            i = skip(doc, i, kind);
        }
        System.out.println("BALISAGE " + String.join(" ", kinds));
        for (int k = 0; k < Data.MARKUP_CASES.size(); k++) {
            String doc1 = Data.MARKUP_CASES.get(k);
            System.out.println(String.format("M%02d", k + 1) + " " + judge(doc1) + " | parseur " + XmlKit.wellFormed(doc1));
        }
    }

    static String judge(String doc) {
        String doctype = null;
        String root = null;
        int i = 0;
        while ((i = doc.indexOf('<', i)) >= 0) {
            Markup kind = classify(doc, i);
            int end = skip(doc, i, kind);
            switch (kind) {
                case COMMENT -> {
                    // Jamais "--" dans un commentaire, et pas de '-' colle au "-->" final.
                    String body = doc.substring(i + 4, end - 3);
                    if (body.contains("--") || body.endsWith("-")) {
                        return "COMMENTAIRE_INVALIDE";
                    }
                }
                case PI -> {
                    String target = doc.substring(i + 2, end - 2).split("[ \t\r\n]", 2)[0];
                    // "xml" est reserve, quelle que soit la casse ; "xml-stylesheet" reste un nom permis.
                    if (target.equalsIgnoreCase("xml")) {
                        return "CIBLE_RESERVEE " + target;
                    }
                }
                case DOCTYPE -> {
                    // Un seul DOCTYPE, avant la racine : c'est une regle de BONNE FORME.
                    if (doctype != null) {
                        return "DOCTYPE_DOUBLE";
                    }
                    doctype = doc.substring(i + "<!DOCTYPE".length(), end - 1).strip().split("[ \t\r\n\\[]", 2)[0];
                }
                case START_TAG -> {
                    if (root == null) {
                        root = doc.substring(i + 1).split("[ \t\r\n/>]", 2)[0];
                    }
                }
                default -> {
                }
            }
            i = end;
        }
        // DOCTYPE != racine : contrainte de VALIDITE (VC), pas de bonne forme : le parseur accepte.
        if (doctype != null && !doctype.equals(root)) {
            return "OK mais DOCTYPE " + doctype + " != racine " + root;
        }
        return "OK";
    }

    // ---------- Etape 6 : CDATA ----------

    static void cdata() {
        for (int i = 0; i < Data.CDATA_TEXTS.size(); i++) {
            String text = Data.CDATA_TEXTS.get(i);
            // Une CDATA ne peut pas contenir "]]>" : on coupe ENTRE "]]" et ">" et on rouvre une section.
            String section = "<![CDATA[" + text.replace("]]>", "]]]]><![CDATA[>") + "]]>";
            boolean same = XmlKit.value("<c>" + section + "</c>", "string(/c)").equals(text);
            System.out.println(String.format("C%02d", i + 1) + " " + section + " | relu=" + yes(same));
        }
    }

    // ---------- outils d'affichage ----------

    /** Rend visibles les caracteres de controle : une tabulation s'affiche \t, etc. */
    static String show(String s) {
        return s.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String yes(boolean b) {
        return b ? "oui" : "non";
    }
}
