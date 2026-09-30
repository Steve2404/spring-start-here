package vorkurs02_xml.drills.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Corrige du drill 1. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill01_DomApi.
 */
public class SolutionDrill01_DomApi {

    public static Element root(Document doc) {
        // Le Document n'est PAS la racine : c'est son parent invisible.
        return doc.getDocumentElement();
    }

    public static int courseCount(Document doc) {
        // Recherche dans TOUT le sous-arbre, par nom ecrit (qName).
        return doc.getElementsByTagName("course").getLength();
    }

    public static List<String> courseIds(Document doc) {
        // NodeList n'est pas une List : boucle par index + item(i).
        List<String> ids = new ArrayList<>();
        var courses = doc.getElementsByTagName("course");
        for (int i = 0; i < courses.getLength(); i++) {
            ids.add(((Element) courses.item(i)).getAttribute("id"));
        }
        return ids;
    }

    public static boolean isClosed(Element course) {
        // hasAttribute distingue "absent" de "present mais vide".
        return course.hasAttribute("status");
    }

    public static String statusOrDefault(Element course) {
        // getAttribute rend "" (jamais null) quand l'attribut manque.
        String s = course.getAttribute("status");
        return s.isEmpty() ? "open" : s;
    }

    public static String titleOf(Element course) {
        // getTextContent concatene tout le texte du sous-arbre de <title>.
        return course.getElementsByTagName("title").item(0).getTextContent();
    }

    public static String firstChildName(Element course) {
        // Piege : le premier enfant est l'indentation, un noeud texte nomme "#text".
        return course.getFirstChild().getNodeName();
    }

    public static String firstElementChildName(Element course) {
        // On saute les noeuds non-elements avec getNextSibling.
        Node n = course.getFirstChild();
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        return n == null ? null : n.getNodeName();
    }

    public static String parentName(Element element) {
        return element.getParentNode().getNodeName();
    }

    public static int childNodeCount(Element element) {
        // TOUS les enfants : elements + textes d'indentation + commentaires.
        return element.getChildNodes().getLength();
    }

    public static String firstTextValue(Element element) {
        // getNodeValue a du sens sur un Text ; sur un Element il vaut null.
        return element.getFirstChild().getNodeValue();
    }

    public static Element addCourse(Document doc, String id, String title) {
        // Creer (orphelin), remplir, PUIS accrocher : sans appendChild l'element n'est nulle part.
        Element course = doc.createElement("course");
        course.setAttribute("id", id);
        Element t = doc.createElement("title");
        t.setTextContent(title);
        course.appendChild(t);
        doc.getDocumentElement().appendChild(course);
        return course;
    }

    public static void insertFirst(Element parent, Node newChild) {
        // insertBefore(x, null) ajouterait a la fin ; avec getFirstChild(), on insere en tete.
        parent.insertBefore(newChild, parent.getFirstChild());
    }

    public static String removePeople(Document doc) {
        // removeChild se demande au PARENT et rend le noeud detache.
        Node people = doc.getElementsByTagName("people").item(0);
        return people.getParentNode().removeChild(people).getNodeName();
    }

    public static void replaceTitle(Element course, String newTitle) {
        // replaceChild(nouveau, ancien) : l'ordre des arguments est le piege classique.
        Node old = course.getElementsByTagName("title").item(0);
        Element fresh = course.getOwnerDocument().createElement("title");
        fresh.setTextContent(newTitle);
        course.replaceChild(fresh, old);
    }

    public static void renameAttribute(Element element, String from, String to) {
        // Il n'y a pas de "renameAttribute" : on copie la valeur puis on supprime l'ancien.
        element.setAttribute(to, element.getAttribute(from));
        element.removeAttribute(from);
    }

    public static Node copyCourse(Element course) {
        // cloneNode(true) = copie PROFONDE, orpheline (pas de parent) mais du meme document.
        return course.cloneNode(true);
    }

    public static Node importInto(Document target, Node node) {
        // Un noeud ne passe pas d'un Document a un autre par appendChild : importNode le copie.
        return target.importNode(node, true);
    }

    public static int attributeCount(Element element) {
        return element.getAttributes().getLength();
    }
}
