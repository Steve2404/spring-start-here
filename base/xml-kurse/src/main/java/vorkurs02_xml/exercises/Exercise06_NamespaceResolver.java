package vorkurs02_xml.exercises;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EXERCICE 6 - Ecrire son propre resolveur de namespaces (niveau : challenge / entretien)
 * =====================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Un parseur namespace-aware transforme chaque nom ecrit (QName, ex.
 * "inv:line") en nom ETENDU {URI}local (ex. {urn:shop:invoice}line). Le
 * prefixe n'est qu'un surnom local ; seule l'URI compte (0.2.12 S4).
 *
 * Tu recois le document sous forme d'une liste d'evenements bruts
 * (deja ecrit : events(path)), lus SANS namespaces : START(qName,
 * attributs bruts, y compris les xmlns) et END. A toi de faire le
 * travail du parseur avec une PILE de portees (une Map prefixe -> URI
 * par element ouvert ; le prefixe "" represente le namespace par defaut).
 *
 * Arbitre : un DOM namespace-aware sur fixtures/ex06/invoice.xml doit
 * donner les memes noms etendus que toi, et il refuse (verifie) les 4
 * fichiers bad-*.xml pour les memes raisons que toi :
 *   bad-undeclared    : prefixe "a" jamais declare
 *   bad-duplicate     : a:id et b:id avec a et b lies a la MEME URI
 *   bad-unbind        : xmlns:a="" (interdit en Namespaces 1.0 ; seul
 *                       xmlns="" peut "desactiver" le defaut)
 *   bad-xmlns-prefix  : xmlns:xml="urn:pas-le-bon"
 *
 *
 * ==================================================================
 * TODO 1 : splitQName(qName)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "inv:line" c'est "nom de famille : prenom". Coupe au ':' : prefixe
 * "inv", local "line". Pas de ':' -> prefixe "". Plus d'un ':' ou un
 * ':' au bord -> IllegalArgumentException.
 *
 * -- Essayons a la main --
 *
 *   "inv:line" -> (inv, line) ; "note" -> ("", note) ; "a:b:c", ":a", "a:" -> exception
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 2 : declarationsOf(attributes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Parmi les attributs d'un element, certains sont des "annonces de
 * surnoms" : xmlns="URI" (surnom vide = defaut) et xmlns:p="URI".
 * Rends la Map surnom -> URI de CET element (les autres attributs sont
 * ignores). Regles (IllegalArgumentException sinon) :
 *   - xmlns:p="" est interdit (on ne peut pas "vider" un prefixe) ;
 *     xmlns="" est permis et vaut "" ;
 *   - le prefixe "xmlns" ne se declare jamais ;
 *   - "xml" ne peut etre lie qu'a XML_NS, et XML_NS qu'a "xml".
 *
 * -- Essayons a la main --
 *
 *   {xmlns:inv=urn:shop:tax, rate=20} -> {inv=urn:shop:tax}
 *   {xmlns=""}                        -> {""=""}
 *   {xmlns:a=""}                      -> exception
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 3 : lookup(prefix, scopes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu cherches qui porte ce surnom : d'abord dans TA piece (sommet de la
 * pile = element le plus interne), puis dans les pieces qui l'entourent.
 * Le premier trouve gagne (c'est ce qui permet de "redefinir" inv plus
 * bas). "xml" est toujours lie a XML_NS sans declaration. Pour "" (le
 * defaut) : trouve avec la valeur "" (xmlns="") ou pas trouve du tout
 * -> Optional.empty() (= pas de namespace).
 *
 * -- Le plan --
 *
 *   1. "xml" -> XML_NS.
 *   2. Parcourir scopes du sommet vers le fond ; 1re Map qui contient le
 *      prefixe : URI vide -> vide, sinon l'URI.
 *   3. Rien -> vide.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non. Remarque : for (Map m : deque) parcourt une ArrayDeque depuis le
 * sommet quand on empile avec push().
 *
 *
 * ==================================================================
 * TODO 4 : resolveElement(qName, scopes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Nom d'ELEMENT -> nom etendu "{uri}local", ou juste "local" s'il n'a
 * pas de namespace. Sans prefixe, le namespace par DEFAUT s'applique.
 * Prefixe inconnu -> IllegalArgumentException("Prefixe non declare : a").
 *
 * -- Essayons a la main (portees de <amount> dans invoice.xml) --
 *
 *   "amount"     -> defaut urn:shop:common -> "{urn:shop:common}amount"
 *   "inv:line"   -> "{urn:shop:invoice}line"
 *   "note" sous xmlns="" -> "note"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 5 fait presque pareil. Ecris expanded(q, scopes, useDefault)
 * que les deux appellent.
 *
 *
 * ==================================================================
 * TODO 5 : resolveAttribute(qName, scopes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Pour un ATTRIBUT sans prefixe, le namespace par defaut NE s'applique
 * PAS (0.2.13 S3, verifie : id reste ns=null sous xmlns="urn:shop:common").
 * Avec prefixe : comme un element.
 *
 * -- Essayons a la main --
 *
 *   "id" -> "id" ; "inv:vip" -> "{urn:shop:invoice}vip" ;
 *   "xml:lang" -> "{http://www.w3.org/XML/1998/namespace}lang"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : la meme que le TODO 4, avec useDefault = false.
 *
 *
 * ==================================================================
 * TODO 6 : resolveDocument(events)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu rejoues le parseur : a chaque START, tu empiles les declarations de
 * l'element (TODO 2, meme vides), tu resous son nom et ses attributs
 * (sauf les xmlns), et tu ecris la ligne
 *     nomEtendu + " " + [attributs etendus tries]
 * A chaque END, tu depiles. Deux attributs qui donnent le MEME nom
 * etendu -> IllegalArgumentException("Attribut en double : {urn:x}id").
 *
 * -- Essayons a la main --
 *
 *   <inv:invoice ... id="F-1" xml:lang="fr">
 *     -> "{urn:shop:invoice}invoice [id, {http://www.w3.org/XML/1998/namespace}lang]"
 *   <inv:tax xmlns:inv="urn:shop:tax" rate="20"/>   (inv redefini ici !)
 *     -> "{urn:shop:tax}tax [rate]"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 2, 4, 5.
 *
 *
 * ==================================================================
 * TODO 7 : xsiTypes(events)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * xsi:type="money:Euro" : le NOM de l'attribut se resout (c'est
 * {http://www.w3.org/2001/XMLSchema-instance}type), mais sa VALEUR est
 * AUSSI un QName qu'il faut resoudre avec les portees de l'element
 * (0.2.13 S6). Pour une valeur sans prefixe, le namespace par defaut
 * s'applique (comme un nom d'element). Rends une ligne
 * "elementEtendu : typeEtendu" par xsi:type trouve.
 *
 * -- Essayons a la main --
 *
 *   <amount xsi:type="money:Euro"> -> "{urn:shop:common}amount : {urn:shop:money}Euro"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : la meme gestion de pile que le TODO 6 + resolveElement pour la valeur.
 *
 *
 * Exemple a verifier :
 *
 *   splitQName, declarationsOf, lookup : voir les TODO
 *   resolveDocument(invoice.xml) == les 7 lignes attendues dans main()
 *   premiers mots de ces lignes == noms etendus du DOM namespace-aware
 *   bad-*.xml : IllegalArgumentException chez toi ET refus du DOM namespace-aware
 *   xsiTypes(invoice.xml) == [{urn:shop:common}amount : {urn:shop:money}Euro]
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - int colon = qName.indexOf(':'); qName.indexOf(':', colon + 1)
 *   - Deque<Map<String, String>> scopes = new ArrayDeque<>(); scopes.push(map); scopes.pop();
 *   - for (Map<String, String> scope : scopes) { if (scope.containsKey(p)) ... }
 *   - lookup(...).orElseThrow(() -> new IllegalArgumentException("Prefixe non declare : " + p))
 *   - Set<String> seen = new HashSet<>(); if (!seen.add(exp)) throw ...
 *   - list.sort(null) trie selon l'ordre naturel des String
 */
public class Exercise06_NamespaceResolver {

    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    public record QName(String prefix, String local) {
    }

    /** start == true : balise ouvrante (attributs bruts) ; false : fermante (attributs vides). */
    public record Event(boolean start, String qName, Map<String, String> attributes) {
    }

    public static QName splitQName(String qName) {
        throw new UnsupportedOperationException("TODO 1 : implementer splitQName()");
    }

    public static Map<String, String> declarationsOf(Map<String, String> attributes) {
        throw new UnsupportedOperationException("TODO 2 : implementer declarationsOf()");
    }

    public static Optional<String> lookup(String prefix, Deque<Map<String, String>> scopes) {
        throw new UnsupportedOperationException("TODO 3 : implementer lookup()");
    }

    public static String resolveElement(String qName, Deque<Map<String, String>> scopes) {
        throw new UnsupportedOperationException("TODO 4 : implementer resolveElement()");
    }

    public static String resolveAttribute(String qName, Deque<Map<String, String>> scopes) {
        throw new UnsupportedOperationException("TODO 5 : implementer resolveAttribute()");
    }

    public static List<String> resolveDocument(List<Event> events) {
        throw new UnsupportedOperationException("TODO 6 : implementer resolveDocument()");
    }

    public static List<String> xsiTypes(List<Event> events) {
        throw new UnsupportedOperationException("TODO 7 : implementer xsiTypes()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("splitQName(inv:line) == (inv, line)", splitQName("inv:line").equals(new QName("inv", "line")));
        ExerciseChecker.check("splitQName(note) == (\"\", note)", splitQName("note").equals(new QName("", "note")));
        for (String bad : List.of("a:b:c", ":a", "a:")) {
            ExerciseChecker.check("splitQName(" + bad + ") -> IllegalArgumentException", throwsIae(() -> splitQName(bad)));
        }

        ExerciseChecker.check("declarationsOf({xmlns:inv=urn:shop:tax, rate=20}) == {inv=urn:shop:tax}",
                declarationsOf(Map.of("xmlns:inv", "urn:shop:tax", "rate", "20")).equals(Map.of("inv", "urn:shop:tax")));
        ExerciseChecker.check("declarationsOf({xmlns=\"\"}) == {\"\"=\"\"}", declarationsOf(Map.of("xmlns", "")).equals(Map.of("", "")));
        ExerciseChecker.check("declarationsOf({xmlns:a=\"\"}) -> exception", throwsIae(() -> declarationsOf(Map.of("xmlns:a", ""))));
        ExerciseChecker.check("declarationsOf : xml mal lie, XML_NS sous un autre prefixe, xmlns:xmlns -> exception",
                throwsIae(() -> declarationsOf(Map.of("xmlns:xml", "urn:x")))
                        && throwsIae(() -> declarationsOf(Map.of("xmlns:x", XML_NS)))
                        && throwsIae(() -> declarationsOf(Map.of("xmlns:xmlns", "urn:x"))));
        ExerciseChecker.check("declarationsOf({xmlns:xml=XML_NS}) est permis",
                declarationsOf(Map.of("xmlns:xml", XML_NS)).equals(Map.of("xml", XML_NS)));

        Deque<Map<String, String>> scopes = new ArrayDeque<>();
        scopes.push(Map.of("inv", "urn:shop:invoice", "", "urn:shop:common"));
        scopes.push(Map.of("inv", "urn:shop:tax"));
        ExerciseChecker.check("lookup(inv) == urn:shop:tax (la portee interne gagne)", lookup("inv", scopes).equals(Optional.of("urn:shop:tax")));
        ExerciseChecker.check("lookup(\"\") == urn:shop:common (herite)", lookup("", scopes).equals(Optional.of("urn:shop:common")));
        ExerciseChecker.check("lookup(xml) == XML_NS sans declaration", lookup("xml", scopes).equals(Optional.of(XML_NS)));
        ExerciseChecker.check("lookup(zz) vide", lookup("zz", scopes).isEmpty());
        scopes.push(Map.of("", ""));
        ExerciseChecker.check("lookup(\"\") apres xmlns=\"\" -> vide", lookup("", scopes).isEmpty());
        scopes.pop();

        ExerciseChecker.check("resolveElement(amount) == {urn:shop:common}amount", resolveElement("amount", scopes).equals("{urn:shop:common}amount"));
        ExerciseChecker.check("resolveElement(inv:tax) == {urn:shop:tax}tax", resolveElement("inv:tax", scopes).equals("{urn:shop:tax}tax"));
        ExerciseChecker.check("resolveElement(zz:x) -> exception", throwsIae(() -> resolveElement("zz:x", scopes)));
        ExerciseChecker.check("resolveAttribute(id) == id (le defaut ne s'applique PAS)", resolveAttribute("id", scopes).equals("id"));
        ExerciseChecker.check("resolveAttribute(xml:lang)", resolveAttribute("xml:lang", scopes).equals("{" + XML_NS + "}lang"));

        List<Event> invoice = events(Fixtures.path("ex06/invoice.xml"));
        List<String> lines = resolveDocument(invoice);
        List<String> expected = List.of(
                "{urn:shop:invoice}invoice [id, {http://www.w3.org/XML/1998/namespace}lang]",
                "{urn:shop:common}customer [{urn:shop:invoice}vip]",
                "{urn:shop:invoice}line [nr]",
                "{urn:shop:common}amount [{http://www.w3.org/2001/XMLSchema-instance}type]",
                "note []",
                "{urn:shop:tax}tax [rate]",
                "{urn:shop:money}total [{urn:shop:money}currency]");
        for (int i = 0; i < expected.size(); i++) {
            ExerciseChecker.check("resolveDocument ligne " + (i + 1) + " : " + expected.get(i),
                    i < lines.size() && lines.get(i).equals(expected.get(i)));
        }
        ExerciseChecker.check("resolveDocument : exactement 7 lignes", lines.size() == 7);
        ExerciseChecker.check("noms etendus == ceux du DOM namespace-aware",
                lines.stream().map(l -> l.substring(0, l.indexOf(' '))).toList().equals(domExpandedNames(Fixtures.path("ex06/invoice.xml"))));

        for (String bad : List.of("bad-undeclared.xml", "bad-duplicate.xml", "bad-unbind.xml", "bad-xmlns-prefix.xml")) {
            Path p = Fixtures.path("ex06/" + bad);
            ExerciseChecker.check(bad + " : IllegalArgumentException chez toi ET refus du DOM namespace-aware",
                    throwsIae(() -> resolveDocument(eventsUnchecked(p))) && !jdkNamespaceWellFormed(p));
        }

        ExerciseChecker.check("xsiTypes(invoice.xml) == [{urn:shop:common}amount : {urn:shop:money}Euro]",
                xsiTypes(invoice).equals(List.of("{urn:shop:common}amount : {urn:shop:money}Euro")));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static boolean throwsIae(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static List<Event> eventsUnchecked(Path p) {
        try {
            return events(p);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Les evenements bruts du document, lus par un DOM SANS namespaces. */
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

    /** L'arbitre : noms etendus {uri}local de tous les elements, via un DOM namespace-aware. */
    private static List<String> domExpandedNames(Path xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        List<String> names = new ArrayList<>();
        walk(factory.newDocumentBuilder().parse(xml.toFile()).getDocumentElement(), names);
        return names;
    }

    private static void walk(Element e, List<String> names) {
        names.add(e.getNamespaceURI() == null ? e.getLocalName() : "{" + e.getNamespaceURI() + "}" + e.getLocalName());
        for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element child) {
                walk(child, names);
            }
        }
    }

    private static boolean jdkNamespaceWellFormed(Path xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            builder.parse(xml.toFile());
            return true;
        } catch (SAXParseException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
