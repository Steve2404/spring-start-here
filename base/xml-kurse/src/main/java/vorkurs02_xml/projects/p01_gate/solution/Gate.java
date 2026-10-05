package vorkurs02_xml.projects.p01_gate.solution;

import vorkurs02_xml.projects.p01_gate.Data;
import xmlkit.XmlKit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du projet 1 : le portier d'import (0.2.1 -> 0.2.5). Un diagnostic maison en UNE passe
 * gauche -> droite avec une pile, compare au vrai parseur (XmlKit.wellFormed).
 */
public class Gate {

    enum Rule {
        OK, BAD_DECLARATION, MISPLACED_DECLARATION, BAD_NAME, MALFORMED_TAG, DUPLICATE_ATTRIBUTE,
        MISMATCHED_END_TAG, END_WITHOUT_START, UNCLOSED_ELEMENT, CONTENT_OUTSIDE_ROOT, MULTIPLE_ROOTS, NO_ROOT
    }

    enum Kind { START, END, EMPTY }

    /** Une balise lue : les attributs gardent l'ordre du texte (LinkedHashMap), sans que l'ordre ait de sens pour XML. */
    record Tag(Kind kind, String name, Map<String, String> attributes, int start, int end) {
    }

    /** Un element de l'arbre maison : un noeud = un element complet, de sa balise ouvrante a sa fermante. */
    static final class Element {
        final String name;
        final Map<String, String> attributes;
        final Element parent;
        final List<Element> children = new ArrayList<>();
        final StringBuilder text = new StringBuilder();

        Element(String name, Map<String, String> attributes, Element parent) {
            this.name = name;
            this.attributes = attributes;
            this.parent = parent;
        }
    }

    /** Le verdict : la regle, la ligne de la CAUSE, et l'arbre si tout va bien. */
    record Verdict(Rule rule, int line, Element root) {
    }

    /** Une erreur trouvee pendant la lecture d'une balise : on remonte la regle et la position du '<'. */
    static final class Problem extends RuntimeException {
        final Rule rule;
        final int index;

        Problem(Rule rule, int index) {
            super(rule.name());
            this.rule = rule;
            this.index = index;
        }
    }

    // version obligatoire et EN PREMIER, puis encoding, puis standalone (yes|no) : l'ordre est impose (0.2.2 S2).
    private static final Pattern DECLARATION = Pattern.compile(
            "<\\?xml\\s+version\\s*=\\s*(\"1\\.[0-9]+\"|'1\\.[0-9]+')"
                    + "(\\s+encoding\\s*=\\s*(\"[A-Za-z][A-Za-z0-9._-]*\"|'[A-Za-z][A-Za-z0-9._-]*'))?"
                    + "(\\s+standalone\\s*=\\s*(\"(yes|no)\"|'(yes|no)'))?\\s*\\?>");

    public static void main(String[] args) {
        int accepted = 0;
        int agree = 0;
        Element first = null;
        for (Data.Submission s : Data.SUBMISSIONS) {
            Verdict v = diagnose(s.text());
            String parser = XmlKit.wellFormed(s.text());
            // On compare seulement OK/KO : la LIGNE peut differer (cause contre point de decouverte, voir D14).
            if ((v.rule() == Rule.OK) == parser.equals("OK")) {
                agree++;
            }
            if (v.rule() == Rule.OK) {
                accepted++;
                if (first == null) {
                    first = v.root();
                }
                System.out.println(s.id() + " OK " + summary(v.root()) + " | parseur " + parser);
            } else {
                System.out.println(s.id() + " " + v.rule() + " ligne " + v.line() + " | parseur " + parser);
            }
        }
        System.out.println("BILAN : " + accepted + " acceptes, " + (Data.SUBMISSIONS.size() - accepted)
                + " refuses, accord avec le parseur " + agree + "/" + Data.SUBMISSIONS.size());

        System.out.println("PLAN de D01 :");
        plan(first, 1);

        System.out.println("RELATIONS de D01 :");
        relations(first, first.name);

        for (String book : Data.BOOKS) {
            String xml = convert(book);
            System.out.println("CONVERSION " + xml + " | portier " + diagnose(xml).rule() + " | parseur " + XmlKit.wellFormed(xml));
        }
    }

