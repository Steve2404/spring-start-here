package vorkurs02_xml.projects.p08_dom.solution;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import vorkurs02_xml.projects.p08_dom.Data;

import javax.xml.parsers.DocumentBuilderFactory;
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
 * Corrige du projet 8 : le laboratoire DOM (0.2.17). Lire, naviguer, modifier puis serialiser
 * un arbre DOM, avec les pieges classiques : noeuds blancs, NodeList vivante, namespaces.
 */
public class DomLab {

    public static void main(String[] args) throws Exception {
        Document playlist = parse("playlist.xml", true);
        navigate(playlist);
        liveList(playlist);
        namespaces();
        migrate();
    }

    static Document parse(String file, boolean namespaceAware) throws Exception {
        // La fabrique se CONFIGURE (namespaces...), le builder PARSE : deux roles distincts.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        return factory.newDocumentBuilder().parse(Data.file(file).toFile());
    }

    // ---------- Etapes 1 et 2 : lire et naviguer ----------

    static void navigate(Document doc) {
        // Le Document n'est pas la racine : ses enfants sont la PI, le commentaire et l'element racine.
        List<String> top = new ArrayList<>();
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            top.add(kind(n));
        }
        System.out.println("DOCUMENT " + String.join(" ", top));
        Element root = doc.getDocumentElement();
        ProcessingInstruction pi = (ProcessingInstruction) doc.getFirstChild();
        System.out.println("RACINE " + root.getTagName() + " name=" + root.getAttribute("name")
                + " | PI " + pi.getTarget() + " [" + pi.getData() + "]");

