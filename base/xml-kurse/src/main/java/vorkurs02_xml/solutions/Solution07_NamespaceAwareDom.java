package vorkurs02_xml.solutions;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Corrige de l'exercice 7. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise07_NamespaceAwareDom.
 */
public class Solution07_NamespaceAwareDom {

    public static final String XSI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
    public static final String INVOICE = "urn:shop:invoice";

    public static DocumentBuilder newNamespaceAwareBuilder() throws Exception {
        // DocumentBuilderFactory n'est PAS namespace-aware par defaut : sans cette ligne,
        // getNamespaceURI() et getLocalName() rendent null.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
    }

    public static String expandedName(Node node) {
        // L'identite d'un nom = URI + nom local ; le prefixe (getPrefix / getNodeName) n'y est pas.
        String uri = node.getNamespaceURI();
        return uri == null ? node.getLocalName() : "{" + uri + "}" + node.getLocalName();
    }

    public static String xsiType(Element element) {
        // La valeur "m:Euro" est un QName : lookupNamespaceURI fait pour nous la recherche dans
        // les portees englobantes (ce qu'on a code a la main dans l'exercice 6). null = defaut.
        if (!element.hasAttributeNS(XSI, "type")) {
            return null;
        }
        String value = element.getAttributeNS(XSI, "type");
        int colon = value.indexOf(':');
        String prefix = colon < 0 ? null : value.substring(0, colon);
        String local = value.substring(colon + 1);
        String uri = element.lookupNamespaceURI(prefix);
        return uri == null ? local : "{" + uri + "}" + local;
    }

    public static String signature(Element element) {
        // Forme canonique recursive : seuls les noms ETENDUS, les valeurs et le texte utile
        // comptent. Les declarations xmlns, les commentaires et l'indentation disparaissent.
        List<String> attrs = new ArrayList<>();
        NamedNodeMap map = element.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr a = (Attr) map.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) {
                continue;
            }
            // xsi:type porte un QName : on compare sa valeur RESOLUE, pas le texte brut "m:Euro".
            boolean isType = XSI.equals(a.getNamespaceURI()) && "type".equals(a.getLocalName());
            attrs.add(expandedName(a) + "=" + (isType ? xsiType(element) : a.getValue()));
        }
        attrs.sort(null);
        StringBuilder children = new StringBuilder();
        for (Node c = element.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element child) {
                children.append(signature(child));
            } else if (c.getNodeType() == Node.TEXT_NODE && !c.getNodeValue().isBlank()) {
                children.append('\'').append(c.getNodeValue().strip()).append('\'');
            }
        }
        return expandedName(element) + "{" + String.join(",", attrs) + "}[" + children + "]";
    }

    public static boolean sameIgnoringPrefixes(Path a, Path b) throws Exception {
        // Deux fichiers "disent la meme chose" si leurs signatures canoniques sont egales.
        DocumentBuilder builder = newNamespaceAwareBuilder();
        return signature(builder.parse(a.toFile()).getDocumentElement())
                .equals(signature(builder.parse(b.toFile()).getDocumentElement()));
    }

    public static Map<String, SortedSet<String>> prefixesByNamespace(Document doc) {
        // On regarde les NOMS reellement ecrits (elements + attributs), pas les declarations :
        // une URI peut avoir plusieurs prefixes, et "" pour le namespace par defaut.
        Map<String, SortedSet<String>> result = new TreeMap<>();
        NodeList all = doc.getElementsByTagNameNS("*", "*");
        for (int i = 0; i < all.getLength(); i++) {
            Element e = (Element) all.item(i);
            record(result, e);
            NamedNodeMap map = e.getAttributes();
            for (int k = 0; k < map.getLength(); k++) {
                Node a = map.item(k);
                if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) {
                    record(result, a);
                }
            }
        }
        return result;
    }

    private static void record(Map<String, SortedSet<String>> result, Node node) {
        if (node.getNamespaceURI() != null) {
            String prefix = node.getPrefix() == null ? "" : node.getPrefix();
            result.computeIfAbsent(node.getNamespaceURI(), u -> new TreeSet<>()).add(prefix);
        }
    }

    public static BigDecimal totalAmount(Document doc) {
        // getElementsByTagNameNS(URI, local) marche quel que soit le prefixe choisi par l'auteur ;
        // BigDecimal evite les 19.749999... d'un double.
        NodeList amounts = doc.getElementsByTagNameNS(INVOICE, "amount");
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < amounts.getLength(); i++) {
            total = total.add(new BigDecimal(amounts.item(i).getTextContent().strip()));
        }
        return total;
    }
}
