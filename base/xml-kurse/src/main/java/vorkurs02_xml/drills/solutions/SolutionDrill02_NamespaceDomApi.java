package vorkurs02_xml.drills.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import vorkurs02_xml.drills.Campus;

/**
 * Corrige du drill 2. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill02_NamespaceDomApi.
 */
public class SolutionDrill02_NamespaceDomApi {

    public static int courseCountNS(Document doc) {
        // Recherche par (URI, nom local) : independante du prefixe ecrit dans le fichier.
        return doc.getElementsByTagNameNS(Campus.NS, "course").getLength();
    }

    public static int gradeCount(Document doc) {
        return doc.getElementsByTagNameNS(Campus.GRADES, "grade").getLength();
    }

    public static int elementsInNamespace(Document doc, String uri) {
        // "*" comme nom local = tous les elements de ce namespace.
        return doc.getElementsByTagNameNS(uri, "*").getLength();
    }

    public static String namespaceOf(Node node) {
        return node.getNamespaceURI();
    }

    public static String localNameOf(Node node) {
        // getLocalName sans prefixe ; getNodeName / getTagName gardent "g:grade".
        return node.getLocalName();
    }

    public static String prefixOf(Node node) {
        // null (et non "") quand le nom n'a pas de prefixe, meme dans un namespace par defaut.
        return node.getPrefix();
    }

    public static String uriForPrefix(Node node, String prefix) {
        // lookupNamespaceURI remonte les portees ; prefix null = namespace par defaut.
        return node.lookupNamespaceURI(prefix);
    }

    public static String prefixForUri(Node node, String uri) {
        // Le sens inverse ; pour le namespace par DEFAUT il n'y a pas de prefixe -> null.
        return node.lookupPrefix(uri);
    }

    public static boolean isDefault(Node node, String uri) {
        return node.isDefaultNamespace(uri);
    }

    public static double gradeOf(Element student) {
        return Double.parseDouble(student.getElementsByTagNameNS(Campus.GRADES, "grade").item(0).getTextContent());
    }

    public static Element addGrade(Document doc, Element student, String value) {
        // createElementNS(URI, qName) : le prefixe fait partie du qName, l'URI donne l'identite.
        Element grade = doc.createElementNS(Campus.GRADES, "g:grade");
        grade.setTextContent(value);
        student.appendChild(grade);
        return grade;
    }

    public static Element addCourseNS(Document doc, String id) {
        // createElement (sans NS) creerait un <course> SANS namespace, invisible pour getElementsByTagNameNS.
        Element course = doc.createElementNS(Campus.NS, "course");
        course.setAttribute("id", id);
        doc.getDocumentElement().appendChild(course);
        return course;
    }

    public static void setNamespacedAttribute(Element element, String uri, String qName, String value) {
        element.setAttributeNS(uri, qName, value);
    }

    public static String attributeNoNamespace(Element element, String localName) {
        // Un attribut sans prefixe n'a PAS de namespace (meme sous un xmlns par defaut) : URI = null.
        return element.getAttributeNS(null, localName);
    }

    public static Node renameToNamespace(Document doc, Node node, String uri, String qName) {
        // renameNode change nom ET namespace ; il peut rendre un autre noeud : on garde son retour.
        return doc.renameNode(node, uri, qName);
    }
}
