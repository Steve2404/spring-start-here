package vorkurs02_xml.projects.p04_dtd.solution;

import vorkurs02_xml.projects.p04_dtd.Data;
import xmlkit.XmlKit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du projet 4 : le validateur DTD maison (0.2.11). Les modeles de contenu deviennent des
 * expressions regulieres sur la suite des enfants ; les ATTLIST se verifient attribut par attribut.
 */
public class DtdCheck {

    /** Un element de l'arbre maison : juste ce qu'il faut pour valider. */
    static final class Element {
        final String name;
        final Map<String, String> attributes = new LinkedHashMap<>();
        final List<Element> children = new ArrayList<>();
        final StringBuilder text = new StringBuilder();
        boolean hasContent; // au moins un caractere ou un enfant entre les balises

        Element(String name) {
            this.name = name;
        }
    }

    /** Une declaration d'attribut : son type (CDATA, ID, IDREF ou une liste) et sa regle par defaut. */
    record AttDecl(String element, String name, String type, List<String> values, String mode, String value) {
    }

    /** La DTD fusionnee (interne puis externe), et les erreurs nees de la fusion elle-meme. */
    record Dtd(Map<String, String> elements, Map<String, AttDecl> attributes, List<String> errors) {
    }

    public static void main(String[] args) {
        Dtd external = merge("", Data.CATALOG_DTD);
        external.elements().forEach((name, model) ->
                System.out.println("MODELE " + name + " : " + model + " -> [" + toRegex(model) + "]"));

        for (Data.Submission s : Data.DOCUMENTS) {
            List<String> errors = validate(s.text());
            String mine = errors.isEmpty() ? "VALIDE" : "INVALIDE " + errors.size() + " : " + String.join(" ; ", errors);
            String parser = XmlKit.validateDtd(s.text(), Map.of("catalog.dtd", Data.CATALOG_DTD));
            boolean agree = errors.isEmpty() == parser.equals("VALID");
            System.out.println(s.id() + " " + mine + " | parseur " + parser + " | accord " + (agree ? "oui" : "non"));
        }

        defaults("V01", "catalog", "/catalog");
        defaults("V09", "item", "//item[1]");
    }

    // ---------- lecture de la DTD ----------

    private static final Pattern ELEMENT = Pattern.compile("<!ELEMENT\\s+(\\S+)\\s+(.*?)\\s*>", Pattern.DOTALL);
    private static final Pattern ATTLIST = Pattern.compile("<!ATTLIST\\s+(\\S+)\\s+(.*?)>", Pattern.DOTALL);
    private static final Pattern ATTDEF = Pattern.compile(
            "(\\S+)\\s+(CDATA|ID|IDREF|\\([^)]*\\))\\s+(#REQUIRED|#IMPLIED|#FIXED\\s+\"[^\"]*\"|\"[^\"]*\")");

    static Dtd merge(String internal, String external) {
        // Le sous-ensemble INTERNE est lu en premier. Pour un attribut, la PREMIERE declaration gagne :
        // c'est ainsi que l'interne "l'emporte". Mais un ELEMENT declare deux fois est une erreur (VC).
        Map<String, String> elements = new LinkedHashMap<>();
        Map<String, AttDecl> attributes = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        for (String part : List.of(internal, external)) {
            String dtd = part.replaceAll("(?s)<!--.*?-->", "");
            Matcher e = ELEMENT.matcher(dtd);
            while (e.find()) {
                String model = e.group(2).replaceAll("\\s+", " ");
                if (elements.putIfAbsent(e.group(1), model) != null) {
                    errors.add(e.group(1) + " -> declare deux fois");
                }
            }
            Matcher a = ATTLIST.matcher(dtd);
            while (a.find()) {
                Matcher d = ATTDEF.matcher(a.group(2));
                while (d.find()) {
                    String type = d.group(2);
                    List<String> values = type.startsWith("(") ? List.of(type.substring(1, type.length() - 1).split("\\s*\\|\\s*")) : List.of();
                    String def = d.group(3);
                    String mode = def.startsWith("#FIXED") ? "#FIXED" : def.startsWith("#") ? def : "DEFAULT";
                    String value = def.contains("\"") ? def.substring(def.indexOf('"') + 1, def.lastIndexOf('"')) : null;
                    attributes.putIfAbsent(a.group(1) + "@" + d.group(1),
                            new AttDecl(a.group(1), d.group(1), type.startsWith("(") ? "LISTE" : type, values, mode, value));
                }
            }
        }
        return new Dtd(elements, attributes, errors);
    }