    static Verdict diagnose(String doc) {
        // Une seule passe, comme un vrai parseur : la PREMIERE erreur rencontree gagne.
        Deque<Element> open = new ArrayDeque<>();
        Deque<Integer> openAt = new ArrayDeque<>();
        Element root = null;
        int i = 0;
        if (doc.startsWith("<?xml") && doc.length() > 5 && isXmlWhitespace(doc.charAt(5))) {
            Matcher m = DECLARATION.matcher(doc);
            // lookingAt : la declaration doit commencer a l'indice 0, sans rien devant (pas meme un blanc).
            if (!m.lookingAt()) {
                return new Verdict(Rule.BAD_DECLARATION, 1, null);
            }
            i = m.end();
        }
        while (i < doc.length()) {
            if (doc.charAt(i) != '<') {
                int next = doc.indexOf('<', i);
                int stop = next < 0 ? doc.length() : next;
                String text = doc.substring(i, stop);
                if (open.isEmpty()) {
                    // Hors de la racine, seuls des blancs XML sont permis.
                    if (!text.isBlank()) {
                        return new Verdict(Rule.CONTENT_OUTSIDE_ROOT, lineOf(doc, firstNonBlank(doc, i)), null);
                    }
                } else {
                    open.peek().text.append(text);
                }
                i = stop;
                continue;
            }
            if (doc.startsWith("<?xml", i)) {
                // Piege : la declaration n'est PERMISE qu'au tout debut ; une ligne vide devant suffit a la refuser.
                return new Verdict(Rule.MISPLACED_DECLARATION, lineOf(doc, i), null);
            }
            Tag tag;
            try {
                tag = readTag(doc, i);
            } catch (Problem p) {
                return new Verdict(p.rule, lineOf(doc, p.index), null);
            }
            switch (tag.kind()) {
                case START, EMPTY -> {
                    if (open.isEmpty() && root != null) {
                        return new Verdict(Rule.MULTIPLE_ROOTS, lineOf(doc, i), null);
                    }
                    Element e = new Element(tag.name(), tag.attributes(), open.peek());
                    if (open.isEmpty()) {
                        root = e;
                    } else {
                        open.peek().children.add(e);
                    }
                    if (tag.kind() == Kind.START) {
                        open.push(e);
                        openAt.push(i);
                    }
                }
                case END -> {
                    if (open.isEmpty()) {
                        return new Verdict(Rule.END_WITHOUT_START, lineOf(doc, i), null);
                    }
                    openAt.pop();
                    // equals, jamais equalsIgnoreCase : XML distingue les majuscules (<Titre> != </titre>).
                    if (!open.pop().name.equals(tag.name())) {
                        return new Verdict(Rule.MISMATCHED_END_TAG, lineOf(doc, i), null);
                    }
                }
            }
            i = tag.end();
        }
        if (!open.isEmpty()) {
            // La CAUSE est l'element ouvert le plus interne ; le parseur, lui, ne s'en apercoit qu'a la fin.
            return new Verdict(Rule.UNCLOSED_ELEMENT, lineOf(doc, openAt.peek()), null);
        }
        // Sans racine, l'erreur se voit a la FIN du document : c'est la qu'on la situe.
        return root == null ? new Verdict(Rule.NO_ROOT, lineOf(doc, doc.length()), null) : new Verdict(Rule.OK, 0, root);
    }