        NodeList tracks = doc.getElementsByTagName("track");
        for (int i = 0; i < tracks.getLength(); i++) {
            Element t = (Element) tracks.item(i);
            // getTextContent concatene TOUT le texte descendant : balises internes et CDATA comprises.
            String title = t.getElementsByTagName("title").item(0).getTextContent();
            String[] d = t.getElementsByTagName("duration").item(0).getTextContent().split(":");
            int seconds = Integer.parseInt(d[0]) * 60 + Integer.parseInt(d[1]);
            List<String> tags = new ArrayList<>();
            NodeList tagNodes = t.getElementsByTagName("tag");
            for (int k = 0; k < tagNodes.getLength(); k++) {
                tags.add(tagNodes.item(k).getTextContent());
            }
            System.out.println("PISTE " + t.getAttribute("id") + " \"" + title + "\" " + seconds + "s noeuds=" + t.getChildNodes().getLength()
                    + " elements=" + elementChildren(t).size() + " tags=" + tags);
        }
        // Un attribut absent : getAttribute rend "" (jamais null), hasAttribute distingue les deux cas.
        Element first = (Element) tracks.item(0);
        System.out.println("ATTRIBUT absent : [" + first.getAttribute("rating") + "] present=" + first.hasAttribute("rating"));
    }

    static String kind(Node n) {
        return switch (n.getNodeType()) {
            case Node.ELEMENT_NODE -> "ELEMENT";
            case Node.TEXT_NODE -> "TEXT";
            case Node.CDATA_SECTION_NODE -> "CDATA";
            case Node.COMMENT_NODE -> "COMMENT";
            case Node.PROCESSING_INSTRUCTION_NODE -> "PI";
            default -> "AUTRE";
        };
    }

    static List<Element> elementChildren(Node parent) {
        List<Element> out = new ArrayList<>();
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element e) {
                out.add(e);
            }
        }
        return out;
    }

    // ---------- Etape 3 : la NodeList vivante ----------

    static void liveList(Document doc) {
        NodeList ads = doc.getElementsByTagName("ad");
        int before = ads.getLength();
        int removed = 0;
        // Piege : la liste est VIVANTE. Retirer item(0) decale tout ; i++ saute alors un element.
        for (int i = 0; i < ads.getLength(); i++) {
            ads.item(i).getParentNode().removeChild(ads.item(i));
            removed++;
        }
        System.out.println("PUBS boucle naive : " + before + " au depart, " + removed + " retirees, reste " + ads.getLength());
        // Correct : figer la liste avant de modifier l'arbre (ou parcourir a l'envers).
        List<Node> frozen = new ArrayList<>();
        for (int i = 0; i < ads.getLength(); i++) {
            frozen.add(ads.item(i));
        }
        for (Node n : frozen) {
            n.getParentNode().removeChild(n);
        }
        System.out.println("PUBS liste figee : " + frozen.size() + " retirees, reste " + ads.getLength());
    }

    // ---------- Etape 4 : namespaces ----------

    static void namespaces() throws Exception {
        Document doc = parse("orders-ns.xml", true);
        // getElementsByTagName compare le nom TEL QU'ECRIT (prefixe compris) ; ...NS compare (URI, nom local).
        System.out.println("NS par nom ecrit : item=" + doc.getElementsByTagName("item").getLength()
                + " o:item=" + doc.getElementsByTagName("o:item").getLength());
        System.out.println("NS par URI : orders=" + doc.getElementsByTagNameNS("urn:shop:orders", "item").getLength()
                + " common=" + doc.getElementsByTagNameNS("urn:shop:common", "item").getLength()
                + " other=" + doc.getElementsByTagNameNS("urn:other", "item").getLength()
                + " tous=" + doc.getElementsByTagNameNS("*", "item").getLength());
        for (Element item : elementChildren(doc.getDocumentElement())) {
            System.out.println("NS " + item.getAttribute("sku") + " : nom=" + item.getTagName() + " prefixe=" + item.getPrefix()
                    + " local=" + item.getLocalName() + " uri=" + item.getNamespaceURI());
        }
        // Sans setNamespaceAware(true) (le DEFAUT !), le DOM ne connait ni URI ni nom local.
        Element raw = elementChildren(parse("orders-ns.xml", false).getDocumentElement()).get(0);
        System.out.println("NS sans namespaces : nom=" + raw.getTagName() + " local=" + raw.getLocalName() + " uri=" + raw.getNamespaceURI());
    }

    // ---------- Etapes 5 et 6 : migrer puis serialiser ----------

    static void migrate() throws Exception {
        Document doc = parse("customers-v1.xml", true);
        // L'ORDRE compte : nettoyer, normaliser, PUIS supprimer ce qui est devenu vide.
        int cleaned = removeWhitespaceAndComments(doc);
        int renamed = renameAll(doc, "customers", "clients") + renameAll(doc, "customer", "client");
        Element root = doc.getDocumentElement();
        root.setAttribute("version", "2");
        int moved = 0;
        for (Element client : elementChildren(root)) {
            moved += moveAttributeToFirstChild(client, "vip") ? 1 : 0;
            for (Element field : elementChildren(client)) {
                // setTextContent REMPLACE tous les enfants par un seul texte : a reserver aux feuilles.
                field.setTextContent(field.getTextContent().strip().replaceAll("\\s+", " "));
                if (field.getTagName().equals("email")) {
                    field.setTextContent(field.getTextContent().toLowerCase(Locale.ROOT));
                }
            }
        }
        int emptied = removeEmptyElements(root);
        sortChildElementsBy(root, "id");
        System.out.println("MIGRATION nettoyes=" + cleaned + " renommes=" + renamed + " deplaces=" + moved + " vides=" + emptied);
        System.out.println("V2 " + serialize(doc));
    }

    static int removeWhitespaceAndComments(Node node) {
        // On retient le frere SUIVANT avant de retirer : un noeud detache n'a plus de frere.
        int removed = 0;
        Node c = node.getFirstChild();
        while (c != null) {
            Node next = c.getNextSibling();
            boolean blank = c.getNodeType() == Node.TEXT_NODE && c.getNodeValue().isBlank();
            if (blank || c.getNodeType() == Node.COMMENT_NODE) {
                node.removeChild(c);
                removed++;
            } else if (c.getNodeType() == Node.ELEMENT_NODE) {
                removed += removeWhitespaceAndComments(c);
            }
            c = next;
        }
        return removed;
    }

    static int renameAll(Document doc, String oldName, String newName) {
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

    static boolean moveAttributeToFirstChild(Element element, String attribute) {
        if (!element.hasAttribute(attribute)) {
            return false;
        }
        Element child = element.getOwnerDocument().createElement(attribute);
        child.setTextContent(element.getAttribute(attribute));
        // insertBefore(x, getFirstChild()) met x en TETE ; si pas d'enfant (null), il l'ajoute a la fin.
        element.insertBefore(child, element.getFirstChild());
        element.removeAttribute(attribute);
        return true;
    }

    static int removeEmptyElements(Element parent) {
        // Du bas vers le haut : les enfants d'abord, puis le jugement de l'element.
        int removed = 0;
        for (Element e : elementChildren(parent)) {
            removed += removeEmptyElements(e);
            if (!e.hasChildNodes() && !e.hasAttributes()) {
                parent.removeChild(e);
                removed++;
            }
        }
        return removed;
    }

    static void sortChildElementsBy(Element parent, String attribute) {
        // appendChild d'un noeud DEJA dans l'arbre le DEPLACE : re-ajouter dans l'ordre suffit.
        List<Element> children = elementChildren(parent);
        children.sort(Comparator.comparing(e -> e.getAttribute(attribute)));
        children.forEach(parent::appendChild);
    }

    static String serialize(Document doc) throws Exception {
        // Tant qu'on n'a pas serialise, les modifications n'existent qu'en memoire.
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }
}
