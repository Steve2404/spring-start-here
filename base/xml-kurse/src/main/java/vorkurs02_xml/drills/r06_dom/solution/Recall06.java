package vorkurs02_xml.drills.r06_dom.solution;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import vorkurs02_xml.drills.Data;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Corrige du drill 6 : l'API DOM (0.2.17), sur campus.xml.
 */
public class Recall06 {

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder builder = f.newDocumentBuilder();
        Document doc = builder.parse(Data.campus().toFile());
        Element root = doc.getDocumentElement();

        // D01 : le Document n'est pas la racine.
        System.out.println("D01 : " + doc.getNodeName() + " " + root.getTagName() + " " + root.getAttribute("name"));
        // D02 : par nom ecrit, puis par (URI, nom local).
        System.out.println("D02 : " + doc.getElementsByTagName("course").getLength() + " "
                + doc.getElementsByTagNameNS("urn:campus:grades", "grade").getLength());
        NodeList courses = doc.getElementsByTagName("course");
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < courses.getLength(); i++) {
            ids.add(((Element) courses.item(i)).getAttribute("id"));
        }
        System.out.println("D03 : " + ids);
        Element c1 = (Element) courses.item(0);
        Element c3 = (Element) courses.item(2);
        // D04 : getAttribute rend "" pour un absent ; hasAttribute distingue.
        System.out.println("D04 : " + c1.hasAttribute("status") + " [" + c1.getAttribute("status") + "] "
                + c3.hasAttribute("status") + " [" + c3.getAttribute("status") + "]");
        // D05 : le premier enfant est l'indentation.
        Node n = c1.getFirstChild();
        String first = n.getNodeName();
        while (n.getNodeType() != Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        System.out.println("D05 : " + first + " " + n.getNodeName());
        // D06 : tous les noeuds enfants, puis le parent.
        System.out.println("D06 : " + c1.getChildNodes().getLength() + " " + c1.getParentNode().getNodeName());
        // D07 : getNodeValue n'a de sens que sur un texte (null sur un element) ; getTextContent concatene.
        Element title = (Element) c1.getElementsByTagName("title").item(0);
        System.out.println("D07 : " + title.getNodeValue() + " " + title.getFirstChild().getNodeValue() + " " + title.getTextContent());
        // D08 : createElement cree un element SANS namespace, meme dans un DOM namespace-aware.
        Element plain = doc.createElement("course");
        root.appendChild(plain);
        int byName = doc.getElementsByTagName("course").getLength();
        int byNs = doc.getElementsByTagNameNS("urn:campus", "course").getLength();
        Element good = doc.createElementNS("urn:campus", "course");
        root.appendChild(good);
        System.out.println("D08 : " + byName + " " + byNs + " " + doc.getElementsByTagNameNS("urn:campus", "course").getLength());
        // D09 : replaceChild(NOUVEAU, ANCIEN) ; removeChild se demande au parent et rend le noeud.
        Element fresh = doc.createElementNS("urn:campus", "title");
        fresh.setTextContent("Algo 2");
        c1.replaceChild(fresh, title);
        Node people = doc.getElementsByTagName("people").item(0);
        Node removed = people.getParentNode().removeChild(people);
        System.out.println("D09 : " + c1.getElementsByTagName("title").item(0).getTextContent() + " " + removed.getNodeName()
                + " " + doc.getElementsByTagName("person").getLength());
        // D10 : cloneNode(true) copie en profondeur, sans parent ; importNode copie vers un AUTRE document.
        Node copy = c3.cloneNode(true);
        Document other = builder.newDocument();
        Node imported = other.importNode(c3, true);
        other.appendChild(imported);
        System.out.println("D10 : " + (copy.getParentNode() == null) + " " + (imported.getOwnerDocument() == other)
                + " " + other.getDocumentElement().getAttribute("id"));
        // D11 : dans un DOM namespace-aware, les xmlns sont AUSSI des attributs du DOM.
        System.out.println("D11 : " + root.getAttributes().getLength());
        // D12 : serialiser un sous-arbre.
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(other), new StreamResult(out));
        System.out.println("D12 : " + out.toString().replaceAll("\\s+", " "));
    }
}
