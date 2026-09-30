package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.List;
import java.util.Map;

/**
 * EXERCICE 10 - XPath 1.0 : ecrire les requetes, et les pieges de position, d'axe et de namespace (niveau : avance)
 * =============================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex10/company.xml : une entreprise, des departements (dont Dev
 * IMBRIQUE dans IT), des employes, et un bloc d'evaluations dans le
 * namespace urn:hr:reviews (ecrit avec le prefixe r:). Ouvre le fichier.
 *
 * Attention : javax.xml.xpath du JDK implemente XPath 1.0 (pas 2.0/3.1).
 * Pas de sequences typees ni de distinct-values : des node-sets, des
 * nombres (double), des chaines et des booleens.
 *
 * TODO 1 a 3 : l'outillage Java. TODO 4 a 11 : tu ecris des EXPRESSIONS
 * XPath (des String) ; main() les evalue sur le vrai document.
 *
 *
 * ==================================================================
 * TODO 1 : namespaceContext(bindings)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * XPath ne regarde PAS les xmlns du document (0.2.16 S6) : dans une
 * expression, "h:review" veut dire "l'URI que TU as liee a h". Tu donnes
 * ce dictionnaire a XPath sous forme d'un NamespaceContext. Prefixe
 * inconnu -> XMLConstants.NULL_NS_URI ("").
 *
 * -- Le plan --
 *
 *   1. Classe anonyme NamespaceContext.
 *   2. getNamespaceURI(p) : la Map, sinon "".
 *   3. getPrefix / getPrefixes : le chemin inverse (peu utilises ici).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 2 : nodes(doc, expression, context)
 * ==================================================================
 *
 * Evalue l'expression en XPathConstants.NODESET (context peut etre null :
 * alors pas de setNamespaceContext) et recopie la NodeList dans une List.
 *
 *
 * ==================================================================
 * TODO 3 : number(doc, expression)
 * ==================================================================
 *
 * Evalue en XPathConstants.NUMBER (le resultat est un Double).
 *
 *
 * ==================================================================
 * TODO 4 : exprNamesInDepartment(department)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Les <name> des employes DIRECTS du departement de premier niveau donne
 * (pas ceux des sous-departements). Chemin absolu depuis /company.
 *
 * -- Essayons a la main --
 *
 *   "IT"    -> Alice, Bob, Chloe   (Dan et Eva sont dans Dev : exclus)
 *   "Sales" -> Farid, "  Gina   Lopez "
 *
 *
 * ==================================================================
 * TODO 5 : exprSecondPerDepartment()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "Le 2e de chaque rang" : dans CHAQUE departement (a tout niveau), le
 * 2e employe direct. Un predicat [2] colle a un step compte les freres
 * de ce step (0.2.16 S4).
 *
 * -- Essayons a la main --
 *
 *   IT -> Bob ; Dev -> Eva ; Sales -> Gina ; Legal (1 seul) -> rien
 *
 *
 * ==================================================================
 * TODO 6 : exprSecondOverall()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "Le 2e de toute l'ecole" : d'abord la liste de TOUS les employes du
 * document, ensuite le 2e. Il faut des parentheses pour que [2]
 * s'applique a la liste entiere et non a chaque parent.
 *
 * -- Essayons a la main --
 *
 *   -> Bob (et UN seul resultat, contrairement au TODO 5)
 *
 *
 * ==================================================================
 * TODO 7 : exprTotalSalary(department)
 * ==================================================================
 *
 * Somme des salaires du departement ET de ses sous-departements.
 *   "IT" -> 6000+4000+4200+5500+4800 = 24500 ; "Dev" -> 10300 ; "Sales" -> 8900
 *
 *
 * ==================================================================
 * TODO 8 : exprManagersWithoutReports()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Les <name> des managers (role='manager') a qui PERSONNE ne reporte
 * (aucun @reportsTo egal a leur @id). XPath 1.0 n'a pas de variable pour
 * "le manager courant", mais dans un predicat, @id designe l'attribut du
 * noeud teste. Et une comparaison "node-set = node-set" est vraie des
 * qu'UNE paire de valeurs est egale : not(...) la retourne.
 *
 * -- Essayons a la main --
 *
 *   e1 : e2, e3, e4 reportent -> exclu ; e4 : e5 -> exclu ;
 *   e6 (Farid), e8 (Hugo) : personne -> [Farid, Hugo]
 *
 *
 * ==================================================================
 * TODO 9 : exprNearestDepartment(employeeId)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le @name du departement le PLUS PROCHE au-dessus de l'employe. Sur un
 * axe inverse (ancestor, preceding...), [1] designe le plus PROCHE.
 * Piege verifie dans main() : sans [1], string(...) du node-set rend le
 * PREMIER DANS L'ORDRE DU DOCUMENT, donc "IT" pour e5, pas "Dev".
 *
 *   "e5" -> "Dev" ; "e2" -> "IT"
 *
 *
 * ==================================================================
 * TODO 10 : exprNormalizedName(employeeId)
 * ==================================================================
 *
 * Une expression qui rend une CHAINE : le nom sans blancs de bord et avec
 * les blancs internes fusionnes.  "e7" -> "Gina Lopez"
 *
 *
 * ==================================================================
 * TODO 11 : exprTopReviewed()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Les attributs @employee des evaluations dont le score est >= 4. Le
 * document ecrit r:review, mais TOI tu utilises le prefixe h (lie a
 * urn:hr:reviews par le NamespaceContext du TODO 1). Piege verifie :
 * "//review" sans prefixe ne trouve RIEN (review sans prefixe = sans
 * namespace en XPath 1.0).
 *
 *   -> [e2, e7] ; et reutilise dans une requete plus grosse :
 *      //employee[@id = TON_EXPRESSION]/name -> [Bob, "  Gina   Lopez "]
 *
 *
 * -- Ces plans ont-ils besoin d'une boite magique separee ? --
 *
 * Les TODO 4 a 11 sont des expressions d'une ligne. Les boites sont
 * les TODO 1 a 3, reutilisees par tous les tests.
 *
 *
 * Exemple a verifier : voir chaque TODO ; tous les resultats sont compares dans main().
 *
 *
 * Indices techniques (a lire seulement si le plan est clair mais que
 * la traduction bloque) :
 *
 *   - XPath x = XPathFactory.newInstance().newXPath(); x.setNamespaceContext(ctx);
 *   - (NodeList) x.evaluate(expr, doc, XPathConstants.NODESET) ; (Double) ... XPathConstants.NUMBER
 *   - map.getOrDefault(prefix, XMLConstants.NULL_NS_URI)
 *   - /a/b[@name='X']/c    //a/b[2]    (//b)[2]    sum(//a[@n='X']//s)
 *   - //e[@role='manager'][not(//e/@reportsTo = @id)]
 *   - ancestor::department[1]/@name    normalize-space(...)    //h:review[h:score >= 4]/@employee
 */
