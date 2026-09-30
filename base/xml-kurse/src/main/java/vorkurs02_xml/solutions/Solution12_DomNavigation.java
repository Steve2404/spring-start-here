package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Corrige de l'exercice 12. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise12_DomNavigation.
 */
public class Solution12_DomNavigation {

    public record Track(String id, String title, int seconds, List<String> tags) {
    }

    public static Map<String, Integer> countNodeTypes(Node root) {
        // Parcours recursif de TOUS les enfants (pas seulement les elements) : c'est la seule
        // facon de "voir" les noeuds texte d'indentation que le DOM a vraiment crees.
        Map<String, Integer> counts = new TreeMap<>();
        count(root, counts);
        return counts;
    }

    private static void count(Node n, Map<String, Integer> counts) {
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
            counts.merge(typeName(c.getNodeType()), 1, Integer::sum);
            count(c, counts);
        }
    }

    private static String typeName(short type) {
        return switch (type) {
            case Node.ELEMENT_NODE -> "ELEMENT";
            case Node.TEXT_NODE -> "TEXT";
            case Node.CDATA_SECTION_NODE -> "CDATA";
            case Node.COMMENT_NODE -> "COMMENT";
            case Node.PROCESSING_INSTRUCTION_NODE -> "PI";
            default -> "OTHER";
        };
    }

    public static List<Element> childElements(Element parent) {
        // getChildNodes() melange elements, textes blancs, commentaires : on filtre ELEMENT_NODE.
        List<Element> result = new ArrayList<>();
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element e) {
                result.add(e);
            }
        }
        return result;
    }

    public static Optional<Element> firstChildElement(Element parent, String name) {
        // getFirstChild() rend souvent le "\n    " d'indentation : on cherche le premier ELEMENT du bon nom.
        return childElements(parent).stream().filter(e -> e.getTagName().equals(name)).findFirst();
    }

    public static String ownText(Element element) {
        // getTextContent() concatene TOUT le sous-arbre ; ici seulement les textes DIRECTS
        // (TEXT et CDATA : CDATASection est une sous-interface de Text).
        StringBuilder out = new StringBuilder();
        for (Node c = element.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
                out.append(c.getNodeValue());
            }
        }
        return out.toString();
    }

    public static String pathOf(Node node) {
        // On remonte les parents ; la position [n] compte seulement les freres de MEME nom
        // (exactement comme un step XPath "nom[n]"). Un attribut n'a pas de parent : getOwnerElement.
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return pathOf(((org.w3c.dom.Attr) node).getOwnerElement()) + "/@" + node.getNodeName();
        }
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return "";
        }
        int position = 1;
        for (Node s = node.getPreviousSibling(); s != null; s = s.getPreviousSibling()) {
            if (s.getNodeType() == Node.ELEMENT_NODE && s.getNodeName().equals(node.getNodeName())) {
                position++;
            }
        }
        return pathOf(node.getParentNode()) + "/" + node.getNodeName() + "[" + position + "]";
    }

    public static int removeChildrenNamed(Element parent, String name) {
        // PIEGE : getChildNodes() est une liste VIVANTE ; supprimer l'element i decale les suivants
        // et i++ saute le voisin. On lit getNextSibling() AVANT de supprimer.
        int removed = 0;
        Node c = parent.getFirstChild();
        while (c != null) {
            Node next = c.getNextSibling();
            if (c.getNodeType() == Node.ELEMENT_NODE && c.getNodeName().equals(name)) {
                parent.removeChild(c);
                removed++;
            }
            c = next;
        }
        return removed;
    }

    public static int parseDuration(String mmss) {
        // "5:37" -> 5 * 60 + 37 ; strip() car le texte vient d'un document indente.
        String[] parts = mmss.strip().split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public static List<Track> readTracks(Document doc) {
        // On assemble les boites : enfants elements, premier enfant d'un nom, et textContent
        // seulement quand on VEUT tout le sous-arbre (le titre "Blue in Green").
        List<Track> tracks = new ArrayList<>();
        for (Element track : childElements(doc.getDocumentElement())) {
            String title = firstChildElement(track, "title").map(Element::getTextContent).orElse("");
            int seconds = firstChildElement(track, "duration").map(d -> parseDuration(d.getTextContent())).orElse(0);
            List<String> tags = firstChildElement(track, "tags")
                    .map(t -> childElements(t).stream().map(Element::getTextContent).toList())
                    .orElse(List.of());
            tracks.add(new Track(track.getAttribute("id"), title, seconds, tags));
        }
        return tracks;
    }
}
