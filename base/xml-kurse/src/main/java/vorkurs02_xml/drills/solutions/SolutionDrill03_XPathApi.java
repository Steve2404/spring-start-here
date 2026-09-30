package vorkurs02_xml.drills.solutions;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Corrige du drill 3. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill03_XPathApi.
 */
public class SolutionDrill03_XPathApi {

    public static XPath newXPath(NamespaceContext ctx) {
        // Une XPath par thread (pas thread-safe) ; le NamespaceContext relie NOS prefixes aux URI.
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(ctx);
        return xpath;
    }

    public static XPathExpression compile(XPath xpath, String expression) throws XPathExpressionException {
        // Compiler une fois, evaluer souvent (et l'erreur de syntaxe arrive tout de suite).
        return xpath.compile(expression);
    }

    public static double asNumber(XPath xpath, String expr, Object context) throws XPathExpressionException {
        return (Double) xpath.evaluate(expr, context, XPathConstants.NUMBER);
    }

    public static String asString(XPath xpath, String expr, Object context) throws XPathExpressionException {
        // Sur un node-set, STRING rend la valeur du PREMIER noeud (ordre du document).
        return (String) xpath.evaluate(expr, context, XPathConstants.STRING);
    }

    public static boolean asBoolean(XPath xpath, String expr, Object context) throws XPathExpressionException {
        // Un node-set non vide vaut true : pratique pour "existe-t-il... ?".
        return (Boolean) xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
    }

    public static List<String> asTexts(XPath xpath, String expr, Object context) throws XPathExpressionException {
        NodeList list = (NodeList) xpath.evaluate(expr, context, XPathConstants.NODESET);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            out.add(list.item(i).getTextContent());
        }
        return out;
    }

    public static Node asNode(XPath xpath, String expr, Object context) throws XPathExpressionException {
        // NODE rend le premier noeud, ou null si aucun.
        return (Node) xpath.evaluate(expr, context, XPathConstants.NODE);
    }

    public static String exprCourseTitles() {
        return "//c:course/c:title";
    }

    public static String exprCreditsSum() {
        return "sum(//c:course/@credits)";
    }

    public static String exprStudentCount(String courseId) {
        return "count(//c:course[@id='" + courseId + "']/c:student)";
    }

    public static String exprCoursesTaughtBy(String teacher) {
        // Un predicat [enfant='valeur'] compare le texte de l'enfant.
        return "//c:course[c:teacher='" + teacher + "']/@id";
    }

    public static String exprLastCourseTitle() {
        return "//c:course[last()]/c:title";
    }

    public static String exprBestGrade() {
        // XPath 1.0 n'a pas de max() : "une note qui n'est plus petite qu'AUCUNE autre".
        return "//g:grade[not(. < //g:grade)]";
    }

    public static String exprPeopleWithoutEmail() {
        return "//c:person[not(@email)]";
    }

    public static String exprTitlesContaining(String word) {
        return "//c:title[contains(., '" + word + "')]";
    }

    public static String exprEmailsStartingWith(String start) {
        return "//c:person[starts-with(@email, '" + start + "')]/@email";
    }

    public static String exprNextCourses(String courseId) {
        return "//c:course[@id='" + courseId + "']/following-sibling::c:course/@id";
    }

    public static String exprCourseOfGrade(String value) {
        return "//g:grade[. = " + value + "]/ancestor::c:course/@id";
    }

    public static String exprTitleLength(String courseId) {
        return "string-length(//c:course[@id='" + courseId + "']/c:title)";
    }

    public static String exprPositionOfCourse(String courseId) {
        // Position = nombre de freres precedents + 1.
        return "count(//c:course[@id='" + courseId + "']/preceding-sibling::c:course) + 1";
    }
}
