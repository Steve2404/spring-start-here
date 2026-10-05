package vorkurs02_xml.projects.p03_tree.solution;

import vorkurs02_xml.projects.p03_tree.Data;
import xmlkit.XmlKit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Corrige du projet 3 : l'arbre maison (0.2.9 -> 0.2.10). Un modele de noeuds a la DOM, construit
 * a la main, verifie noeud par noeud contre le parseur ; puis la chaine bien forme -> valide -> metier.
 */
public class Tree {

    enum Type { DOCUMENT, ELEMENT, TEXT, COMMENT, PI }

    /**
     * Un noeud. Les attributs sont rattaches a l'element mais NE SONT PAS des enfants (0.2.10 S6) :
     * ils vivent dans leur propre Map, a cote de la liste des enfants.
     */
    static final class Node {
        final Type type;
        final String name;   // nom de l'element ou cible de la PI
        final StringBuilder value = new StringBuilder(); // texte, commentaire, donnees de PI
        final Node parent;
        final List<Node> children = new ArrayList<>();
        final Map<String, String> attributes = new LinkedHashMap<>();

        Node(Type type, String name, Node parent) {
            this.type = type;
            this.name = name;
            this.parent = parent;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        /** Le meme rendu que XmlKit.select : on peut comparer les listes element par element. */
        String render() {
            return switch (type) {
                case DOCUMENT -> "/";
                case ELEMENT -> name;
                case TEXT -> "\"" + value + "\"";
                case COMMENT -> "<!--" + value + "-->";
                case PI -> "<?" + name + " " + value + "?>";
            };
        }
    }

    public static void main(String[] args) {
        Node doc = parse(Data.ORDER);
        System.out.println("ARBRE :");
        dump(doc, 0);

        Node root = rootOf(doc);
        Node line1 = child(root, "ligne", 1);
        Node line2 = child(root, "ligne", 2);
        compare("Q1", "/node()", doc.children);
        compare("Q2", "/commande/node()", root.children);
        compare("Q3", "/commande/note/node()", child(root, "note", 1).children);
        compare("Q4", "/commande/ligne[2]/prix/ancestor::node()", ancestors(child(line2, "prix", 1)));
        compare("Q5", "/commande/ligne[1]/following-sibling::node()", followingSiblings(line1));
        compare("Q6", "/commande//text()", descendants(root, n -> n.type == Type.TEXT));
        compare("Q7", "/commande/ligne[1]/@*", attributeNodes(line1));
        // Un noeud enfant n'est pas forcement un element enfant ; un attribut n'est ni l'un ni l'autre.
        System.out.println("COMPTES commande : noeuds enfants=" + root.children.size()
                + " elements enfants=" + root.children.stream().filter(n -> n.type == Type.ELEMENT).count()
                + " attributs=" + root.attributes.size());

        for (Data.Submission s : Data.ORDERS) {
            System.out.println(s.id() + " " + pipeline(s.text()));
        }
        for (String id : List.of("O01", "O07")) {
            String text = Data.ORDERS.stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow().text();
            String mine = rootOf(parse(text)).attributes.getOrDefault("devise", "absent");
            System.out.println("DEFAUT " + id + " devise : arbre maison " + mine + " | parseur " + XmlKit.value(text, "string(/commande/@devise)"));
        }
    }

    // ---------- construction ----------

