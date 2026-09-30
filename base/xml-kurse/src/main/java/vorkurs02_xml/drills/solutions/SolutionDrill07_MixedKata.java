package vorkurs02_xml.drills.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import vorkurs02_xml.drills.Campus;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Corrige du drill 7. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill07_MixedKata.
 */
public class SolutionDrill07_MixedKata {

    private static XPath xpath() {
        XPath x = XPathFactory.newInstance().newXPath();
        x.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String p) {
                return switch (p) {
                    case "c" -> Campus.NS;
                    case "g" -> Campus.GRADES;
                    default -> XMLConstants.NULL_NS_URI;
                };
            }

            public String getPrefix(String uri) {
                return null;
            }

            public Iterator<String> getPrefixes(String uri) {
                return List.<String>of().iterator();
            }
        });
        return x;
    }

    private static List<String> texts(Document doc, String expr) throws Exception {
        NodeList list = (NodeList) xpath().evaluate(expr, doc, XPathConstants.NODESET);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            out.add(list.item(i).getTextContent());
        }
        return out;
    }

    private static String nameOf(Document doc, String personId) throws Exception {
        return xpath().evaluate("//c:person[@id='" + personId + "']", doc);
    }

    public static List<String> studentsOf(Document doc, String courseId) throws Exception {
        // Deux sauts : les refs du cours, puis le nom de chaque personne (une "jointure" sur l'id).
        List<String> names = new ArrayList<>();
        for (String ref : texts(doc, "//c:course[@id='" + courseId + "']/c:student/@ref")) {
            names.add(nameOf(doc, ref));
        }
        return names;
    }

    public static double averageOf(Document doc, String courseId) throws Exception {
        // sum() div count() se fait en XPath ; l'arrondi a 2 decimales en Java.
        double avg = (Double) xpath().evaluate("sum(//c:course[@id='" + courseId + "']//g:grade) div count(//c:course[@id='"
                + courseId + "']//g:grade)", doc, XPathConstants.NUMBER);
        return Math.round(avg * 100) / 100.0;
    }

    public static String bestStudent(Document doc) throws Exception {
        // Le "max sans max()" du drill 3, puis on remonte au student pour lire sa ref.
        String ref = xpath().evaluate("//g:grade[not(. < //g:grade)]/../@ref", doc);
        return nameOf(doc, ref);
    }

    public static List<String> failing(Document doc, double threshold) throws Exception {
        // TreeSet : sans doublon et trie (une personne peut echouer dans plusieurs cours).
        TreeSet<String> names = new TreeSet<>();
        for (String ref : texts(doc, "//c:student[g:grade < " + threshold + "]/@ref")) {
            names.add(nameOf(doc, ref));
        }
        return List.copyOf(names);
    }

    public static List<String> emailDomains(Document doc) throws Exception {
        return texts(doc, "//c:person/@email").stream()
                .map(e -> e.substring(e.indexOf('@') + 1)).distinct().sorted().toList();
    }

    public static int creditsOf(Document doc, String personId) throws Exception {
        // Un cours compte s'il a AU MOINS un student avec cette ref : predicat sur l'enfant.
        return ((Double) xpath().evaluate("sum(//c:course[c:student/@ref='" + personId + "']/@credits)", doc,
                XPathConstants.NUMBER)).intValue();
    }

    public static Map<String, Long> teachersWithCourseCount(Document doc) throws Exception {
        return texts(doc, "//c:course/c:teacher").stream()
                .collect(Collectors.groupingBy(t -> t, TreeMap::new, Collectors.counting()));
    }

    public static List<String> closedCourses(Document doc) throws Exception {
        return texts(doc, "//c:course[@status='closed']/@id");
    }

    public static List<String> coursesWithoutStudents(Document doc) throws Exception {
        return texts(doc, "//c:course[not(c:student)]/@id");
    }

    public static int renameTeacher(Document doc, String from, String to) {
        // Modifier = DOM. getElementsByTagNameNS trouve les <teacher> quel que soit leur prefixe.
        NodeList teachers = doc.getElementsByTagNameNS(Campus.NS, "teacher");
        int changed = 0;
        for (int i = 0; i < teachers.getLength(); i++) {
            if (teachers.item(i).getTextContent().equals(from)) {
                teachers.item(i).setTextContent(to);
                changed++;
            }
        }
        return changed;
    }

    public static void addStudent(Document doc, String courseId, String personId, String grade) throws Exception {
        // Creer DANS LES BONS namespaces : student dans urn:campus, grade dans urn:campus:grades.
        Element course = (Element) xpath().evaluate("//c:course[@id='" + courseId + "']", doc, XPathConstants.NODE);
        Element student = doc.createElementNS(Campus.NS, "student");
        student.setAttribute("ref", personId);
        Element g = doc.createElementNS(Campus.GRADES, "g:grade");
        g.setTextContent(grade);
        student.appendChild(g);
        course.appendChild(student);
    }

    public static List<String> toCsv(Document doc) throws Exception {
        List<String> lines = new ArrayList<>();
        for (String id : texts(doc, "//c:course/@id")) {
            String title = xpath().evaluate("//c:course[@id='" + id + "']/c:title", doc);
            int n = ((Double) xpath().evaluate("count(//c:course[@id='" + id + "']/c:student)", doc, XPathConstants.NUMBER)).intValue();
            lines.add(id + ";" + title + ";" + n);
        }
        return lines;
    }

    public static String personWithoutEmail(Document doc) throws Exception {
        return xpath().evaluate("//c:person[not(@email)]", doc);
    }

    public static Map<String, Long> levelHistogram(Document doc) throws Exception {
        return texts(doc, "//c:course/@level").stream()
                .collect(Collectors.groupingBy(l -> l, TreeMap::new, Collectors.counting()));
    }

    public static String transcript(Document doc, String personId) throws Exception {
        // Pour chaque cours suivi : titre=note, dans l'ordre du document, separes par "; ".
        List<String> parts = new ArrayList<>();
        for (String id : texts(doc, "//c:course[c:student/@ref='" + personId + "']/@id")) {
            String title = xpath().evaluate("//c:course[@id='" + id + "']/c:title", doc);
            String grade = xpath().evaluate("//c:course[@id='" + id + "']/c:student[@ref='" + personId + "']/g:grade", doc);
            parts.add(title + "=" + grade);
        }
        return nameOf(doc, personId) + ": " + String.join("; ", parts);
    }
}
