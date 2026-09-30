package vorkurs02_xml.solutions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Corrige de l'exercice 1. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise01_WellFormednessDiagnoser.
 */
public class Solution01_WellFormednessDiagnoser {

    public enum Rule {
        OK, BAD_NAME, MALFORMED_TAG, DUPLICATE_ATTRIBUTE, LT_IN_ATTRIBUTE, BAD_REFERENCE,
        CDATA_END_IN_TEXT, BAD_COMMENT, MISPLACED_DECLARATION, MISMATCHED_END_TAG,
        END_WITHOUT_START, UNCLOSED_ELEMENT, CONTENT_OUTSIDE_ROOT, MULTIPLE_ROOTS, NO_ROOT
    }

    public enum Kind { START, END, EMPTY }

    public record Attribute(String name, String value) {
    }

    public record Tag(Kind kind, String name, List<Attribute> attributes, int start, int end) {
    }

    public record Diagnosis(Rule rule, int line) {
    }

    public static final class XmlProblem extends RuntimeException {
        public final Rule rule;
        public final int index;

        public XmlProblem(Rule rule, int index) {
            super(rule + " @" + index);
            this.rule = rule;
            this.index = index;
        }
    }

    private static final Set<String> PREDEFINED = Set.of("lt", "gt", "amp", "apos", "quot");

