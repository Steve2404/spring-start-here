package vorkurs02_xml.drills.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

/**
 * DRILL 1 - L'API DOM de base (Document, Element, Node, Attr, NodeList)
 * ====================================================================
 *
 * Mode d'emploi : un drill ne s'apprend pas, il se REPETE. Chaque TODO
 * tient en 1 a 3 lignes et vise UNE methode (entre crochets). Fais-le
 * sans regarder la carte memoire ; si tu bloques plus de 2 minutes,
 * lis-la, puis cache-la. Refais le drill aux dates de REVISION.md
 * (git restore sur ce fichier pour remettre les TODO a vide).
 *
 * Les donnees viennent TOUJOURS de vorkurs02_xml.drills.Campus
 * (fixtures/drills/campus.xml).
 *
 *
 * -- Les TODO (methode visee entre crochets) --
 *
 * TODO 1  : root(doc)                 [getDocumentElement] la racine <campus>.
 * TODO 2  : courseCount(doc)          [getElementsByTagName + getLength] -> 3.
 * TODO 3  : courseIds(doc)            [item(i) + getAttribute] -> [C1, C2, C3].
 * TODO 4  : isClosed(course)          [hasAttribute] a-t-il un attribut status ? C3 -> true.
 * TODO 5  : statusOrDefault(course)   [getAttribute] status, ou "open" s'il manque ("" !).
 * TODO 6  : titleOf(course)           [getTextContent] C2 -> "Bases de donnees".
 * TODO 7  : firstChildName(course)    [getFirstChild + getNodeName] C1 -> "#text" (piege).
 * TODO 8  : firstElementChildName(c)  [getNextSibling + getNodeType] C1 -> "title".
 * TODO 9  : parentName(element)       [getParentNode] une <person> -> "people".
 * TODO 10 : childNodeCount(element)   [getChildNodes] <people> -> 9 (4 person + 5 blancs).
 * TODO 11 : firstTextValue(element)   [getNodeValue] 1re person -> "Ana".
 * TODO 12 : addCourse(doc, id, title) [createElement, setAttribute, appendChild] ajoute
 *           <course id=..><title>..</title></course> a la fin de la racine, et le rend.
 * TODO 13 : insertFirst(parent, n)    [insertBefore] n devient le 1er enfant.
 * TODO 14 : removePeople(doc)         [removeChild] detache <people>, rend son nom.
 * TODO 15 : replaceTitle(course, t)   [replaceChild] remplace <title> par un nouveau <title>t</title>.
 * TODO 16 : renameAttribute(e, a, b)  [setAttribute + removeAttribute] credits -> ects.
 * TODO 17 : copyCourse(course)        [cloneNode(true)] copie profonde, sans parent.
 * TODO 18 : importInto(target, node)  [importNode] copie un noeud dans un AUTRE Document.
 * TODO 19 : attributeCount(element)   [getAttributes().getLength()] C3 -> 4.
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   Document : getDocumentElement()  getElementsByTagName(n)  createElement(n)
 *              createTextNode(t)  importNode(node, deep)
 *   Node     : getNodeType()  getNodeName()  getNodeValue()  getTextContent()
 *              getParentNode()  getFirstChild()  getLastChild()  getNextSibling()
 *              getPreviousSibling()  getChildNodes()  appendChild(n)
 *              insertBefore(new, ref)  removeChild(n)  replaceChild(new, old)
 *              cloneNode(deep)  getOwnerDocument()  getAttributes()
 *   Element  : getTagName()  getAttribute(n) ("" si absent)  hasAttribute(n)
 *              setAttribute(n, v)  removeAttribute(n)  getAttributeNode(n)
 *   NodeList : getLength()  item(i)   (VIVANTE, pas une java.util.List)
 *   Noms     : element -> son nom ; texte -> "#text" ; commentaire -> "#comment" ;
 *              document -> "#document"
 * ---------------------------------------------------------------------
 */
public class Drill01_DomApi {

    public static Element root(Document doc) {
        throw new UnsupportedOperationException("TODO 1 : implementer root()");
    }

    public static int courseCount(Document doc) {
        throw new UnsupportedOperationException("TODO 2 : implementer courseCount()");
    }

    public static List<String> courseIds(Document doc) {
        throw new UnsupportedOperationException("TODO 3 : implementer courseIds()");
    }

    public static boolean isClosed(Element course) {
        throw new UnsupportedOperationException("TODO 4 : implementer isClosed()");
    }

    public static String statusOrDefault(Element course) {
        throw new UnsupportedOperationException("TODO 5 : implementer statusOrDefault()");
    }

    public static String titleOf(Element course) {
        throw new UnsupportedOperationException("TODO 6 : implementer titleOf()");
    }

    public static String firstChildName(Element course) {
        throw new UnsupportedOperationException("TODO 7 : implementer firstChildName()");
    }

    public static String firstElementChildName(Element course) {
        throw new UnsupportedOperationException("TODO 8 : implementer firstElementChildName()");
    }

    public static String parentName(Element element) {
        throw new UnsupportedOperationException("TODO 9 : implementer parentName()");
    }

    public static int childNodeCount(Element element) {
        throw new UnsupportedOperationException("TODO 10 : implementer childNodeCount()");
    }

