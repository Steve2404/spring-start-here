package vorkurs02_xml.drills.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.XMLConstants;

/**
 * DRILL 2 - DOM namespace-aware (URI, nom local, prefixe, lookup, *NS)
 * ===================================================================
 *
 * Mode d'emploi : voir Drill01_DomApi. Donnees : Campus.dom() est
 * DEJA namespace-aware. Rappel : tous les elements sont dans
 * Campus.NS (urn:campus, namespace par defaut), <g:grade> dans
 * Campus.GRADES (urn:campus:grades) ; les attributs n'ont pas de namespace.
 *
 *
 * -- Les TODO (methode visee entre crochets) --
 *
 * TODO 1  : courseCountNS(doc)            [getElementsByTagNameNS] -> 3.
 * TODO 2  : gradeCount(doc)               [getElementsByTagNameNS] -> 5.
 * TODO 3  : elementsInNamespace(doc, uri) ["*" comme nom local] NS -> 20, GRADES -> 5.
 * TODO 4  : namespaceOf(node)             [getNamespaceURI] une grade -> GRADES.
 * TODO 5  : localNameOf(node)             [getLocalName] une grade -> "grade" (tagName : "g:grade").
 * TODO 6  : prefixOf(node)                [getPrefix] grade -> "g" ; course -> null.
 * TODO 7  : uriForPrefix(node, prefix)    [lookupNamespaceURI] "g" -> GRADES ; null -> NS.
 * TODO 8  : prefixForUri(node, uri)       [lookupPrefix] GRADES -> "g" ; NS (defaut) -> null.
 * TODO 9  : isDefault(node, uri)          [isDefaultNamespace] NS -> true, GRADES -> false.
 * TODO 10 : gradeOf(student)              [getElementsByTagNameNS + getTextContent] 1er student -> 15.5.
 * TODO 11 : addGrade(doc, student, v)     [createElementNS(GRADES, "g:grade")] ajoute la note.
 * TODO 12 : addCourseNS(doc, id)          [createElementNS(NS, "course")] + setAttribute + appendChild.
 * TODO 13 : setNamespacedAttribute(e, uri, qName, v) [setAttributeNS] ex. xml:lang.
 * TODO 14 : attributeNoNamespace(e, name) [getAttributeNS(null, ...)] C1, "id" -> "C1".
 * TODO 15 : renameToNamespace(doc, node, uri, qName) [renameNode] teacher -> {urn:campus:hr}hr:teacher.
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   factory.setNamespaceAware(true) AVANT newDocumentBuilder() (sinon tout est null)
 *   Document : getElementsByTagNameNS(uri, local) ("*" = joker)  createElementNS(uri, qName)
 *              renameNode(node, uri, qName)
 *   Node     : getNamespaceURI()  getLocalName()  getPrefix() (null si pas de prefixe)
 *              lookupNamespaceURI(prefixOuNull)  lookupPrefix(uri)  isDefaultNamespace(uri)
 *   Element  : getAttributeNS(uriOuNull, local)  setAttributeNS(uri, qName, v)
 *              hasAttributeNS(uri, local)  getAttributeNodeNS(uri, local)
 *   Constantes : XMLConstants.XML_NS_URI ("http://www.w3.org/XML/1998/namespace")
 *                XMLConstants.XMLNS_ATTRIBUTE_NS_URI (les declarations xmlns)
 * ---------------------------------------------------------------------
 */
public class Drill02_NamespaceDomApi {

    public static int courseCountNS(Document doc) {
        throw new UnsupportedOperationException("TODO 1 : implementer courseCountNS()");
    }

    public static int gradeCount(Document doc) {
        throw new UnsupportedOperationException("TODO 2 : implementer gradeCount()");
    }

    public static int elementsInNamespace(Document doc, String uri) {
        throw new UnsupportedOperationException("TODO 3 : implementer elementsInNamespace()");
    }

    public static String namespaceOf(Node node) {
        throw new UnsupportedOperationException("TODO 4 : implementer namespaceOf()");
    }

    public static String localNameOf(Node node) {
        throw new UnsupportedOperationException("TODO 5 : implementer localNameOf()");
    }

    public static String prefixOf(Node node) {
        throw new UnsupportedOperationException("TODO 6 : implementer prefixOf()");
    }

    public static String uriForPrefix(Node node, String prefix) {
        throw new UnsupportedOperationException("TODO 7 : implementer uriForPrefix()");
    }

    public static String prefixForUri(Node node, String uri) {
        throw new UnsupportedOperationException("TODO 8 : implementer prefixForUri()");
    }

    public static boolean isDefault(Node node, String uri) {
        throw new UnsupportedOperationException("TODO 9 : implementer isDefault()");
    }

    public static double gradeOf(Element student) {
        throw new UnsupportedOperationException("TODO 10 : implementer gradeOf()");
    }

