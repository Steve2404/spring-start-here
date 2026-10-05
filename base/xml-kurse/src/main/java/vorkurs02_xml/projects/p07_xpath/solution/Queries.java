package vorkurs02_xml.projects.p07_xpath.solution;

import vorkurs02_xml.projects.p07_xpath.Data;
import xmlkit.XmlKit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du projet 7 : le moteur de requetes (0.2.16). D'abord ecrire les bonnes expressions XPath,
 * puis evaluer soi-meme un sous-ensemble de XPath sur son propre arbre.
 */
public class Queries {

    static final String DOC = Data.COMPANY;

    public static void main(String[] args) {
        // Chemin absolu avec '/' : seulement les employes DIRECTS d'IT (pas ceux de Dev).
        q("Q1", list(XmlKit.select(DOC, "/company/department[@name='IT']/employee/name/text()")));
        // [2] s'applique au step employee : le 2e employe de CHAQUE departement parent.
        q("Q2", list(XmlKit.select(DOC, "//department/employee[2]/name/text()")));
        // Les parentheses construisent d'abord LA liste globale, puis [2] y choisit.
        q("Q3", list(XmlKit.select(DOC, "(//employee)[2]/name/text()")));
        // '//' sous le departement descend aussi dans Dev.
        q("Q4", XmlKit.value(DOC, "sum(//department[@name='IT']//employee/salary)"));
        // Comparaison de node-sets : vraie si UNE valeur de gauche egale UNE valeur de droite.
        q("Q5", list(XmlKit.select(DOC, "//employee[@role='manager'][not(//employee/@reportsTo = @id)]/name/text()")));
        // ancestor est un axe INVERSE : [1] y est le plus PROCHE.
        q("Q6", XmlKit.value(DOC, "//employee[@id='e5']/ancestor::department[1]/@name"));
        q("Q7", XmlKit.value(DOC, "normalize-space(//employee[@id='e7']/name)"));
        // Le prefixe h n'est PAS celui du document (r) : c'est l'URI qui compte, liee par NOUS.
        q("Q8", list(XmlKit.select(DOC, "//h:review[h:score >= 4]/@employee", Map.of("h", "urn:hr:reviews"))));
        // Piege : sans prefixe, un nom XPath 1.0 designe un element SANS namespace -> rien.
        q("Q9", XmlKit.value(DOC, "count(//review)"));
        q("Q10", XmlKit.value(DOC, "count(//employee)") + " " + XmlKit.value(DOC, "count(//department)"));
        q("Q11", list(XmlKit.select(DOC, "//employee[salary > sum(//salary) div count(//salary)]/name/text()")));

        Element root = parse(DOC);
        for (int i = 0; i < Data.PATHS.size(); i++) {
            String path = Data.PATHS.get(i);
            List<String> mine = evaluate(root, path).stream().map(Queries::stringValue).toList();
            List<String> parser = new ArrayList<>();
            int count = Integer.parseInt(XmlKit.value(DOC, "count(" + path + ")"));
            for (int k = 1; k <= count; k++) {
                parser.add(XmlKit.value(DOC, "normalize-space((" + path + ")[" + k + "])"));
            }
            System.out.println(String.format("M%02d", i + 1) + " " + mine.size() + " : " + String.join(" | ", mine)
                    + " | parseur " + (mine.equals(parser) ? "identique" : "DIFFERENT " + parser));
        }
    }

    static void q(String id, String result) {
        System.out.println(id + " " + result);
    }

    static String list(List<String> nodes) {
        return String.join(", ", nodes);
    }

    // ---------- l'arbre maison (elements, attributs, texte) ----------

    static final class Element {
        final String name;
        final Element parent;
        final Map<String, String> attributes = new LinkedHashMap<>();
        final List<Object> content = new ArrayList<>(); // des Element et des String, dans l'ordre

        Element(String name, Element parent) {
            this.name = name;
            this.parent = parent;
        }

        List<Element> children() {
            List<Element> out = new ArrayList<>();
            for (Object o : content) {
                if (o instanceof Element e) {
                    out.add(e);
                }
            }
            return out;
        }
    }

    private static final Pattern ATTR = Pattern.compile("([^\\s=]+)\\s*=\\s*\"([^\"]*)\"");

