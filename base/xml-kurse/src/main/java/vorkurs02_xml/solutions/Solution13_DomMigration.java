package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Corrige de l'exercice 13. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise13_DomMigration.
 */
public class Solution13_DomMigration {

    public static int renameAll(Document doc, String oldName, String newName) {
        // getElementsByTagName est VIVANTE selon la spec. Le Xerces du JDK ne saute rien ici
        // (verifie), mais removeChild, lui, saute (exercice 12) : figer la liste d'abord evite de
        // dependre de ce detail d'implementation.
        NodeList live = doc.getElementsByTagName(oldName);
        List<Node> frozen = new ArrayList<>();
        for (int i = 0; i < live.getLength(); i++) {
            frozen.add(live.item(i));
        }
        for (Node n : frozen) {
            doc.renameNode(n, null, newName);
        }
        return frozen.size();
    }

    public static boolean moveAttributeToFirstChild(Element element, String attribute) {
        // insertBefore(nouveau, getFirstChild()) place l'enfant en TETE (et marche aussi si
        // l'element n'a aucun enfant : getFirstChild() == null -> ajout a la fin).
        if (!element.hasAttribute(attribute)) {
            return false;
        }
        Element child = element.getOwnerDocument().createElement(attribute);
        child.setTextContent(element.getAttribute(attribute));
        element.insertBefore(child, element.getFirstChild());
        element.removeAttribute(attribute);
        return true;
    }

    public static int removeWhitespaceAndComments(Node node) {
        // On retient le frere SUIVANT avant toute suppression (un noeud detache n'a plus de
        // frere), puis on descend dans les elements.
        int removed = 0;
        Node c = node.getFirstChild();
        while (c != null) {
            Node next = c.getNextSibling();
            boolean blankText = c.getNodeType() == Node.TEXT_NODE && c.getNodeValue().isBlank();
            if (blankText || c.getNodeType() == Node.COMMENT_NODE) {
                node.removeChild(c);
                removed++;
            } else if (c.getNodeType() == Node.ELEMENT_NODE) {
                removed += removeWhitespaceAndComments(c);
            }
            c = next;
        }
        return removed;
    }

    public static void normalizeText(Element leaf) {
        // setTextContent REMPLACE tous les enfants par un seul noeud texte (ou aucun si "") :
        // a reserver aux feuilles.
        leaf.setTextContent(leaf.getTextContent().strip().replaceAll("\\s+", " "));
    }

    public static int removeEmptyElements(Element parent) {
        // Du bas vers le haut : on nettoie d'abord les enfants, puis on juge l'element.
        // "Vide" = aucun enfant et aucun attribut (<phone/>, <email></email>).
        int removed = 0;
        Node c = parent.getFirstChild();
        while (c != null) {
            Node next = c.getNextSibling();
            if (c instanceof Element e) {
                removed += removeEmptyElements(e);
                if (!e.hasChildNodes() && !e.hasAttributes()) {
                    parent.removeChild(e);
                    removed++;
                }
            }
            c = next;
        }
        return removed;
    }

    public static void sortChildElementsBy(Element parent, String attribute) {
        // appendChild d'un noeud DEJA dans l'arbre le DEPLACE (pas de copie) : re-ajouter les
        // enfants tries suffit a les reordonner.
        List<Element> children = new ArrayList<>();
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element e) {
                children.add(e);
            }
        }
        children.sort(Comparator.comparing(e -> e.getAttribute(attribute)));
        for (Element e : children) {
            parent.appendChild(e);
        }
    }

    public static String serialize(Document doc, boolean indent) throws Exception {
        // Tant qu'on n'a pas serialise, les modifications n'existent qu'en memoire (0.2.17 S7).
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        StringWriter out = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }

    public static Document migrate(Document doc) {
        // L'ORDRE compte : nettoyer d'abord (blancs, commentaires), normaliser le texte, et
        // seulement ensuite supprimer ce qui est devenu vide (" " -> "" -> element supprime).
        removeWhitespaceAndComments(doc);
        renameAll(doc, "customers", "clients");
        renameAll(doc, "customer", "client");
        Element root = doc.getDocumentElement();
        root.setAttribute("version", "2");
        for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
            Element client = (Element) c;
            moveAttributeToFirstChild(client, "vip");
            for (Node f = client.getFirstChild(); f != null; f = f.getNextSibling()) {
                Element field = (Element) f;
                normalizeText(field);
                if (field.getTagName().equals("email")) {
                    field.setTextContent(field.getTextContent().toLowerCase(Locale.ROOT));
                }
            }
        }
        removeEmptyElements(root);
        sortChildElementsBy(root, "id");
        return doc;
    }
}