public class Exercise10_XPathQueries {

    public static NamespaceContext namespaceContext(Map<String, String> bindings) {
        throw new UnsupportedOperationException("TODO 1 : implementer namespaceContext()");
    }

    public static List<Node> nodes(Document doc, String expression, NamespaceContext context)
            throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 2 : implementer nodes()");
    }

    public static double number(Document doc, String expression) throws XPathExpressionException {
        throw new UnsupportedOperationException("TODO 3 : implementer number()");
    }

    public static String exprNamesInDepartment(String department) {
        throw new UnsupportedOperationException("TODO 4 : implementer exprNamesInDepartment()");
    }

    public static String exprSecondPerDepartment() {
        throw new UnsupportedOperationException("TODO 5 : implementer exprSecondPerDepartment()");
    }

    public static String exprSecondOverall() {
        throw new UnsupportedOperationException("TODO 6 : implementer exprSecondOverall()");
    }

    public static String exprTotalSalary(String department) {
        throw new UnsupportedOperationException("TODO 7 : implementer exprTotalSalary()");
    }

    public static String exprManagersWithoutReports() {
        throw new UnsupportedOperationException("TODO 8 : implementer exprManagersWithoutReports()");
    }

    public static String exprNearestDepartment(String employeeId) {
        throw new UnsupportedOperationException("TODO 9 : implementer exprNearestDepartment()");
    }

    public static String exprNormalizedName(String employeeId) {
        throw new UnsupportedOperationException("TODO 10 : implementer exprNormalizedName()");
    }

    public static String exprTopReviewed() {
        throw new UnsupportedOperationException("TODO 11 : implementer exprTopReviewed()");
    }

    public static void main(String[] args) throws Exception {
        NamespaceContext hr = namespaceContext(Map.of("h", "urn:hr:reviews"));
        ExerciseChecker.check("namespaceContext : h -> urn:hr:reviews, inconnu -> \"\"",
                hr.getNamespaceURI("h").equals("urn:hr:reviews") && hr.getNamespaceURI("zz").isEmpty());
        ExerciseChecker.check("namespaceContext : getPrefix(urn:hr:reviews) == h", "h".equals(hr.getPrefix("urn:hr:reviews")));

        Document doc = parse();
        ExerciseChecker.check("nodes(//department) : 4 departements", nodes(doc, "//department", null).size() == 4);
        ExerciseChecker.check("number(count(//employee)) == 8", number(doc, "count(//employee)") == 8.0);

        ExerciseChecker.check("NamesInDepartment(IT) == [Alice, Bob, Chloe]",
                texts(doc, exprNamesInDepartment("IT"), null).equals(List.of("Alice", "Bob", "Chloe")));
        ExerciseChecker.check("NamesInDepartment(Sales) == [Farid,   Gina   Lopez ]",
                texts(doc, exprNamesInDepartment("Sales"), null).equals(List.of("Farid", "  Gina   Lopez ")));

        ExerciseChecker.check("SecondPerDepartment == [Bob, Eva,   Gina   Lopez ]",
                texts(doc, exprSecondPerDepartment(), null).equals(List.of("Bob", "Eva", "  Gina   Lopez ")));
        ExerciseChecker.check("SecondOverall == [Bob] (un seul !)", texts(doc, exprSecondOverall(), null).equals(List.of("Bob")));

        ExerciseChecker.check("TotalSalary(IT) == 24500 (Dev compris)", number(doc, exprTotalSalary("IT")) == 24500.0);
        ExerciseChecker.check("TotalSalary(Dev) == 10300", number(doc, exprTotalSalary("Dev")) == 10300.0);
        ExerciseChecker.check("TotalSalary(Sales) == 8900", number(doc, exprTotalSalary("Sales")) == 8900.0);

        ExerciseChecker.check("ManagersWithoutReports == [Farid, Hugo]",
                texts(doc, exprManagersWithoutReports(), null).equals(List.of("Farid", "Hugo")));

        ExerciseChecker.check("NearestDepartment(e5) == Dev", string(doc, exprNearestDepartment("e5")).equals("Dev"));
        ExerciseChecker.check("NearestDepartment(e2) == IT", string(doc, exprNearestDepartment("e2")).equals("IT"));
        ExerciseChecker.check("le piege : sans [1], string(ancestor::department/@name) de e5 == IT (ordre du document)",
                string(doc, "//employee[@id='e5']/ancestor::department/@name").equals("IT"));

        ExerciseChecker.check("NormalizedName(e7) == Gina Lopez", string(doc, exprNormalizedName("e7")).equals("Gina Lopez"));

        ExerciseChecker.check("le piege : //review sans prefixe -> 0 noeud", nodes(doc, "//review", hr).isEmpty());
        ExerciseChecker.check("TopReviewed == [e2, e7]", texts(doc, exprTopReviewed(), hr).equals(List.of("e2", "e7")));
        ExerciseChecker.check("//employee[@id = TopReviewed]/name == [Bob,   Gina   Lopez ]",
                texts(doc, "//employee[@id = " + exprTopReviewed() + "]/name", hr).equals(List.of("Bob", "  Gina   Lopez ")));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Document parse() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(Fixtures.path("ex10/company.xml").toFile());
    }

    private static List<String> texts(Document doc, String expression, NamespaceContext ctx) throws Exception {
        return nodes(doc, expression, ctx).stream().map(Node::getTextContent).toList();
    }

    private static String string(Document doc, String expression) throws Exception {
        return (String) XPathFactory.newInstance().newXPath().evaluate(expression, doc, XPathConstants.STRING);
    }
}