    public static String firstTextValue(Element element) {
        throw new UnsupportedOperationException("TODO 11 : implementer firstTextValue()");
    }

    public static Element addCourse(Document doc, String id, String title) {
        throw new UnsupportedOperationException("TODO 12 : implementer addCourse()");
    }

    public static void insertFirst(Element parent, Node newChild) {
        throw new UnsupportedOperationException("TODO 13 : implementer insertFirst()");
    }

    public static String removePeople(Document doc) {
        throw new UnsupportedOperationException("TODO 14 : implementer removePeople()");
    }

    public static void replaceTitle(Element course, String newTitle) {
        throw new UnsupportedOperationException("TODO 15 : implementer replaceTitle()");
    }

    public static void renameAttribute(Element element, String from, String to) {
        throw new UnsupportedOperationException("TODO 16 : implementer renameAttribute()");
    }

    public static Node copyCourse(Element course) {
        throw new UnsupportedOperationException("TODO 17 : implementer copyCourse()");
    }

    public static Node importInto(Document target, Node node) {
        throw new UnsupportedOperationException("TODO 18 : implementer importInto()");
    }

    public static int attributeCount(Element element) {
        throw new UnsupportedOperationException("TODO 19 : implementer attributeCount()");
    }

    public static void main(String[] args) throws Exception {
        Document doc = Campus.dom();
        ExerciseChecker.check("TODO 1 : root == <campus>", root(doc).getTagName().equals("campus"));
        ExerciseChecker.check("TODO 2 : courseCount == 3", courseCount(doc) == 3);
        ExerciseChecker.check("TODO 3 : courseIds == [C1, C2, C3]", courseIds(doc).equals(List.of("C1", "C2", "C3")));
        Element c1 = course(doc, 0);
        Element c2 = course(doc, 1);
        Element c3 = course(doc, 2);
        ExerciseChecker.check("TODO 4 : isClosed(C3) && !isClosed(C1)", isClosed(c3) && !isClosed(c1));
        ExerciseChecker.check("TODO 5 : statusOrDefault C1 -> open, C3 -> closed",
                statusOrDefault(c1).equals("open") && statusOrDefault(c3).equals("closed"));
        ExerciseChecker.check("TODO 6 : titleOf(C2) == Bases de donnees", titleOf(c2).equals("Bases de donnees"));
        ExerciseChecker.check("TODO 7 : firstChildName(C1) == #text", firstChildName(c1).equals("#text"));
        ExerciseChecker.check("TODO 8 : firstElementChildName(C1) == title", "title".equals(firstElementChildName(c1)));
        Element ana = (Element) doc.getElementsByTagName("person").item(0);
        Element people = (Element) doc.getElementsByTagName("people").item(0);
        ExerciseChecker.check("TODO 9 : parentName(Ana) == people", parentName(ana).equals("people"));
        ExerciseChecker.check("TODO 10 : childNodeCount(people) == 9", childNodeCount(people) == 9);
        ExerciseChecker.check("TODO 11 : firstTextValue(Ana) == Ana (et getNodeValue d'un Element == null)",
                firstTextValue(ana).equals("Ana") && ana.getNodeValue() == null);

        Element added = addCourse(doc, "C9", "Java");
        ExerciseChecker.check("TODO 12 : addCourse -> 4 cours, dernier enfant de la racine, titre Java",
                courseCount(doc) == 4 && doc.getDocumentElement().getLastChild() == added
                        && added.getAttribute("id").equals("C9") && titleOf(added).equals("Java"));
        Node comment = doc.createComment("en tete");
        insertFirst(doc.getDocumentElement(), comment);
        ExerciseChecker.check("TODO 13 : insertFirst -> le commentaire est le 1er enfant", doc.getDocumentElement().getFirstChild() == comment);
        ExerciseChecker.check("TODO 14 : removePeople == people, plus aucune person",
                removePeople(doc).equals("people") && doc.getElementsByTagName("person").getLength() == 0);
        replaceTitle(c1, "Algo avancee");
        ExerciseChecker.check("TODO 15 : replaceTitle -> un seul <title>, 'Algo avancee'",
                titleOf(c1).equals("Algo avancee") && c1.getElementsByTagName("title").getLength() == 1);
        renameAttribute(c1, "credits", "ects");
        ExerciseChecker.check("TODO 16 : renameAttribute credits -> ects", c1.getAttribute("ects").equals("6") && !c1.hasAttribute("credits"));
        Node copy = copyCourse(c2);
        ExerciseChecker.check("TODO 17 : copyCourse -> copie sans parent, 2 students, pas le meme objet",
                copy != c2 && copy.getParentNode() == null && ((Element) copy).getElementsByTagName("student").getLength() == 2);
        Document other = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node imported = importInto(other, c2);
        ExerciseChecker.check("TODO 18 : importInto -> appartient au nouveau Document, l'original reste",
                imported.getOwnerDocument() == other && c2.getOwnerDocument() == doc && c2.getParentNode() != null);
        ExerciseChecker.check("TODO 19 : attributeCount(C3) == 4", attributeCount(c3) == 4);

        ExerciseChecker.summary();
    }

    private static Element course(Document doc, int i) {
        return (Element) doc.getElementsByTagName("course").item(i);
    }
}
