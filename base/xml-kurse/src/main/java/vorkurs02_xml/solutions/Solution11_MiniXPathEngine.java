package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige de l'exercice 11. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise11_MiniXPathEngine.
 */
public class Solution11_MiniXPathEngine {

    public enum PredicateKind { POSITION, LAST, HAS_ATTRIBUTE, ATTRIBUTE_EQUALS, HAS_CHILD, CHILD_EQUALS }

    public record Predicate(PredicateKind kind, String name, String value, int position) {
    }

    public record Step(boolean descendant, String test, List<Predicate> predicates) {
    }

    private static final Pattern PREDICATE = Pattern.compile("\\[([^\\]]*)]");

    public static List<Step> parse(String path) {
        // On coupe sur '/' en comptant les crochets (un predicat pourrait contenir '/') ; un segment
        // vide entre deux '/' signifie qu'on vient de lire "//" : le step suivant est "descendant".
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Seuls les chemins absolus sont geres : " + path);
        }
        List<Step> steps = new ArrayList<>();
        int i = 1;
        boolean descendant = false;
        while (i <= path.length()) {
            int depth = 0;
            int j = i;
            while (j < path.length() && (path.charAt(j) != '/' || depth > 0)) {
                if (path.charAt(j) == '[') {
                    depth++;
                } else if (path.charAt(j) == ']') {
                    depth--;
                }
                j++;
            }
            String segment = path.substring(i, j);
            if (segment.isEmpty()) {
                descendant = true;
            } else {
                steps.add(parseStep(segment, descendant));
                descendant = false;
            }
            i = j + 1;
        }
        return steps;
    }

    private static Step parseStep(String segment, boolean descendant) {
        int bracket = segment.indexOf('[');
        String test = bracket < 0 ? segment : segment.substring(0, bracket);
        List<Predicate> predicates = new ArrayList<>();
        if (bracket >= 0) {
            Matcher m = PREDICATE.matcher(segment.substring(bracket));
            while (m.find()) {
                predicates.add(parsePredicate(m.group(1)));
            }
        }
        return new Step(descendant, test, predicates);
    }

    public static Predicate parsePredicate(String inside) {
        // Du plus specifique au plus general : un nombre, last(), puis les formes avec '='
        // (attribut ou enfant), enfin les simples tests d'existence.
        String s = inside.strip();
        if (s.matches("\\d+")) {
            return new Predicate(PredicateKind.POSITION, null, null, Integer.parseInt(s));
        }
        if (s.equals("last()")) {
            return new Predicate(PredicateKind.LAST, null, null, 0);
        }
        Matcher eq = Pattern.compile("(@?)([\\w.-]+)\\s*=\\s*'([^']*)'").matcher(s);
        if (eq.matches()) {
            PredicateKind kind = eq.group(1).isEmpty() ? PredicateKind.CHILD_EQUALS : PredicateKind.ATTRIBUTE_EQUALS;
            return new Predicate(kind, eq.group(2), eq.group(3), 0);
        }
        if (s.startsWith("@")) {
            return new Predicate(PredicateKind.HAS_ATTRIBUTE, s.substring(1), null, 0);
        }
        return new Predicate(PredicateKind.HAS_CHILD, s, null, 0);
    }

    public static List<Node> candidates(Node context, String test) {
        // Le "node test" decide QUELS voisins du contexte sont candidats : parent, un attribut,
        // les noeuds texte enfants, ou les elements enfants (par nom ou '*').
        List<Node> result = new ArrayList<>();
        if (test.equals("..")) {
            if (context.getParentNode() != null) {
                result.add(context.getParentNode());
            }
        } else if (test.startsWith("@")) {
            if (context instanceof Element e && e.hasAttribute(test.substring(1))) {
                result.add(e.getAttributeNode(test.substring(1)));
            }
        } else {
            for (Node c = context.getFirstChild(); c != null; c = c.getNextSibling()) {
                boolean match = test.equals("text()")
                        ? c.getNodeType() == Node.TEXT_NODE
                        : c.getNodeType() == Node.ELEMENT_NODE && (test.equals("*") || c.getNodeName().equals(test));
                if (match) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    public static List<Node> applyPredicate(List<Node> candidates, Predicate p) {
        // Les positions se comptent dans la liste de CE contexte (1 = premier) : c'est pourquoi
        // //book[1] rend le 1er livre de CHAQUE parent. Les predicats s'enchainent a gauche -> droite.
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Node n = candidates.get(i);
            boolean keep = switch (p.kind()) {
                case POSITION -> i + 1 == p.position();
                case LAST -> i == candidates.size() - 1;
                case HAS_ATTRIBUTE -> n instanceof Element e && e.hasAttribute(p.name());
                case ATTRIBUTE_EQUALS -> n instanceof Element e && e.hasAttribute(p.name())
                        && e.getAttribute(p.name()).equals(p.value());
                case HAS_CHILD -> !candidates(n, p.name()).isEmpty();
                // XPath 1.0 : vrai si AU MOINS UN enfant de ce nom a cette valeur texte.
                case CHILD_EQUALS -> candidates(n, p.name()).stream().anyMatch(c -> c.getTextContent().equals(p.value()));
            };
            if (keep) {
                result.add(n);
            }
        }
        return result;
    }

    public static List<Node> descendantsOrSelf(List<Node> contexts) {
        // "//" = /descendant-or-self::node()/ : chaque contexte PLUS tous ses descendants
        // (ceux qui peuvent avoir des enfants suffisent : le document et les elements).
        List<Node> out = new ArrayList<>();
        for (Node c : contexts) {
            collect(c, out);
        }
        return sortDocumentOrder(out);
    }

    private static void collect(Node n, List<Node> out) {
        out.add(n);
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                collect(c, out);
            }
        }
    }

    public static List<Node> sortDocumentOrder(Collection<Node> nodes) {
        // Un node-set n'a pas de doublons et se lit dans l'ordre du document :
        // LinkedHashSet retire les doublons, compareDocumentPosition donne l'ordre.
        List<Node> distinct = new ArrayList<>(new LinkedHashSet<>(nodes));
        distinct.sort((a, b) -> {
            if (a == b) {
                return 0;
            }
            return (a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING) != 0 ? -1 : 1;
        });
        return distinct;
    }

    public static List<Node> evaluate(Document doc, String path) {
        // Chaque step transforme l'ensemble de contextes en un nouvel ensemble : descendre
        // si "//", calculer les candidats PAR contexte, filtrer, puis fusionner dans l'ordre du document.
        List<Node> contexts = List.of(doc);
        for (Step step : parse(path)) {
            if (step.descendant()) {
                contexts = descendantsOrSelf(contexts);
            }
            List<Node> next = new ArrayList<>();
            for (Node ctx : contexts) {
                List<Node> cands = candidates(ctx, step.test());
                for (Predicate p : step.predicates()) {
                    cands = applyPredicate(cands, p);
                }
                next.addAll(cands);
            }
            contexts = sortDocumentOrder(next);
        }
        return contexts;
    }
}