    public static Element addGrade(Document doc, Element student, String value) {
        throw new UnsupportedOperationException("TODO 11 : implementer addGrade()");
    }

    public static Element addCourseNS(Document doc, String id) {
        throw new UnsupportedOperationException("TODO 12 : implementer addCourseNS()");
    }

    public static void setNamespacedAttribute(Element element, String uri, String qName, String value) {
        throw new UnsupportedOperationException("TODO 13 : implementer setNamespacedAttribute()");
    }

    public static String attributeNoNamespace(Element element, String localName) {
        throw new UnsupportedOperationException("TODO 14 : implementer attributeNoNamespace()");
    }

    public static Node renameToNamespace(Document doc, Node node, String uri, String qName) {
        throw new UnsupportedOperationException("TODO 15 : implementer renameToNamespace()");
    }

    public static void main(String[] args) {
        Document doc = Campus.dom();
        ExerciseChecker.check("TODO 1 : courseCountNS == 3", courseCountNS(doc) == 3);
        ExerciseChecker.check("TODO 2 : gradeCount == 5", gradeCount(doc) == 5);
        ExerciseChecker.check("TODO 3 : elementsInNamespace NS == 20, GRADES == 5",
                elementsInNamespace(doc, Campus.NS) == 20 && elementsInNamespace(doc, Campus.GRADES) == 5);
        Element grade = (Element) doc.getElementsByTagNameNS(Campus.GRADES, "grade").item(0);
        Element c1 = (Element) doc.getElementsByTagNameNS(Campus.NS, "course").item(0);
        ExerciseChecker.check("TODO 4 : namespaceOf(grade) == GRADES", Campus.GRADES.equals(namespaceOf(grade)));
        ExerciseChecker.check("TODO 5 : localNameOf(grade) == grade (tagName g:grade)",
                "grade".equals(localNameOf(grade)) && grade.getTagName().equals("g:grade"));
        ExerciseChecker.check("TODO 6 : prefixOf(grade) == g, prefixOf(course) == null", "g".equals(prefixOf(grade)) && prefixOf(c1) == null);
        ExerciseChecker.check("TODO 7 : uriForPrefix g -> GRADES, null -> NS",
                Campus.GRADES.equals(uriForPrefix(grade, "g")) && Campus.NS.equals(uriForPrefix(c1, null)));
        ExerciseChecker.check("TODO 8 : prefixForUri GRADES -> g, NS -> null",
                "g".equals(prefixForUri(c1, Campus.GRADES)) && prefixForUri(c1, Campus.NS) == null);
        ExerciseChecker.check("TODO 9 : isDefault NS true, GRADES false", isDefault(c1, Campus.NS) && !isDefault(c1, Campus.GRADES));
        Element s1 = (Element) doc.getElementsByTagNameNS(Campus.NS, "student").item(0);
        ExerciseChecker.check("TODO 10 : gradeOf(1er student) == 15.5", gradeOf(s1) == 15.5);

        Element added = addGrade(doc, s1, "16");
        ExerciseChecker.check("TODO 11 : addGrade -> {GRADES}grade, prefixe g, 6 notes",
                Campus.GRADES.equals(added.getNamespaceURI()) && "g".equals(added.getPrefix()) && gradeCount(doc) == 6);
        Element plain = doc.createElement("course");
        doc.getDocumentElement().appendChild(plain);
        Element c9 = addCourseNS(doc, "C9");
        ExerciseChecker.check("TODO 12 : addCourseNS -> 4 cours dans NS (le <course> SANS namespace n'est pas compte)",
                courseCountNS(doc) == 4 && Campus.NS.equals(c9.getNamespaceURI()) && plain.getNamespaceURI() == null);
        setNamespacedAttribute(c1, XMLConstants.XML_NS_URI, "xml:lang", "fr");
        ExerciseChecker.check("TODO 13 : setNamespacedAttribute xml:lang=fr", c1.getAttributeNS(XMLConstants.XML_NS_URI, "lang").equals("fr"));
        ExerciseChecker.check("TODO 14 : attributeNoNamespace(C1, id) == C1 (et getAttributeNS(NS, id) == \"\")",
                attributeNoNamespace(c1, "id").equals("C1") && c1.getAttributeNS(Campus.NS, "id").isEmpty());
        Node teacher = c1.getElementsByTagNameNS(Campus.NS, "teacher").item(0);
        Node renamed = renameToNamespace(doc, teacher, "urn:campus:hr", "hr:teacher");
        ExerciseChecker.check("TODO 15 : renameToNamespace -> {urn:campus:hr}teacher, texte garde",
                "urn:campus:hr".equals(renamed.getNamespaceURI()) && "hr:teacher".equals(renamed.getNodeName())
                        && renamed.getTextContent().equals("Dupont"));

        ExerciseChecker.summary();
    }
}