    public static boolean isXmlName(String name) {
        // On parcourt des CODEPOINTS (pas des char) : un caractere hors BMP tient sur 2 char,
        // et la regle "1er caractere != caracteres suivants" doit s'appliquer au vrai 1er caractere.
        if (name == null || name.isEmpty()) {
            return false;
        }
        int[] cps = name.codePoints().toArray();
        if (!isNameStartChar(cps[0])) {
            return false;
        }
        for (int i = 1; i < cps.length; i++) {
            if (!isNameChar(cps[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNameStartChar(int c) {
        // Recopie directe de la production NameStartChar de XML 1.0 (5e edition).
        return c == ':' || (c >= 'A' && c <= 'Z') || c == '_' || (c >= 'a' && c <= 'z')
                || (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF)
                || (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D)
                || (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF)
                || (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF);
    }

    private static boolean isNameChar(int c) {
        // NameChar = NameStartChar + chiffres, '-', '.', le point median et 2 petites plages.
        return isNameStartChar(c) || c == '-' || c == '.' || (c >= '0' && c <= '9') || c == 0xB7
                || (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <= 0x2040);
    }

    public static Tag readTag(String doc, int lt) {
        // Un vrai petit automate : on avance un curseur j et chaque "attente" non satisfaite
        // (nom, '=', guillemet, espace entre attributs) devient une XmlProblem a la position du '<'.
        boolean end = doc.startsWith("</", lt);
        int j = end ? lt + 2 : lt + 1;
        int nameStart = j;
        while (j < doc.length() && !isTagDelimiter(doc.charAt(j))) {
            j++;
        }
        String name = doc.substring(nameStart, j);
        if (!isXmlName(name)) {
            throw new XmlProblem(Rule.BAD_NAME, lt);
        }
        if (end) {
            j = skipWhitespace(doc, j);
            if (j >= doc.length() || doc.charAt(j) != '>') {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            return new Tag(Kind.END, name, List.of(), lt, j + 1);
        }
        List<Attribute> attributes = new ArrayList<>();
        while (true) {
            int afterSpace = skipWhitespace(doc, j);
            boolean hadSpace = afterSpace > j;
            j = afterSpace;
            if (j >= doc.length()) {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            if (doc.charAt(j) == '>') {
                return new Tag(Kind.START, name, attributes, lt, j + 1);
            }
            if (doc.startsWith("/>", j)) {
                return new Tag(Kind.EMPTY, name, attributes, lt, j + 2);
            }
            // Piege du cours (0.2.4) : 2 attributs DOIVENT etre separes par un blanc.
            if (!hadSpace) {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            int attrStart = j;
            while (j < doc.length() && !isTagDelimiter(doc.charAt(j)) && doc.charAt(j) != '=') {
                j++;
            }
            String attrName = doc.substring(attrStart, j);
            if (!isXmlName(attrName)) {
                throw new XmlProblem(Rule.BAD_NAME, lt);
            }
            j = skipWhitespace(doc, j);
            if (j >= doc.length() || doc.charAt(j) != '=') {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            j = skipWhitespace(doc, j + 1);
            if (j >= doc.length() || (doc.charAt(j) != '"' && doc.charAt(j) != '\'')) {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            char quote = doc.charAt(j);
            int close = doc.indexOf(quote, j + 1);
            if (close < 0) {
                throw new XmlProblem(Rule.MALFORMED_TAG, lt);
            }
            attributes.add(new Attribute(attrName, doc.substring(j + 1, close)));
            j = close + 1;
        }
    }

    private static boolean isTagDelimiter(char c) {
        return c == '>' || c == '/' || isXmlWhitespace(c);
    }

    private static boolean isXmlWhitespace(char c) {
        // XML ne connait que 4 blancs (0.2.6) : pas Character.isWhitespace, trop large.
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static int skipWhitespace(String s, int j) {
        while (j < s.length() && isXmlWhitespace(s.charAt(j))) {
            j++;
        }
        return j;
    }

    public static Optional<Rule> checkAttributes(Tag tag) {
        // L'unicite se teste avec un Set : add() rend false au 2e passage du meme nom.
        // L'ordre des regles suit l'ordre du document : on s'arrete au 1er attribut fautif.
        Set<String> seen = new HashSet<>();
        for (Attribute a : tag.attributes()) {
            if (!seen.add(a.name())) {
                return Optional.of(Rule.DUPLICATE_ATTRIBUTE);
            }
            if (a.value().indexOf('<') >= 0) {
                return Optional.of(Rule.LT_IN_ATTRIBUTE);
            }
            Optional<Rule> ref = checkReferences(a.value());
            if (ref.isPresent()) {
                return ref;
            }
        }
        return Optional.empty();
    }

    public static Optional<Rule> checkReferences(String text) {
        // Chaque '&' ouvre une reference qui DOIT finir par ';' : on isole le corps entre les deux,
        // puis on accepte seulement les 5 noms predefinis, &#decimal; et &#xhexa; (x minuscule !).
        int amp = text.indexOf('&');
        while (amp >= 0) {
            int semi = text.indexOf(';', amp);
            if (semi < 0) {
                return Optional.of(Rule.BAD_REFERENCE);
            }
            String body = text.substring(amp + 1, semi);
            if (!isValidReferenceBody(body)) {
                return Optional.of(Rule.BAD_REFERENCE);
            }
            amp = text.indexOf('&', semi);
        }
        return Optional.empty();
    }

    private static boolean isValidReferenceBody(String body) {
        if (PREDEFINED.contains(body)) {
            return true;
        }
        int codePoint;
        if (body.matches("#[0-9]+")) {
            codePoint = parseCodePoint(body.substring(1), 10);
        } else if (body.matches("#x[0-9a-fA-F]+")) {
            codePoint = parseCodePoint(body.substring(2), 16);
        } else {
            return false;
        }
        // Une reference syntaxiquement juste peut designer un caractere interdit (&#0;).
        return codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD
                || (codePoint >= 0x20 && codePoint <= 0xD7FF)
                || (codePoint >= 0xE000 && codePoint <= 0xFFFD)
                || (codePoint >= 0x10000 && codePoint <= 0x10FFFF);
    }

    private static int parseCodePoint(String digits, int radix) {
        // Integer.parseInt deborderait sur "&#99999999999;" : on plafonne a -1 (= invalide).
        try {
            return Integer.parseInt(digits, radix);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Optional<Rule> checkText(String text) {
        // "]]>" n'a le droit d'exister QUE comme fin de CDATA ; dans du texte normal c'est une erreur,
        // alors qu'un '>' seul est permis (le parseur l'a confirme : <a>a>b</a> est well-formed).
        Optional<Rule> ref = checkReferences(text);
        if (ref.isPresent()) {
            return ref;
        }
        return text.contains("]]>") ? Optional.of(Rule.CDATA_END_IN_TEXT) : Optional.empty();
    }

    public static Optional<Rule> checkComment(String content) {
        // Grammaire : '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->' : jamais "--" dedans,
        // et jamais un '-' colle au "-->" final (sinon on ecrirait "--->").
        if (content.contains("--") || content.endsWith("-")) {
            return Optional.of(Rule.BAD_COMMENT);
        }
        return Optional.empty();
    }

    public static Diagnosis diagnose(String doc) {
        // Une seule passe gauche -> droite avec une pile (LIFO, 0.2.5) : comme un vrai parseur,
        // la PREMIERE erreur rencontree gagne. Chaque sous-verification est une boite deja ecrite.
        Deque<Tag> open = new ArrayDeque<>();
        boolean rootSeen = false;
        int i = 0;
        if (doc.startsWith("<?xml") && doc.length() > 5 && isXmlWhitespace(doc.charAt(5))) {
            int close = doc.indexOf("?>");
            if (close < 0) {
                return new Diagnosis(Rule.MALFORMED_TAG, 1);
            }
            i = close + 2;
        }
        while (i < doc.length()) {
            if (doc.charAt(i) != '<') {
                int next = doc.indexOf('<', i);
                int stop = next < 0 ? doc.length() : next;
                String text = doc.substring(i, stop);
                if (open.isEmpty()) {
                    if (!text.isBlank()) {
                        return new Diagnosis(Rule.CONTENT_OUTSIDE_ROOT, lineOf(doc, firstNonBlank(doc, i)));
                    }
                } else {
                    Optional<Rule> r = checkText(text);
                    if (r.isPresent()) {
                        return new Diagnosis(r.get(), lineOf(doc, i));
                    }
                }
                i = stop;
            } else if (doc.startsWith("<!--", i)) {
                int close = doc.indexOf("-->", i + 4);
                if (close < 0) {
                    return new Diagnosis(Rule.MALFORMED_TAG, lineOf(doc, i));
                }
                Optional<Rule> r = checkComment(doc.substring(i + 4, close));
                if (r.isPresent()) {
                    return new Diagnosis(r.get(), lineOf(doc, i));
                }
                i = close + 3;
            } else if (doc.startsWith("<?", i)) {
                int close = doc.indexOf("?>", i + 2);
                if (close < 0) {
                    return new Diagnosis(Rule.MALFORMED_TAG, lineOf(doc, i));
                }
                int t = i + 2;
                while (t < close && !isXmlWhitespace(doc.charAt(t))) {
                    t++;
                }
                String target = doc.substring(i + 2, t);
                if (target.equalsIgnoreCase("xml")) {
                    return new Diagnosis(Rule.MISPLACED_DECLARATION, lineOf(doc, i));
                }
                if (!isXmlName(target)) {
                    return new Diagnosis(Rule.BAD_NAME, lineOf(doc, i));
                }
                i = close + 2;
            } else {
                Tag tag;
                try {
                    tag = readTag(doc, i);
                } catch (XmlProblem p) {
                    return new Diagnosis(p.rule, lineOf(doc, p.index));
                }
                Optional<Rule> r = checkAttributes(tag);
                if (r.isPresent()) {
                    return new Diagnosis(r.get(), lineOf(doc, i));
                }
                switch (tag.kind()) {
                    case START, EMPTY -> {
                        if (open.isEmpty() && rootSeen) {
                            return new Diagnosis(Rule.MULTIPLE_ROOTS, lineOf(doc, i));
                        }
                        rootSeen = true;
                        if (tag.kind() == Kind.START) {
                            open.push(tag);
                        }
                    }
                    case END -> {
                        if (open.isEmpty()) {
                            return new Diagnosis(Rule.END_WITHOUT_START, lineOf(doc, i));
                        }
                        // Comparaison EXACTE (equals, pas equalsIgnoreCase) : <Title> != </title>.
                        if (!open.pop().name().equals(tag.name())) {
                            return new Diagnosis(Rule.MISMATCHED_END_TAG, lineOf(doc, i));
                        }
                    }
                }
                i = tag.end();
            }
        }
        if (!open.isEmpty()) {
            // On pointe l'element ouvert le plus interne : c'est la CAUSE, alors que le parseur
            // ne s'en apercoit qu'a la fin du fichier (le point de DECOUVERTE, cf. 0.2.22).
            return new Diagnosis(Rule.UNCLOSED_ELEMENT, lineOf(doc, open.peek().start()));
        }
        return rootSeen ? new Diagnosis(Rule.OK, 0) : new Diagnosis(Rule.NO_ROOT, 0);
    }

    private static int firstNonBlank(String doc, int i) {
        while (i < doc.length() && isXmlWhitespace(doc.charAt(i))) {
            i++;
        }
        return i;
    }

    static int lineOf(String doc, int index) {
        int line = 1;
        for (int k = 0; k < index && k < doc.length(); k++) {
            if (doc.charAt(k) == '\n') {
                line++;
            }
        }
        return line;
    }

    static boolean jdkSaysWellFormed(String doc) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            builder.parse(new InputSource(new StringReader(doc)));
            return true;
        } catch (SAXParseException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