    static Tag readTag(String doc, int lt) {
        // Un petit automate : chaque attente non satisfaite (nom, '=', guillemet, blanc) devient un Problem.
        boolean end = doc.startsWith("</", lt);
        int j = end ? lt + 2 : lt + 1;
        int nameStart = j;
        while (j < doc.length() && !isTagDelimiter(doc.charAt(j))) {
            j++;
        }
        String name = doc.substring(nameStart, j);
        if (!isXmlName(name)) {
            throw new Problem(Rule.BAD_NAME, lt);
        }
        if (end) {
            // Une balise fermante n'a JAMAIS d'attribut : seulement des blancs avant '>'.
            j = skipWhitespace(doc, j);
            if (j >= doc.length() || doc.charAt(j) != '>') {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            return new Tag(Kind.END, name, Map.of(), lt, j + 1);
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        while (true) {
            int afterSpace = skipWhitespace(doc, j);
            boolean hadSpace = afterSpace > j;
            j = afterSpace;
            if (j >= doc.length()) {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            if (doc.charAt(j) == '>') {
                return new Tag(Kind.START, name, attributes, lt, j + 1);
            }
            if (doc.startsWith("/>", j)) {
                return new Tag(Kind.EMPTY, name, attributes, lt, j + 2);
            }
            // Piege (0.2.4 S4) : deux attributs DOIVENT etre separes par au moins un blanc.
            if (!hadSpace) {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            int attrStart = j;
            while (j < doc.length() && !isTagDelimiter(doc.charAt(j)) && doc.charAt(j) != '=') {
                j++;
            }
            String attrName = doc.substring(attrStart, j);
            if (!isXmlName(attrName)) {
                throw new Problem(Rule.BAD_NAME, lt);
            }
            j = skipWhitespace(doc, j);
            if (j >= doc.length() || doc.charAt(j) != '=') {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            j = skipWhitespace(doc, j + 1);
            // Les guillemets sont obligatoires, simples ou doubles, et le meme des deux cotes.
            if (j >= doc.length() || (doc.charAt(j) != '"' && doc.charAt(j) != '\'')) {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            char quote = doc.charAt(j);
            int close = doc.indexOf(quote, j + 1);
            if (close < 0) {
                throw new Problem(Rule.MALFORMED_TAG, lt);
            }
            // put rend l'ancienne valeur : non null = le nom etait deja la (Unique Att Spec).
            if (attributes.put(attrName, doc.substring(j + 1, close)) != null) {
                throw new Problem(Rule.DUPLICATE_ATTRIBUTE, lt);
            }
            j = close + 1;
        }
    }

    static boolean isXmlName(String name) {
        // Par CODEPOINTS : un caractere hors BMP tient sur deux char.
        if (name.isEmpty()) {
            return false;
        }
        int[] cps = name.codePoints().toArray();
        if (!isNameStartChar(cps[0])) {
            return false;
        }
        for (int k = 1; k < cps.length; k++) {
            if (!isNameChar(cps[k])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNameStartChar(int c) {
        // La production NameStartChar de XML 1.0 (5e edition) : pas de chiffre, de '-' ni de '.' en tete.
        return c == ':' || (c >= 'A' && c <= 'Z') || c == '_' || (c >= 'a' && c <= 'z')
                || (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF)
                || (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D)
                || (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF)
                || (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF);
    }

    private static boolean isNameChar(int c) {
        return isNameStartChar(c) || c == '-' || c == '.' || (c >= '0' && c <= '9') || c == 0xB7
                || (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <= 0x2040);
    }

    private static boolean isTagDelimiter(char c) {
        return c == '>' || c == '/' || isXmlWhitespace(c);
    }

    static boolean isXmlWhitespace(char c) {
        // Les 4 seuls blancs XML : pas Character.isWhitespace, qui en accepte bien d'autres.
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static int skipWhitespace(String s, int j) {
        while (j < s.length() && isXmlWhitespace(s.charAt(j))) {
            j++;
        }
        return j;
    }

    private static int firstNonBlank(String doc, int i) {
        return skipWhitespace(doc, i);
    }

    static int lineOf(String doc, int index) {
        int line = 1;
        for (int k = 0; k < index; k++) {
            if (doc.charAt(k) == '\n') {
                line++;
            }
        }
        return line;
    }

    static String summary(Element root) {
        int[] counts = new int[3]; // elements, attributs, profondeur maximale
        Set<String> reserved = new HashSet<>();
        walk(root, 1, counts, reserved);
        String r = reserved.isEmpty() ? "" : " reserve=" + String.join(",", reserved.stream().sorted().toList());
        return "racine=" + root.name + " elements=" + counts[0] + " attributs=" + counts[1] + " profondeur=" + counts[2] + r;
    }

    private static void walk(Element e, int depth, int[] counts, Set<String> reserved) {
        counts[0]++;
        counts[1] += e.attributes.size();
        counts[2] = Math.max(counts[2], depth);
        // Grammaticalement permis, mais reserve aux normes XML (0.2.5 S3) : tout nom qui commence par x-m-l.
        for (String n : names(e)) {
            if (n.toLowerCase().startsWith("xml")) {
                reserved.add(n);
            }
        }
        for (Element c : e.children) {
            walk(c, depth + 1, counts, reserved);
        }
    }

    private static List<String> names(Element e) {
        List<String> all = new ArrayList<>(e.attributes.keySet());
        all.add(e.name);
        return all;
    }

    static void plan(Element e, int depth) {
        StringBuilder line = new StringBuilder("  ".repeat(depth)).append(e.name);
        e.attributes.forEach((k, v) -> line.append(' ').append(k).append('=').append(v));
        String text = e.text.toString().strip();
        if (!text.isEmpty()) {
            line.append(" : ").append(text);
        }
        // <stock/> et <stock></stock> sont le MEME element vide : seule l'ecriture change (0.2.3 S5).
        if (e.children.isEmpty() && e.text.isEmpty()) {
            line.append(" (vide)");
        }
        System.out.println(line);
        for (Element c : e.children) {
            plan(c, depth + 1);
        }
    }

    static void relations(Element e, String path) {
        // Parent et freres viennent de la pile : le parent = l'element ouvert au moment de l'ouverture.
        List<Element> siblings = e.parent == null ? List.of(e) : e.parent.children;
        int at = siblings.indexOf(e);
        String before = at > 0 ? siblings.get(at - 1).name : "-";
        String after = at < siblings.size() - 1 ? siblings.get(at + 1).name : "-";
        System.out.println("  " + path + " : parent=" + (e.parent == null ? "-" : e.parent.name) + " precedent=" + before
                + " suivant=" + after + " enfants=" + e.children.size());
        for (Element c : e.children) {
            int rank = 1;
            for (Element other : e.children) {
                if (other == c) {
                    break;
                }
                if (other.name.equals(c.name)) {
                    rank++;
                }
            }
            relations(c, path + "/" + c.name + "[" + rank + "]");
        }
    }

    static String convert(String flat) {
        // Choix de modelisation (0.2.4 S6) : identifiant et langue decrivent la fiche -> attributs ;
        // titre, auteur, annee sont le contenu -> elements enfants.
        String[] v = flat.split(";");
        Map<String, String> f = new LinkedHashMap<>();
        for (int k = 0; k < Data.FIELDS.size(); k++) {
            f.put(Data.FIELDS.get(k), v[k]);
        }
        return "<livre id=\"" + f.get("id") + "\" langue=\"" + f.get("langue") + "\">"
                + "<titre>" + f.get("titre") + "</titre><auteur>" + f.get("auteur") + "</auteur><annee>" + f.get("annee") + "</annee>"
                + "</livre>";
    }
}
