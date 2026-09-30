package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige de l'exercice 5. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise05_ContentModelCompiler.
 */
public class Solution05_ContentModelCompiler {

    public static List<String> tokenize(String model) {
        // Les symboles ( ) , | ? * + sont des jetons d'un caractere ; tout le reste (noms,
        // #PCDATA) se lit d'un bloc. Les blancs ne servent qu'a separer : on les jette.
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < model.length()) {
            char c = model.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if ("(),|?*+".indexOf(c) >= 0) {
                tokens.add(String.valueOf(c));
                i++;
            } else {
                int start = i;
                while (i < model.length() && "(),|?*+".indexOf(model.charAt(i)) < 0
                        && !Character.isWhitespace(model.charAt(i))) {
                    i++;
                }
                tokens.add(model.substring(start, i));
            }
        }
        return tokens;
    }

    public static String toRegex(String model) {
        // Descente recursive : une "particule" est un nom ou un groupe, suivie d'un quantificateur
        // optionnel que la regex Java comprend tel quel (? * +). EMPTY et ANY sont des cas a part.
        String trimmed = model.strip();
        if (trimmed.equals("EMPTY")) {
            return "";
        }
        if (trimmed.equals("ANY")) {
            return "(?:[^;]+;)*";
        }
        List<String> tokens = tokenize(trimmed);
        int[] pos = {0};
        String regex = particle(tokens, pos);
        if (pos[0] != tokens.size()) {
            throw new IllegalArgumentException("Jetons en trop dans " + model);
        }
        return regex;
    }

    private static String particle(List<String> tokens, int[] pos) {
        String token = tokens.get(pos[0]++);
        String base;
        if (token.equals("(")) {
            base = group(tokens, pos);
        } else if (token.equals("#PCDATA")) {
            // Le texte n'apparait pas dans la suite des ENFANTS elements : il ne consomme rien.
            base = "";
        } else {
            base = "(?:" + token + ";)";
        }
        if (pos[0] < tokens.size() && "?*+".contains(tokens.get(pos[0]))) {
            base += tokens.get(pos[0]++);
        }
        return base;
    }

    private static String group(List<String> tokens, int[] pos) {
        // Dans un groupe, on ne melange jamais ',' et '|' : le premier separateur lu fixe le type
        // (concatenation ou alternative), puis on lit jusqu'a la ')'.
        List<String> parts = new ArrayList<>();
        parts.add(particle(tokens, pos));
        String separator = null;
        while (!tokens.get(pos[0]).equals(")")) {
            String sep = tokens.get(pos[0]++);
            if (separator != null && !separator.equals(sep)) {
                throw new IllegalArgumentException("Melange de ',' et '|' dans un meme groupe");
            }
            separator = sep;
            parts.add(particle(tokens, pos));
        }
        pos[0]++;
        return "(?:" + String.join("|".equals(separator) ? "|" : "", parts) + ")";
    }

    public static String childSequence(Element element) {
        // Seuls les ENFANTS DIRECTS de type element comptent ; texte, commentaires et petits-enfants
        // ne font pas partie du modele de contenu de CET element.
        StringBuilder out = new StringBuilder();
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                out.append(n.getNodeName()).append(';');
            }
        }
        return out.toString();
    }

    public static boolean hasOnlyWhitespaceText(Element element) {
        // Dans un contenu "elements seulement", l'indentation est toleree mais pas un vrai mot.
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.TEXT_NODE && !n.getNodeValue().isBlank()) {
                return false;
            }
        }
        return true;
    }

    public static boolean allowsText(String model) {
        // Seuls ANY et un modele mixte (#PCDATA...) autorisent du texte non blanc.
        return model.strip().equals("ANY") || model.contains("#PCDATA");
    }

    private static final Pattern ELEMENT_DECL = Pattern.compile("<!ELEMENT\\s+(\\S+)\\s+(.*?)\\s*>", Pattern.DOTALL);

    public static Map<String, String> parseElementDeclarations(String dtd) {
        // On normalise les blancs du modele pour pouvoir l'afficher et le comparer proprement.
        Map<String, String> result = new LinkedHashMap<>();
        Matcher m = ELEMENT_DECL.matcher(dtd);
        while (m.find()) {
            result.put(m.group(1), m.group(2).replaceAll("\\s+", " "));
        }
        return result;
    }

    public static List<String> validate(Document doc, Map<String, String> decls) {
        // Parcours de TOUS les elements dans l'ordre du document ; pour chacun, 3 questions
        // independantes, chacune deja emballee dans sa boite magique.
        List<String> errors = new ArrayList<>();
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element e = (Element) all.item(i);
            String model = decls.get(e.getTagName());
            if (model == null) {
                errors.add(e.getTagName() + " -> non declare");
                continue;
            }
            if (!Pattern.matches(toRegex(model), childSequence(e))) {
                errors.add(e.getTagName() + " -> enfants");
            } else if (!allowsText(model) && !hasOnlyWhitespaceText(e)) {
                errors.add(e.getTagName() + " -> texte");
            }
        }
        return errors;
    }

    static boolean jdkSaysValid(Path xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        boolean[] valid = {true};
        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) {
                valid[0] = false;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        builder.parse(xml.toFile());
        return valid[0];
    }
}
