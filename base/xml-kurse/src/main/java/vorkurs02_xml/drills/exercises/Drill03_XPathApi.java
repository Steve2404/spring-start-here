package vorkurs02_xml.drills.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DRILL 3 - L'API javax.xml.xpath et les expressions XPath 1.0 du quotidien
 * =======================================================================
 *
 * Mode d'emploi : voir Drill01_DomApi. Donnees : Campus.dom(). Le
 * NamespaceContext est deja ecrit (CTX) : c -> urn:campus, g -> urn:campus:grades.
 * TOUS les noms d'elements s'ecrivent donc avec c: (ou g: pour grade) ;
 * les attributs (@id, @credits...) SANS prefixe.
 *
 *
 * -- Les TODO (methode ou fonction XPath visee entre crochets) --
 *
 * TODO 1  : newXPath(ctx)               [XPathFactory.newXPath + setNamespaceContext]
 * TODO 2  : compile(xpath, expr)        [compile] -> XPathExpression.
 * TODO 3  : asNumber(xpath, expr, ctx)  [XPathConstants.NUMBER] -> double.
 * TODO 4  : asString(...)               [XPathConstants.STRING].
 * TODO 5  : asBoolean(...)              [XPathConstants.BOOLEAN].
 * TODO 6  : asTexts(...)                [XPathConstants.NODESET] -> textes des noeuds.
 * TODO 7  : asNode(...)                 [XPathConstants.NODE] -> 1er noeud ou null.
 * TODO 8  : exprCourseTitles()          [chemin //] -> [Algorithmique, Bases de donnees, Reseaux].
 * TODO 9  : exprCreditsSum()            [sum()] -> 15.
 * TODO 10 : exprStudentCount(id)        [count()] C1 -> 3.
 * TODO 11 : exprCoursesTaughtBy(t)      [predicat enfant='v'] Dupont -> [C1, C3].
 * TODO 12 : exprLastCourseTitle()       [last()] -> Reseaux.
 * TODO 13 : exprBestGrade()             [not(. < ...)] la meilleure note -> 18.
 * TODO 14 : exprPeopleWithoutEmail()    [not(@a)] -> [Ben].
 * TODO 15 : exprTitlesContaining(w)     [contains()] "Base" -> [Bases de donnees].
 * TODO 16 : exprEmailsStartingWith(s)   [starts-with()] "c" -> [chloe@campus.fr].
 * TODO 17 : exprNextCourses(id)         [following-sibling::] C1 -> [C2, C3].
 * TODO 18 : exprCourseOfGrade(v)        [ancestor::] 18 -> C2.
 * TODO 19 : exprTitleLength(id)         [string-length()] C2 -> 16.
 * TODO 20 : exprPositionOfCourse(id)    [preceding-sibling:: + count] C2 -> 2.
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   XPath x = XPathFactory.newInstance().newXPath(); x.setNamespaceContext(ctx);
 *   x.evaluate(expr, contexte, XPathConstants.NODESET | NODE | STRING | NUMBER | BOOLEAN)
 *     -> NodeList | Node | String | Double | Boolean   (cast obligatoire)
 *   XPathExpression e = x.compile(expr); e.evaluate(contexte, type)
 *   Chemins : /abs  //partout  .  ..  @attr  *  node()  text()
 *   Axes    : child:: descendant:: parent:: ancestor:: following-sibling::
 *             preceding-sibling:: following:: preceding:: self:: attribute::
 *   Predicats : [2] [last()] [@a] [@a='v'] [enfant='v'] [not(...)] [position() < 3]
 *   Fonctions : count() sum() contains() starts-with() string-length()
 *               normalize-space() translate() concat() substring() number() string()
 *   XPath 1.0 : pas de max()/min()/distinct-values() -> astuces avec not() et preceding::
 * ---------------------------------------------------------------------
 */
public class Drill03_XPathApi {

    public static XPath newXPath(NamespaceContext ctx) {
        throw new UnsupportedOperationException("TODO 1 : implementer newXPath()");
    }

    public static XPathExpression compile(XPath xpath, String expression) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 2 : implementer compile()");
    }

    public static double asNumber(XPath xpath, String expr, Object context) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 3 : implementer asNumber()");
    }

    public static String asString(XPath xpath, String expr, Object context) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 4 : implementer asString()");
    }

    public static boolean asBoolean(XPath xpath, String expr, Object context) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 5 : implementer asBoolean()");
    }

    public static List<String> asTexts(XPath xpath, String expr, Object context) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 6 : implementer asTexts()");
    }

    public static Node asNode(XPath xpath, String expr, Object context) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 7 : implementer asNode()");
    }

    public static String exprCourseTitles() {
        throw new UnsupportedOperationException("TODO 8 : implementer exprCourseTitles()");
    }

    public static String exprCreditsSum() {
        throw new UnsupportedOperationException("TODO 9 : implementer exprCreditsSum()");
    }

    public static String exprStudentCount(String courseId) {
        throw new UnsupportedOperationException("TODO 10 : implementer exprStudentCount()");
    }

    public static String exprCoursesTaughtBy(String teacher) {
        throw new UnsupportedOperationException("TODO 11 : implementer exprCoursesTaughtBy()");
    }

    public static String exprLastCourseTitle() {
        throw new UnsupportedOperationException("TODO 12 : implementer exprLastCourseTitle()");
    }

    public static String exprBestGrade() {
        throw new UnsupportedOperationException("TODO 13 : implementer exprBestGrade()");
    }

    public static String exprPeopleWithoutEmail() {
        throw new UnsupportedOperationException("TODO 14 : implementer exprPeopleWithoutEmail()");
    }

    public static String exprTitlesContaining(String word) {
        throw new UnsupportedOperationException("TODO 15 : implementer exprTitlesContaining()");
    }

    public static String exprEmailsStartingWith(String start) {
        throw new UnsupportedOperationException("TODO 16 : implementer exprEmailsStartingWith()");
    }

    public static String exprNextCourses(String courseId) {
        throw new UnsupportedOperationException("TODO 17 : implementer exprNextCourses()");
    }

    public static String exprCourseOfGrade(String value) {
        throw new UnsupportedOperationException("TODO 18 : implementer exprCourseOfGrade()");
    }

    public static String exprTitleLength(String courseId) {
        throw new UnsupportedOperationException("TODO 19 : implementer exprTitleLength()");
    }

    public static String exprPositionOfCourse(String courseId) {
        throw new UnsupportedOperationException("TODO 20 : implementer exprPositionOfCourse()");
    }

    public static void main(String[] args) throws Exception {
        XPath x = newXPath(CTX);
        Document doc = Campus.dom();
        ExerciseChecker.check("TODO 1 : newXPath garde le NamespaceContext", x.getNamespaceContext() == CTX);
        XPathExpression compiled = compile(x, "count(//c:person)");
        ExerciseChecker.check("TODO 2 : compile(count(//c:person)) -> 4", ((Double) compiled.evaluate(doc, XPathConstants.NUMBER)) == 4.0);
        ExerciseChecker.check("TODO 3 : asNumber(count(//g:grade)) == 5", asNumber(x, "count(//g:grade)", doc) == 5.0);
        ExerciseChecker.check("TODO 4 : asString(//c:person) == Ana (1er noeud)", asString(x, "//c:person", doc).equals("Ana"));
        ExerciseChecker.check("TODO 5 : asBoolean(//c:course[@status]) == true", asBoolean(x, "//c:course[@status]", doc));
        ExerciseChecker.check("TODO 6 : asTexts(//c:teacher) == [Dupont, Martin, Dupont]",
                asTexts(x, "//c:teacher", doc).equals(List.of("Dupont", "Martin", "Dupont")));
        ExerciseChecker.check("TODO 7 : asNode(//c:nothing) == null et asNode(//c:people) non null",
                asNode(x, "//c:nothing", doc) == null && asNode(x, "//c:people", doc) != null);

        ExerciseChecker.check("TODO 8 : titres", asTexts(x, exprCourseTitles(), doc).equals(List.of("Algorithmique", "Bases de donnees", "Reseaux")));
        ExerciseChecker.check("TODO 9 : somme des credits == 15", asNumber(x, exprCreditsSum(), doc) == 15.0);
        ExerciseChecker.check("TODO 10 : etudiants de C1 == 3, de C3 == 0",
                asNumber(x, exprStudentCount("C1"), doc) == 3.0 && asNumber(x, exprStudentCount("C3"), doc) == 0.0);
        ExerciseChecker.check("TODO 11 : cours de Dupont == [C1, C3]", asTexts(x, exprCoursesTaughtBy("Dupont"), doc).equals(List.of("C1", "C3")));
        ExerciseChecker.check("TODO 12 : titre du dernier cours == Reseaux", asString(x, exprLastCourseTitle(), doc).equals("Reseaux"));
        ExerciseChecker.check("TODO 13 : meilleure note == [18]", asTexts(x, exprBestGrade(), doc).equals(List.of("18")));
        ExerciseChecker.check("TODO 14 : sans email == [Ben]", asTexts(x, exprPeopleWithoutEmail(), doc).equals(List.of("Ben")));
        ExerciseChecker.check("TODO 15 : titres contenant Base", asTexts(x, exprTitlesContaining("Base"), doc).equals(List.of("Bases de donnees")));
        ExerciseChecker.check("TODO 16 : emails commencant par c", asTexts(x, exprEmailsStartingWith("c"), doc).equals(List.of("chloe@campus.fr")));
        ExerciseChecker.check("TODO 17 : cours apres C1 == [C2, C3]", asTexts(x, exprNextCourses("C1"), doc).equals(List.of("C2", "C3")));
        ExerciseChecker.check("TODO 18 : cours de la note 18 == C2", asString(x, exprCourseOfGrade("18"), doc).equals("C2"));
        ExerciseChecker.check("TODO 19 : longueur du titre de C2 == 16", asNumber(x, exprTitleLength("C2"), doc) == 16.0);
        ExerciseChecker.check("TODO 20 : position de C2 == 2, de C3 == 3",
                asNumber(x, exprPositionOfCourse("C2"), doc) == 2.0 && asNumber(x, exprPositionOfCourse("C3"), doc) == 3.0);

        ExerciseChecker.summary();
    }

    // Deja ecrit : c -> urn:campus, g -> urn:campus:grades.
    static final NamespaceContext CTX = new NamespaceContext() {
        private final Map<String, String> map = Map.of("c", Campus.NS, "g", Campus.GRADES);

        @Override
        public String getNamespaceURI(String prefix) {
            return map.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
        }

        @Override
        public String getPrefix(String uri) {
            return map.entrySet().stream().filter(e -> e.getValue().equals(uri)).map(Map.Entry::getKey).findFirst().orElse(null);
        }

        @Override
        public Iterator<String> getPrefixes(String uri) {
            return map.entrySet().stream().filter(e -> e.getValue().equals(uri)).map(Map.Entry::getKey).iterator();
        }
    };
}
