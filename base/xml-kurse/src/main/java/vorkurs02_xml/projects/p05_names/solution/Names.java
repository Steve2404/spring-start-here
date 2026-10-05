package vorkurs02_xml.projects.p05_names.solution;

import vorkurs02_xml.projects.p05_names.Data;
import xmlkit.XmlKit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corrige du projet 5 : le resolveur de noms (0.2.12 -> 0.2.13). Une pile de portees suit
 * l'imbrication ; chaque QName devient un nom etendu {uri}local.
 */
public class Names {

    static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /** Un evenement de lecture : une ouverture (avec ses attributs bruts), une fermeture, ou du texte. */
    record Event(Kind kind, String qName, Map<String, String> attributes, String text) {
    }

    enum Kind { START, END, TEXT }

    /** Une violation des regles des namespaces. */
    static final class NsError extends RuntimeException {
        NsError(String message) {
            super(message);
        }
    }

    /** Un element resolu : son nom etendu, ses attributs etendus (sans les declarations), ses portees visibles. */
    record Resolved(String name, Map<String, String> attributes, Map<String, String> inScope, String text) {
    }

    public static void main(String[] args) {
        List<Resolved> invoice = resolve(events(Data.INVOICE));
        for (int n = 1; n <= invoice.size(); n++) {
            Resolved r = invoice.get(n - 1);
            String uri = XmlKit.value(Data.INVOICE, "namespace-uri((//*)[" + n + "])");
            String local = XmlKit.value(Data.INVOICE, "local-name((//*)[" + n + "])");
            String parser = uri.isEmpty() ? local : "{" + uri + "}" + local;
            StringBuilder line = new StringBuilder("N" + n + " " + r.name());
            r.attributes().keySet().stream().sorted().forEach(a -> line.append(" @").append(a));
            System.out.println(line + " | parseur " + (parser.equals(r.name()) ? "identique" : "DIFFERENT " + parser));
        }
        for (Resolved r : invoice) {
            if (r.name().endsWith("}tax") || r.name().equals("note")) {
                StringBuilder line = new StringBuilder("PORTEE " + r.name() + " :");
                r.inScope().forEach((p, u) -> line.append(' ').append(p.isEmpty() ? "(defaut)" : p).append('=').append(u));
                System.out.println(line);
            }
            String type = r.attributes().get("{" + XSI_NS + "}type");
            if (type != null) {
                System.out.println("XSI " + r.name() + " : " + type);
            }
            String locations = r.attributes().get("{" + XSI_NS + "}schemaLocation");
            if (locations != null) {
                // schemaLocation = des PAIRES "namespace emplacement" : un indice, pas une obligation.
                String[] parts = locations.trim().split("\\s+");
                List<String> pairs = new ArrayList<>();
                for (int k = 0; k + 1 < parts.length; k += 2) {
                    pairs.add(parts[k] + " -> " + parts[k + 1]);
                }
                System.out.println("SCHEMAS " + String.join(" ; ", pairs));
            }
        }

        for (Data.Submission s : Data.CASES) {
            String mine;
            try {
                resolve(events(s.text()));
                mine = "OK";
            } catch (NsError e) {
                mine = e.getMessage();
            }
            System.out.println(s.id() + " " + mine + " | XML 1.0 " + XmlKit.wellFormed(s.text()) + " | namespaces " + XmlKit.wellFormedNs(s.text()));
        }

        List<String> a = signature(Data.ORDER_A);
        System.out.println("EGAL A B : " + compare(a, signature(Data.ORDER_B)));
        System.out.println("EGAL A C : " + compare(a, signature(Data.ORDER_C)));
    }

    // ---------- lecture ----------

    private static final Pattern ATTR = Pattern.compile("([^\\s=]+)\\s*=\\s*([\"'])(.*?)\\2", Pattern.DOTALL);