    // ---------- modele de contenu -> expression reguliere ----------

    static String toRegex(String model) {
        // Les enfants d'un element s'ecrivent "nom;nom;..." : un modele devient une regex sur ce texte.
        String m = model.strip();
        if (m.equals("EMPTY")) {
            return "";
        }
        if (m.equals("ANY")) {
            return "(?:[^;]+;)*";
        }
        List<String> tokens = tokenize(m);
        int[] pos = {0};
        return particle(tokens, pos);
    }

    static List<String> tokenize(String model) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("[(),|?*+]|[^\\s(),|?*+]+").matcher(model);
        while (m.find()) {
            tokens.add(m.group());
        }
        return tokens;
    }

    private static String particle(List<String> tokens, int[] pos) {
        // Descente recursive : un nom ou un groupe, puis un quantificateur que la regex comprend tel quel.
        String token = tokens.get(pos[0]++);
        String base;
        if (token.equals("(")) {
            List<String> parts = new ArrayList<>();
            parts.add(particle(tokens, pos));
            String sep = "";
            while (!tokens.get(pos[0]).equals(")")) {
                sep = tokens.get(pos[0]++);
                parts.add(particle(tokens, pos));
            }
            pos[0]++;
            base = "(?:" + String.join(sep.equals("|") ? "|" : "", parts) + ")";
        } else if (token.equals("#PCDATA")) {
            // Le texte n'est pas un enfant ELEMENT : il ne consomme rien dans "nom;nom;".
            base = "";
        } else {
            base = "(?:" + token + ";)";
        }
        if (pos[0] < tokens.size() && "?*+".contains(tokens.get(pos[0]))) {
            base += tokens.get(pos[0]++);
        }
        return base;
    }

    // ---------- l'arbre maison ----------

    record Parsed(String doctype, String internal, Element root) {
    }

    static Parsed parse(String text) {
        String doctype = null;
        String internal = "";
        List<Element> stack = new ArrayList<>();
        Element root = null;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != '<') {
                int next = text.indexOf('<', i);
                int stop = next < 0 ? text.length() : next;
                if (!stack.isEmpty()) {
                    Element top = stack.get(stack.size() - 1);
                    top.text.append(text, i, stop);
                    top.hasContent = true;
                }
                i = stop;
            } else if (text.startsWith("<?", i)) {
                i = text.indexOf("?>", i) + 2;
            } else if (text.startsWith("<!DOCTYPE", i)) {
                int bracket = text.indexOf('[', i);
                int close = text.indexOf('>', i);
                doctype = text.substring(i + 9, close).strip().split("\\s+")[0];
                if (bracket >= 0 && bracket < close) {
                    int end = text.indexOf("]>", bracket);
                    internal = text.substring(bracket + 1, end);
                    i = end + 2;
                } else {
                    i = close + 1;
                }
            } else if (text.startsWith("</", i)) {
                stack.remove(stack.size() - 1);
                i = text.indexOf('>', i) + 1;
            } else {
                int end = text.indexOf('>', i);
                boolean empty = text.charAt(end - 1) == '/';
                String inside = text.substring(i + 1, empty ? end - 1 : end).strip();
                String[] parts = inside.split("\\s+", 2);
                Element e = new Element(parts[0]);
                if (parts.length > 1) {
                    Matcher m = Pattern.compile("(\\S+?)\\s*=\\s*\"([^\"]*)\"").matcher(parts[1]);
                    while (m.find()) {
                        e.attributes.put(m.group(1), m.group(2));
                    }
                }
                if (stack.isEmpty()) {
                    root = e;
                } else {
                    Element top = stack.get(stack.size() - 1);
                    top.children.add(e);
                    top.hasContent = true;
                }
                if (!empty) {
                    stack.add(e);
                }
                i = end + 1;
            }
        }
        return new Parsed(doctype, internal, root);
    }

    // ---------- validation ----------

    static List<String> validate(String text) {
        Parsed p = parse(text);
        Dtd dtd = merge(p.internal(), Data.CATALOG_DTD);
        List<String> errors = new ArrayList<>(dtd.errors());
        if (!p.root().name.equals(p.doctype())) {
            errors.add("racine " + p.root().name + " != DOCTYPE " + p.doctype());
        }
        Set<String> ids = new HashSet<>();
        List<String> refs = new ArrayList<>(); // les IDREF, verifies A LA FIN : la cible peut venir apres
        check(p.root(), dtd, errors, ids, refs);
        for (String ref : refs) {
            String value = ref.substring(ref.indexOf('=') + 1);
            if (!ids.contains(value)) {
                errors.add(ref + " -> IDREF sans cible");
            }
        }
        return errors;
    }

    private static void check(Element e, Dtd dtd, List<String> errors, Set<String> ids, List<String> refs) {
        String model = dtd.elements().get(e.name);
        if (model == null) {
            errors.add(e.name + " -> non declare");
        } else {
            StringBuilder seq = new StringBuilder();
            e.children.forEach(c -> seq.append(c.name).append(';'));
            boolean textAllowed = model.equals("ANY") || model.contains("#PCDATA");
            if (!Pattern.matches(toRegex(model), seq)) {
                errors.add(e.name + " -> enfants");
            } else if (model.equals("EMPTY") ? e.hasContent : !textAllowed && !e.text.toString().isBlank()) {
                // EMPTY : RIEN du tout, pas meme un blanc. Contenu "elements" : l'indentation est toleree.
                errors.add(e.name + " -> texte");
            }
        }
        checkAttributes(e, dtd, errors, ids, refs);
        for (Element c : e.children) {
            check(c, dtd, errors, ids, refs);
        }
    }

    private static void checkAttributes(Element e, Dtd dtd, List<String> errors, Set<String> ids, List<String> refs) {
        e.attributes.forEach((name, value) -> {
            if (!dtd.attributes().containsKey(e.name + "@" + name)) {
                errors.add(e.name + "@" + name + " -> non declare");
            }
        });
        for (AttDecl d : dtd.attributes().values()) {
            if (!d.element().equals(e.name)) {
                continue;
            }
            String value = e.attributes.get(d.name());
            String label = e.name + "@" + d.name() + "=" + value;
            if (value == null) {
                if (d.mode().equals("#REQUIRED")) {
                    errors.add(e.name + "@" + d.name() + " -> manquant");
                }
                continue;
            }
            if (d.type().equals("LISTE") && !d.values().contains(value)) {
                errors.add(label + " -> hors liste");
            }
            if (d.mode().equals("#FIXED") && !value.equals(d.value())) {
                errors.add(label + " -> fixe " + d.value());
            }
            if (d.type().equals("ID")) {
                // Un ID est un NOM XML (pas de chiffre en tete) et il est unique dans TOUT le document.
                if (!value.matches("[A-Za-z_:][A-Za-z0-9._:-]*")) {
                    errors.add(label + " -> ID invalide");
                } else if (!ids.add(value)) {
                    errors.add(label + " -> ID double");
                }
            }
            if (d.type().equals("IDREF")) {
                refs.add(label);
            }
        }
    }

    // ---------- valeurs par defaut ----------

    static void defaults(String id, String element, String xpath) {
        String text = Data.DOCUMENTS.stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow().text();
        Parsed p = parse(text);
        Dtd dtd = merge(p.internal(), Data.CATALOG_DTD);
        Element target = find(p.root(), element);
        // Le parseur AJOUTE les attributs absents qui ont une valeur par defaut ou #FIXED.
        Map<String, String> all = new LinkedHashMap<>(target.attributes);
        for (AttDecl d : dtd.attributes().values()) {
            if (d.element().equals(element) && d.value() != null) {
                all.putIfAbsent(d.name(), d.value());
            }
        }
        // Le parseur de XmlKit ne lit PAS la DTD externe en lecture simple (comme un processeur non validant
        // en a le droit) : il n'applique que les defauts du sous-ensemble INTERNE.
        StringBuilder line = new StringBuilder("DEFAUTS " + id + " " + element + " :");
        StringBuilder parser = new StringBuilder(" | parseur :");
        all.forEach((k, v) -> {
            line.append(' ').append(k).append('=').append(v);
            String seen = XmlKit.value(text, "string(" + xpath + "/@" + k + ")");
            parser.append(' ').append(k).append('=').append(seen.isEmpty() ? "absent" : seen);
        });
        System.out.println(line.toString() + parser);
    }

    private static Element find(Element e, String name) {
        if (e.name.equals(name)) {
            return e;
        }
        for (Element c : e.children) {
            Element f = find(c, name);
            if (f != null) {
                return f;
            }
        }
        return null;
    }
}
