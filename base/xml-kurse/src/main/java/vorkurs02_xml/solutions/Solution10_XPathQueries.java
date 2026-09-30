package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Corrige de l'exercice 10. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise10_XPathQueries.
 */
public class Solution10_XPathQueries {

    public static NamespaceContext namespaceContext(Map<String, String> bindings) {
        // XPath ne lit PAS les xmlns du document : c'est NOUS qui lions nos prefixes a des URI.
        // Prefixe inconnu -> NULL_NS_URI (""), comme le demande le contrat de l'interface.
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return bindings.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return bindings.entrySet().stream().filter(e -> e.getValue().equals(namespaceURI))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return bindings.entrySet().stream().filter(e -> e.getValue().equals(namespaceURI))
                        .map(Map.Entry::getKey).iterator();
            }
        };
    }

    public static List<Node> nodes(Document doc, String expression, NamespaceContext context)
            throws XPathExpressionException {
        // NODESET donne une NodeList (pas une java.util.List) : on la recopie pour pouvoir
        // utiliser stream() et equals() cote appelant.
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (context != null) {
            xpath.setNamespaceContext(context);
        }
        NodeList list = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            result.add(list.item(i));
        }
        return result;
    }

    public static double number(Document doc, String expression) throws XPathExpressionException {
        // NUMBER rend un Double : sum(), count() et les comparaisons numeriques arrivent ici.
        return (Double) XPathFactory.newInstance().newXPath().evaluate(expression, doc, XPathConstants.NUMBER);
    }

    public static String exprNamesInDepartment(String department) {
        // Chemin ABSOLU avec des '/' : seulement les employes DIRECTS du departement (pas Dev).
        return "/company/department[@name='" + department + "']/employee/name";
    }

    public static String exprSecondPerDepartment() {
        // [2] s'applique au step "employee" : le 2e employe DE CHAQUE parent department.
        return "//department/employee[2]/name";
    }

    public static String exprSecondOverall() {
        // Les parentheses d'abord construisent LA liste globale, puis [2] y choisit le 2e.
        return "(//employee)[2]/name";
    }

    public static String exprTotalSalary(String department) {
        // '//' apres le departement descend aussi dans ses sous-departements (Dev est dans IT).
        return "sum(//department[@name='" + department + "']//employee/salary)";
    }

    public static String exprManagersWithoutReports() {
        // Comparaison de node-set XPath 1.0 : "A = B" est vrai si UNE valeur de A egale UNE de B.
        // not(...) renverse donc en "personne ne me reporte". Dans le predicat, @id est celui du manager.
        return "//employee[@role='manager'][not(//employee/@reportsTo = @id)]/name";
    }

    public static String exprNearestDepartment(String employeeId) {
        // ancestor est un axe INVERSE : [1] y designe le plus PROCHE (Dev), pas le premier du document.
        return "//employee[@id='" + employeeId + "']/ancestor::department[1]/@name";
    }

    public static String exprNormalizedName(String employeeId) {
        // normalize-space retire les blancs de bord ET fusionne les blancs internes.
        return "normalize-space(//employee[@id='" + employeeId + "']/name)";
    }

    public static String exprTopReviewed() {
        // Le prefixe h n'existe pas dans le document (qui ecrit r:) : il vient de NOTRE NamespaceContext.
        return "//h:review[h:score >= 4]/@employee";
    }
}
