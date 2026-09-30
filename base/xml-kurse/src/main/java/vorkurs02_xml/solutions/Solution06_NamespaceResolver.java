package vorkurs02_xml.solutions;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Corrige de l'exercice 6. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise06_NamespaceResolver.
 */
public class Solution06_NamespaceResolver {

    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    public record QName(String prefix, String local) {
    }

    public record Event(boolean start, String qName, Map<String, String> attributes) {
    }

    public static QName splitQName(String qName) {
        // Un QName a AU PLUS un ':' et jamais en bord : "a:b:c", ":a" et "a:" sont refuses.
        int colon = qName.indexOf(':');
        if (colon < 0) {
            return new QName("", qName);
        }
        if (colon == 0 || colon == qName.length() - 1 || qName.indexOf(':', colon + 1) >= 0) {
            throw new IllegalArgumentException("QName invalide : " + qName);
        }
        return new QName(qName.substring(0, colon), qName.substring(colon + 1));
    }

    public static Map<String, String> declarationsOf(Map<String, String> attributes) {
        // Les declarations sont des attributs "xmlns" / "xmlns:p" ; on applique ici les
        // contraintes de Namespaces 1.0 : pas de liaison vide pour un prefixe, xml et xmlns intouchables.
        Map<String, String> result = new LinkedHashMap<>();
        attributes.forEach((name, uri) -> {
            if (name.equals("xmlns")) {
                result.put("", uri);
            } else if (name.startsWith("xmlns:")) {
                String prefix = name.substring(6);
                if (uri.isEmpty()) {
                    throw new IllegalArgumentException("Liaison vide interdite pour le prefixe " + prefix);
                }
                if (prefix.equals("xmlns") || (prefix.equals("xml") != uri.equals(XML_NS))) {
                    throw new IllegalArgumentException("Prefixe reserve mal utilise : " + prefix + " -> " + uri);
                }
                result.put(prefix, uri);
            }
        });
        return result;
    }

    public static Optional<String> lookup(String prefix, Deque<Map<String, String>> scopes) {
        // On cherche du plus INTERNE (sommet de pile) vers l'exterieur : la liaison la plus proche
        // gagne (rebinding). xmlns="" trouve "" -> "pas de namespace" -> vide.
        if (prefix.equals("xml")) {
            return Optional.of(XML_NS);
        }
        for (Map<String, String> scope : scopes) {
            if (scope.containsKey(prefix)) {
                String uri = scope.get(prefix);
                return uri.isEmpty() ? Optional.empty() : Optional.of(uri);
            }
        }
        return Optional.empty();
    }

    public static String resolveElement(String qName, Deque<Map<String, String>> scopes) {
        // Element : le namespace par defaut S'APPLIQUE aux noms sans prefixe.
        QName q = splitQName(qName);
        return expanded(q, scopes, true);
    }

    public static String resolveAttribute(String qName, Deque<Map<String, String>> scopes) {
        // Attribut : le namespace par defaut NE s'applique PAS (cours 0.2.13 S3) : "id" reste sans namespace.
        QName q = splitQName(qName);
        return expanded(q, scopes, false);
    }

    private static String expanded(QName q, Deque<Map<String, String>> scopes, boolean useDefault) {
        if (q.prefix().isEmpty()) {
            Optional<String> uri = useDefault ? lookup("", scopes) : Optional.empty();
            return uri.map(u -> "{" + u + "}" + q.local()).orElse(q.local());
        }
        String uri = lookup(q.prefix(), scopes)
                .orElseThrow(() -> new IllegalArgumentException("Prefixe non declare : " + q.prefix()));
        return "{" + uri + "}" + q.local();
    }

    public static List<String> resolveDocument(List<Event> events) {
        // La pile de portees suit exactement l'imbrication : push au START (meme une Map vide,
        // pour que le pop du END reste symetrique), pop au END.
        Deque<Map<String, String>> scopes = new ArrayDeque<>();
        List<String> lines = new ArrayList<>();
        for (Event e : events) {
            if (!e.start()) {
                scopes.pop();
                continue;
            }
            scopes.push(declarationsOf(e.attributes()));
            String element = resolveElement(e.qName(), scopes);
            List<String> attrs = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (String name : e.attributes().keySet()) {
                if (name.equals("xmlns") || name.startsWith("xmlns:")) {
                    continue;
                }
                String exp = resolveAttribute(name, scopes);
                // a:id et b:id avec a et b lies a la MEME URI : meme nom etendu -> doublon interdit.
                if (!seen.add(exp)) {
                    throw new IllegalArgumentException("Attribut en double : " + exp);
                }
                attrs.add(exp);
            }
            attrs.sort(null);
            lines.add(element + " " + attrs);
        }
        return lines;
    }

    public static List<String> xsiTypes(List<Event> events) {
        // La VALEUR de xsi:type est elle-meme un QName : 2e resolution, avec les portees de
        // l'element qui porte l'attribut ; un nom sans prefixe prend le namespace par defaut.
        Deque<Map<String, String>> scopes = new ArrayDeque<>();
        List<String> result = new ArrayList<>();
        for (Event e : events) {
            if (!e.start()) {
                scopes.pop();
                continue;
            }
            scopes.push(declarationsOf(e.attributes()));
            for (Map.Entry<String, String> a : e.attributes().entrySet()) {
                // Les declarations xmlns:p ne sont pas des attributs "normaux" : on les saute.
                if (a.getKey().contains(":") && !a.getKey().startsWith("xmlns:")
                        && resolveAttribute(a.getKey(), scopes).equals("{http://www.w3.org/2001/XMLSchema-instance}type")) {
                    result.add(resolveElement(e.qName(), scopes) + " : " + resolveElement(a.getValue(), scopes));
                }
            }
        }
        return result;
    }

    static List<Event> events(Path xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Element root = factory.newDocumentBuilder().parse(xml.toFile()).getDocumentElement();
        List<Event> out = new ArrayList<>();
        collect(root, out);
        return out;
    }

    private static void collect(Element e, List<Event> out) {
        Map<String, String> attrs = new LinkedHashMap<>();
        NamedNodeMap map = e.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr a = (Attr) map.item(i);
            attrs.put(a.getName(), a.getValue());
        }
        out.add(new Event(true, e.getTagName(), attrs));
        for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element child) {
                collect(child, out);
            }
        }
        out.add(new Event(false, e.getTagName(), Map.of()));
    }
}