    static Node parse(String text) {
        Node doc = new Node(Type.DOCUMENT, null, null);
        Node current = doc;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != '<') {
                int next = text.indexOf('<', i);
                int stop = next < 0 ? text.length() : next;
                // Au niveau du document, les blancs ne sont pas des noeuds ; dans un element, si.
                if (current != doc) {
                    textNode(current).value.append(decode(text.substring(i, stop)));
                }
                i = stop;
            } else if (text.startsWith("<?xml ", i)) {
                // La declaration n'est PAS un noeud : ni une PI, ni un enfant du document.
                i = text.indexOf("?>", i) + 2;
            } else if (text.startsWith("<!DOCTYPE", i)) {
                int bracket = text.indexOf('[', i);
                int close = text.indexOf('>', i);
                i = bracket >= 0 && bracket < close ? text.indexOf("]>", bracket) + 2 : close + 1;
            } else if (text.startsWith("<!--", i)) {
                int end = text.indexOf("-->", i);
                new Node(Type.COMMENT, null, current).value.append(text, i + 4, end);
                i = end + 3;
            } else if (text.startsWith("<?", i)) {
                int end = text.indexOf("?>", i);
                String[] parts = text.substring(i + 2, end).split("[ \t\r\n]+", 2);
                new Node(Type.PI, parts[0], current).value.append(parts.length > 1 ? parts[1] : "");
                i = end + 2;
            } else if (text.startsWith("</", i)) {
                current = current.parent;
                i = text.indexOf('>', i) + 1;
            } else {
                int end = text.indexOf('>', i);
                boolean empty = text.charAt(end - 1) == '/';
                String inside = text.substring(i + 1, empty ? end - 1 : end).strip();
                String[] parts = inside.split("[ \t\r\n]+", 2);
                Node e = new Node(Type.ELEMENT, parts[0], current);
                if (parts.length > 1) {
                    var m = Pattern.compile("([^\\s=]+)\\s*=\\s*([\"'])(.*?)\\2").matcher(parts[1]);
                    while (m.find()) {
                        e.attributes.put(m.group(1), decode(m.group(3)));
                    }
                }
                if (!empty) {
                    current = e;
                }
                i = end + 1;
            }
        }
        return doc;
    }

    /** Deux morceaux de texte qui se suivent forment UN seul noeud texte (pas de noeud vide entre eux). */
    private static Node textNode(Node parent) {
        Node last = parent.children.isEmpty() ? null : parent.children.get(parent.children.size() - 1);
        return last != null && last.type == Type.TEXT ? last : new Node(Type.TEXT, null, parent);
    }

    private static String decode(String s) {
        return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&");
    }

    // ---------- navigation ----------

    static Node rootOf(Node doc) {
        // Le document n'est PAS la racine : la racine est son unique enfant ELEMENT (0.2.10 S2).
        return doc.children.stream().filter(n -> n.type == Type.ELEMENT).findFirst().orElseThrow();
    }

    static Node child(Node parent, String name, int rank) {
        return parent.children.stream().filter(n -> n.type == Type.ELEMENT && n.name.equals(name)).skip(rank - 1L).findFirst().orElseThrow();
    }

    static List<Node> ancestors(Node n) {
        // Les ancetres dans l'ordre du DOCUMENT : du plus haut (le document) au parent direct.
        List<Node> out = new ArrayList<>();
        for (Node p = n.parent; p != null; p = p.parent) {
            out.add(0, p);
        }
        return out;
    }

    static List<Node> followingSiblings(Node n) {
        // Freres = MEME parent direct ; et ce sont des noeuds, pas seulement des elements.
        List<Node> siblings = n.parent.children;
        return siblings.subList(siblings.indexOf(n) + 1, siblings.size());
    }

    static List<Node> descendants(Node n, Predicate<Node> keep) {
        List<Node> out = new ArrayList<>();
        for (Node c : n.children) {
            if (keep.test(c)) {
                out.add(c);
            }
            out.addAll(descendants(c, keep));
        }
        return out;
    }

    static List<String> attributeNodes(Node e) {
        List<String> out = new ArrayList<>();
        e.attributes.forEach((k, v) -> out.add("@" + k + "=" + v));
        return out;
    }

    // ---------- affichage ----------

    static void dump(Node n, int depth) {
        StringBuilder line = new StringBuilder("  ".repeat(depth + 1)).append(n.type).append(' ').append(show(n.render()));
        n.attributes.forEach((k, v) -> line.append(" @").append(k).append('=').append(v));
        System.out.println(line);
        for (Node c : n.children) {
            dump(c, depth + 1);
        }
    }

    static void compare(String id, String xpath, List<?> mine) {
        List<String> rendered = mine.stream().map(o -> o instanceof Node n ? n.render() : o.toString()).toList();
        List<String> parser = XmlKit.select(Data.ORDER, xpath);
        System.out.println(id + " " + rendered.size() + " : " + show(String.join(" · ", rendered)) + " | parseur " + (rendered.equals(parser) ? "identique" : "DIFFERENT"));
    }

    static String show(String s) {
        return s.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    // ---------- bien forme -> valide -> metier ----------

    static String pipeline(String text) {
        // Chaque couche suppose la precedente : on s'arrete a la premiere qui echoue.
        String wf = XmlKit.wellFormed(text);
        if (!wf.equals("OK")) {
            // Une DTD ne peut pas reparer un document mal forme : il n'y a meme pas d'arbre a valider.
            return "NON_BIEN_FORME " + wf + " [WFC]";
        }
        String head;
        if (!text.contains("<!DOCTYPE")) {
            head = "BIEN_FORME sans DTD";
        } else {
            String v = XmlKit.validateDtd(text, Map.of());
            if (!v.equals("VALID")) {
                return v.replace("INVALID", "INVALIDE") + " [VC]";
            }
            head = "VALIDE";
        }
        return head + " | metier " + business(rootOf(parse(text)));
    }

    static String business(Node order) {
        // Valide ne veut pas dire correct : la DTD dit "#PCDATA", elle accepte "-2" ou "gratuit".
        BigDecimal total = BigDecimal.ZERO;
        int index = 0;
        for (Node line : order.children) {
            if (line.type != Type.ELEMENT) {
                continue;
            }
            index++;
            String q = textOf(child(line, "quantite", 1));
            String p = textOf(child(line, "prix", 1));
            if (!q.matches("[1-9][0-9]*")) {
                return "KO ligne " + index + " quantite=" + q;
            }
            if (!p.matches("[0-9]+(\\.[0-9]+)?")) {
                return "KO ligne " + index + " prix=" + p;
            }
            total = total.add(new BigDecimal(p).multiply(new BigDecimal(q)));
        }
        return "OK total=" + total.setScale(2, RoundingMode.HALF_UP);
    }

    static String textOf(Node e) {
        return descendants(e, n -> n.type == Type.TEXT).stream().map(n -> n.value.toString()).reduce("", String::concat);
    }
}