    static List<Event> events(String text) {
        List<Event> out = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != '<') {
                int next = text.indexOf('<', i);
                int stop = next < 0 ? text.length() : next;
                out.add(new Event(Kind.TEXT, null, Map.of(), text.substring(i, stop)));
                i = stop;
            } else if (text.startsWith("<?", i)) {
                i = text.indexOf("?>", i) + 2;
            } else if (text.startsWith("<!--", i)) {
                i = text.indexOf("-->", i) + 3;
            } else if (text.startsWith("</", i)) {
                int end = text.indexOf('>', i);
                out.add(new Event(Kind.END, text.substring(i + 2, end).strip(), Map.of(), null));
                i = end + 1;
            } else {
                int end = text.indexOf('>', i);
                boolean empty = text.charAt(end - 1) == '/';
                String inside = text.substring(i + 1, empty ? end - 1 : end).strip();
                String[] parts = inside.split("\\s+", 2);
                Map<String, String> attrs = new LinkedHashMap<>();
                if (parts.length > 1) {
                    Matcher m = ATTR.matcher(parts[1]);
                    while (m.find()) {
                        attrs.put(m.group(1), m.group(3));
                    }
                }
                out.add(new Event(Kind.START, parts[0], attrs, null));
                if (empty) {
                    out.add(new Event(Kind.END, parts[0], Map.of(), null));
                }
                i = end + 1;
            }
        }
        return out;
    }

    // ---------- resolution ----------

    static List<Resolved> resolve(List<Event> events) {
        // La pile suit l'imbrication : push a CHAQUE ouverture (meme une Map vide), pop a chaque fermeture.
        Deque<Map<String, String>> scopes = new ArrayDeque<>();
        List<Resolved> out = new ArrayList<>();
        Deque<StringBuilder> texts = new ArrayDeque<>();
        Deque<Integer> indexes = new ArrayDeque<>();
        for (Event e : events) {
            switch (e.kind()) {
                case TEXT -> {
                    if (!texts.isEmpty()) {
                        texts.peek().append(e.text());
                    }
                }
                case END -> {
                    scopes.pop();
                    int at = indexes.pop();
                    Resolved r = out.get(at);
                    out.set(at, new Resolved(r.name(), r.attributes(), r.inScope(), texts.pop().toString().strip()));
                }
                case START -> {
                    scopes.push(declarations(e.attributes()));
                    String name = expand(e.qName(), scopes, true);
                    Map<String, String> attrs = new LinkedHashMap<>();
                    Set<String> seen = new HashSet<>();
                    for (Map.Entry<String, String> a : e.attributes().entrySet()) {
                        // Les declarations xmlns ne sont PAS des attributs de l'element.
                        if (a.getKey().equals("xmlns") || a.getKey().startsWith("xmlns:")) {
                            continue;
                        }
                        String exp = expand(a.getKey(), scopes, false);
                        // a:id et b:id avec a et b lies a la MEME URI : meme nom etendu = doublon.
                        if (!seen.add(exp)) {
                            throw new NsError("ATTRIBUT_DOUBLE " + exp);
                        }
                        String value = exp.equals("{" + XSI_NS + "}type") ? expand(a.getValue(), scopes, true) : a.getValue();
                        attrs.put(exp, value);
                    }
                    indexes.push(out.size());
                    texts.push(new StringBuilder());
                    out.add(new Resolved(name, attrs, inScope(scopes), ""));
                }
            }
        }
        return out;
    }

    static Map<String, String> declarations(Map<String, String> attributes) {
        Map<String, String> result = new LinkedHashMap<>();
        attributes.forEach((name, uri) -> {
            if (name.equals("xmlns")) {
                result.put("", uri); // xmlns="" est permis : il ANNULE le namespace par defaut
            } else if (name.startsWith("xmlns:")) {
                String prefix = name.substring(6);
                if (prefix.equals("xmlns") || prefix.equals("xml") && !uri.equals(XML_NS)) {
                    throw new NsError("PREFIXE_RESERVE " + prefix);
                }
                if (uri.isEmpty()) {
                    // En Namespaces 1.0, un prefixe ne peut pas etre "delie".
                    throw new NsError("LIAISON_VIDE " + prefix);
                }
                result.put(prefix, uri);
            }
        });
        return result;
    }

    static String lookup(String prefix, Deque<Map<String, String>> scopes) {
        // Du sommet (l'element courant) vers la racine : la liaison la plus PROCHE gagne.
        if (prefix.equals("xml")) {
            return XML_NS;
        }
        for (Map<String, String> scope : scopes) {
            if (scope.containsKey(prefix)) {
                return scope.get(prefix);
            }
        }
        return null;
    }

    static String expand(String qName, Deque<Map<String, String>> scopes, boolean useDefault) {
        int colon = qName.indexOf(':');
        if (colon != qName.lastIndexOf(':') || colon == 0 || colon == qName.length() - 1) {
            throw new NsError("QNAME_INVALIDE " + qName);
        }
        if (colon < 0) {
            // Le namespace par defaut vaut pour les ELEMENTS, jamais pour les attributs sans prefixe.
            String uri = useDefault ? lookup("", scopes) : null;
            return uri == null || uri.isEmpty() ? qName : "{" + uri + "}" + qName;
        }
        String prefix = qName.substring(0, colon);
        String uri = lookup(prefix, scopes);
        if (uri == null) {
            throw new NsError("PREFIXE_NON_DECLARE " + prefix);
        }
        return "{" + uri + "}" + qName.substring(colon + 1);
    }

    static Map<String, String> inScope(Deque<Map<String, String>> scopes) {
        // Tout ce qui est visible ici, trie par prefixe ; xml est toujours lie ; un defaut vide n'est pas une liaison.
        Map<String, String> visible = new TreeMap<>();
        visible.put("xml", XML_NS);
        List<Map<String, String>> outerFirst = new ArrayList<>(scopes);
        Collections.reverse(outerFirst);
        for (Map<String, String> scope : outerFirst) {
            visible.putAll(scope);
        }
        visible.values().removeIf(String::isEmpty);
        return visible;
    }

    // ---------- equivalence ----------

    static List<String> signature(String doc) {
        // Deux documents sont equivalents si leurs NOMS ETENDUS, attributs et textes le sont : les prefixes ne comptent pas.
        List<String> out = new ArrayList<>();
        for (Resolved r : resolve(events(doc))) {
            out.add(r.name() + " " + new TreeMap<>(r.attributes()) + " \"" + r.text() + "\"");
        }
        return out;
    }

    static String compare(List<String> a, List<String> b) {
        for (int k = 0; k < Math.max(a.size(), b.size()); k++) {
            String x = k < a.size() ? a.get(k) : "(rien)";
            String y = k < b.size() ? b.get(k) : "(rien)";
            if (!x.equals(y)) {
                return "non, element " + (k + 1) + " : " + x + " != " + y;
            }
        }
        return "oui (" + a.size() + " elements)";
    }
}