    static Element parse(String text) {
        Element doc = new Element("/", null); // le noeud document, parent de la racine
        Element current = doc;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != '<') {
                int next = text.indexOf('<', i);
                next = next < 0 ? text.length() : next;
                if (current != doc) {
                    current.content.add(text.substring(i, next));
                }
                i = next;
            } else if (text.startsWith("<?", i)) {
                i = text.indexOf("?>", i) + 2;
            } else if (text.startsWith("</", i)) {
                current = current.parent;
                i = text.indexOf('>', i) + 1;
            } else {
                int end = text.indexOf('>', i);
                boolean empty = text.charAt(end - 1) == '/';
                String[] parts = text.substring(i + 1, empty ? end - 1 : end).strip().split("\\s+", 2);
                Element e = new Element(parts[0], current);
                if (parts.length > 1) {
                    Matcher m = ATTR.matcher(parts[1]);
                    while (m.find()) {
                        e.attributes.put(m.group(1), m.group(2));
                    }
                }
                current.content.add(e);
                if (!empty) {
                    current = e;
                }
                i = end + 1;
            }
        }
        return doc;
    }

    /** La valeur texte d'un element (tous ses textes descendants), blancs normalises comme normalize-space. */
    static String stringValue(Element e) {
        StringBuilder sb = new StringBuilder();
        collect(e, sb);
        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    private static void collect(Element e, StringBuilder sb) {
        for (Object o : e.content) {
            if (o instanceof Element c) {
                collect(c, sb);
            } else {
                sb.append(o);
            }
        }
    }

    // ---------- le mini moteur ----------

    record Predicate(String kind, String name, String value, int position) {
    }

    record Step(boolean descendant, String test, List<Predicate> predicates) {
    }

    static List<Step> steps(String path) {
        // "//x" = un step x precede d'un segment vide : on le marque "descendant".
        List<Step> steps = new ArrayList<>();
        boolean descendant = false;
        for (String segment : path.substring(1).split("/", -1)) {
            if (segment.isEmpty()) {
                descendant = true;
                continue;
            }
            int bracket = segment.indexOf('[');
            String test = bracket < 0 ? segment : segment.substring(0, bracket);
            List<Predicate> preds = new ArrayList<>();
            Matcher m = Pattern.compile("\\[([^\\]]*)]").matcher(segment);
            while (m.find()) {
                preds.add(predicate(m.group(1)));
            }
            steps.add(new Step(descendant, test, preds));
            descendant = false;
        }
        return steps;
    }

    static Predicate predicate(String p) {
        if (p.matches("[0-9]+")) {
            return new Predicate("POSITION", null, null, Integer.parseInt(p));
        }
        if (p.equals("last()")) {
            return new Predicate("LAST", null, null, 0);
        }
        Matcher m = Pattern.compile("(@?)([\\w:-]+)(?:='([^']*)')?").matcher(p);
        if (!m.matches()) {
            throw new IllegalArgumentException("predicat non gere : " + p);
        }
        String kind = (m.group(1).isEmpty() ? "CHILD" : "ATTRIBUTE") + (m.group(3) == null ? "" : "_EQUALS");
        return new Predicate(kind, m.group(2), m.group(3), 0);
    }

    static List<Element> evaluate(Element document, String path) {
        List<Element> context = List.of(document);
        for (Step step : steps(path)) {
            // Un ensemble ordonne SANS doublon : deux contextes peuvent atteindre le meme noeud.
            Set<Element> next = new LinkedHashSet<>();
            for (Element c : context) {
                List<Element> starts = step.descendant() ? descendantsOrSelf(c) : List.of(c);
                for (Element s : starts) {
                    // Les predicats s'appliquent a la liste des enfants qui passent le test, PAR parent :
                    // c'est pourquoi //employee[1] rend le premier employe de CHAQUE departement.
                    List<Element> matching = new ArrayList<>();
                    for (Element child : s.children()) {
                        if (step.test().equals("*") || step.test().equals(child.name)) {
                            matching.add(child);
                        }
                    }
                    for (Predicate p : step.predicates()) {
                        matching = filter(matching, p);
                    }
                    next.addAll(matching);
                }
            }
            context = documentOrder(document, next);
        }
        return context;
    }

    static List<Element> filter(List<Element> nodes, Predicate p) {
        List<Element> out = new ArrayList<>();
        for (int k = 0; k < nodes.size(); k++) {
            Element e = nodes.get(k);
            boolean keep = switch (p.kind()) {
                case "POSITION" -> k + 1 == p.position();
                case "LAST" -> k == nodes.size() - 1;
                case "ATTRIBUTE" -> e.attributes.containsKey(p.name());
                case "ATTRIBUTE_EQUALS" -> p.value().equals(e.attributes.get(p.name()));
                case "CHILD" -> e.children().stream().anyMatch(c -> c.name.equals(p.name()));
                case "CHILD_EQUALS" -> e.children().stream().anyMatch(c -> c.name.equals(p.name()) && stringValue(c).equals(p.value()));
                default -> throw new IllegalStateException(p.kind());
            };
            if (keep) {
                out.add(e);
            }
        }
        return out;
    }

    static List<Element> descendantsOrSelf(Element e) {
        List<Element> out = new ArrayList<>();
        out.add(e);
        for (Element c : e.children()) {
            out.addAll(descendantsOrSelf(c));
        }
        return out;
    }

    static List<Element> documentOrder(Element document, Set<Element> nodes) {
        // Un resultat XPath est toujours rendu dans l'ORDRE DU DOCUMENT.
        return descendantsOrSelf(document).stream().filter(nodes::contains).toList();
    }
}
