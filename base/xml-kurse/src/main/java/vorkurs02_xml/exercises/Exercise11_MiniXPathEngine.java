package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * EXERCICE 11 - Ecrire son propre moteur XPath (sous-ensemble) et le battre contre celui du JDK (niveau : challenge / entretien)
 * ==========================================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Pour vraiment comprendre XPath (0.2.16), rien ne vaut d'en ecrire un.
 * Ton moteur gere des chemins ABSOLUS faits de steps separes par '/' ou
 * '//' :
 *   step      : nom | * | .. | @nom | text()   suivi de 0..n predicats
 *   predicat  : [3]  [last()]  [@a]  [@a='v']  [enfant]  [enfant='v']
 *
 * main() evalue 22 expressions avec TON moteur et avec javax.xml.xpath,
 * sur fixtures/ex11/library.xml, et exige les MEMES noeuds (memes objets,
 * meme ordre). Les 3 regles qui font la difference :
 *
 *   R1. "//" veut dire "/descendant-or-self::node()/" : le step suivant
 *       s'applique a CHAQUE noeud (document compris) et a tous ses
 *       descendants, pris comme contextes.
 *   R2. Un predicat de position compte dans la liste des candidats d'UN
 *       contexte. //book[1] = "le 1er livre de chaque parent" (3 livres
 *       ici : shelf A, box, shelf B), pas "le 1er livre du document".
 *   R3. Le resultat d'un step est un ENSEMBLE (sans doublon) dans l'ORDRE
 *       DU DOCUMENT, meme si on l'a construit dans un autre ordre
 *       (//book/.. passe 2 fois par shelf A).
 *
 *
 * ==================================================================
 * TODO 1 : parse(path)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu decoupes l'itineraire en etapes. "/library//book[2]/title" :
 *   library (enfant), book (DESCENDANT, [2]), title (enfant).
 * Un morceau vide entre deux '/' signale "//" : l'etape suivante est
 * descendant = true. Un chemin qui ne commence pas par '/' ->
 * IllegalArgumentException.
 *
 * -- Le plan --
 *
 *   1. Avancer de '/' en '/' (en ignorant les '/' entre crochets).
 *   2. Segment vide -> retenir "descendant" ; sinon fabriquer le Step :
 *      test = avant le premier '[', puis chaque [..] -> parsePredicate.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "segment -> Step" (parseStep) et TODO 2 pour chaque predicat.
 *
 *
 * ==================================================================
 * TODO 2 : parsePredicate(inside)
 * ==================================================================
 *
 * inside = le texte entre crochets :
 *   "2" -> POSITION 2 ; "last()" -> LAST ; "@lang" -> HAS_ATTRIBUTE lang ;
 *   "@lang='fr'" -> ATTRIBUTE_EQUALS lang fr ; "author" -> HAS_CHILD author ;
 *   "author='Camus'" -> CHILD_EQUALS author Camus
 *
 *
 * ==================================================================
 * TODO 3 : candidates(context, test)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Depuis une piece (context), qui peux-tu atteindre en un pas ?
 *   ".."     -> le parent (s'il existe) ;
 *   "@nom"   -> le noeud Attr, si l'element l'a ;
 *   "text()" -> les noeuds texte enfants ;
 *   "*"      -> tous les elements enfants ; "nom" -> ceux de ce nom.
 * Toujours dans l'ordre des enfants.
 *
 *
 * ==================================================================
 * TODO 4 : applyPredicate(candidates, predicate)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu tries les candidats d'UN contexte (regle R2). Position 1 = le
 * premier de CETTE liste ; LAST = le dernier. [enfant='v'] est vrai si
 * AU MOINS UN enfant de ce nom a exactement ce texte (Kafka OU Brod).
 * Enchainer [@lang='fr'][2] : le 2e PARMI les francais (souvent aucun) ;
 * [2][@lang='de'] : le 2e, s'il est allemand.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : HAS_CHILD et CHILD_EQUALS reutilisent candidates (TODO 3).
 *
 *
 * ==================================================================
 * TODO 5 : descendantsOrSelf(contexts)
 * ==================================================================
 *
 * Chaque contexte + tous ses elements descendants, en ordre du document,
 * sans doublon (regle R1). Pour le Document : lui-meme + tous les elements.
 *
 *
 * ==================================================================
 * TODO 6 : sortDocumentOrder(nodes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Retire les doublons et range dans l'ordre du document (regle R3).
 * DOM sait comparer deux noeuds : a.compareDocumentPosition(b) rend un
 * masque de bits ; si DOCUMENT_POSITION_FOLLOWING est allume, b vient
 * APRES a.
 *
 *
 * ==================================================================
 * TODO 7 : evaluate(doc, path)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu pars du document. Pour chaque step : si descendant, elargis les
 * contextes (TODO 5) ; pour CHAQUE contexte, calcule ses candidats
 * (TODO 3) et passe-les dans chaque predicat (TODO 4) ; reunis tout,
 * trie (TODO 6) : ce sont les contextes du step suivant.
 *
 * -- Essayons a la main --
 *
 *   //book[1]/title
 *     "//" : contextes = document, library, shelf A, book.., box, shelf B...
 *     book[1] par contexte : shelf A -> Etranger ; box -> Peste ; shelf B -> 1984
 *     /title -> [L'Etranger, La Peste, 1984]
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 1 a 6 (c'est le chef d'orchestre).
 *
 *
 * Exemple a verifier :
 *
 *   parse et parsePredicate : cas du TODO 1 et 2
 *   //book[1]/title == [L'Etranger, La Peste, 1984] ; //book[@lang='fr'][2] == [] ; //book[2][@lang='de'] == [Prozess]
 *   les 22 expressions de main() : memes noeuds que javax.xml.xpath
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - Pattern.compile("\\[([^\\]]*)]") pour lister les predicats d'un segment
 *   - Pattern.compile("(@?)([\\w.-]+)\\s*=\\s*'([^']*)'") pour les formes avec '='
 *   - element.getAttributeNode(nom) rend l'Attr (un Node)
 *   - switch (p.kind()) { case POSITION -> ...; case LAST -> ...; ... } en expression booleenne
 *   - new ArrayList<>(new LinkedHashSet<>(nodes)) retire les doublons
 *   - (a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING) != 0
 */
public class Exercise11_MiniXPathEngine {

    public enum PredicateKind { POSITION, LAST, HAS_ATTRIBUTE, ATTRIBUTE_EQUALS, HAS_CHILD, CHILD_EQUALS }

    /** name/value servent aux predicats d'attribut ou d'enfant ; position a POSITION. */
    public record Predicate(PredicateKind kind, String name, String value, int position) {
    }

    /** descendant == true si le step etait precede de "//". */
    public record Step(boolean descendant, String test, List<Predicate> predicates) {
    }

    public static List<Step> parse(String path) {
        throw new UnsupportedOperationException("TODO 1 : implementer parse()");
    }

    public static Predicate parsePredicate(String inside) {
        throw new UnsupportedOperationException("TODO 2 : implementer parsePredicate()");
    }

    public static List<Node> candidates(Node context, String test) {
        throw new UnsupportedOperationException("TODO 3 : implementer candidates()");
    }

    public static List<Node> applyPredicate(List<Node> candidates, Predicate p) {
        throw new UnsupportedOperationException("TODO 4 : implementer applyPredicate()");
    }

    public static List<Node> descendantsOrSelf(List<Node> contexts) {
        throw new UnsupportedOperationException("TODO 5 : implementer descendantsOrSelf()");
    }

    public static List<Node> sortDocumentOrder(Collection<Node> nodes) {
        throw new UnsupportedOperationException("TODO 6 : implementer sortDocumentOrder()");
    }

    public static List<Node> evaluate(Document doc, String path) {
        throw new UnsupportedOperationException("TODO 7 : implementer evaluate()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("parse(/library//book[2]/title)", parse("/library//book[2]/title").equals(List.of(
                new Step(false, "library", List.of()),
                new Step(true, "book", List.of(new Predicate(PredicateKind.POSITION, null, null, 2))),
                new Step(false, "title", List.of()))));
        ExerciseChecker.check("parse(//book/@year) : 2 steps, le 1er descendant",
                parse("//book/@year").equals(List.of(new Step(true, "book", List.of()), new Step(false, "@year", List.of()))));
        ExerciseChecker.check("parse(book) -> IllegalArgumentException", throwsIae(() -> parse("book")));

        ExerciseChecker.check("parsePredicate : 2, last()",
                parsePredicate("2").equals(new Predicate(PredicateKind.POSITION, null, null, 2))
                        && parsePredicate("last()").kind() == PredicateKind.LAST);
        ExerciseChecker.check("parsePredicate : @lang, @lang='fr'",
                parsePredicate("@lang").equals(new Predicate(PredicateKind.HAS_ATTRIBUTE, "lang", null, 0))
                        && parsePredicate("@lang='fr'").equals(new Predicate(PredicateKind.ATTRIBUTE_EQUALS, "lang", "fr", 0)));
        ExerciseChecker.check("parsePredicate : author, author='Camus'",
                parsePredicate("author").equals(new Predicate(PredicateKind.HAS_CHILD, "author", null, 0))
                        && parsePredicate("author='Camus'").equals(new Predicate(PredicateKind.CHILD_EQUALS, "author", "Camus", 0)));

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Fixtures.path("ex11/library.xml").toFile());
        Element shelfA = (Element) doc.getElementsByTagName("shelf").item(0);
        ExerciseChecker.check("candidates(shelf A, book) : 2 livres directs (pas celui de box)", candidates(shelfA, "book").size() == 2);
        ExerciseChecker.check("candidates(shelf A, *) : book, book, box", candidates(shelfA, "*").stream().map(Node::getNodeName).toList()
                .equals(List.of("book", "book", "box")));
        ExerciseChecker.check("candidates(shelf A, @id) : l'attribut id=A",
                candidates(shelfA, "@id").size() == 1 && candidates(shelfA, "@id").get(0).getNodeValue().equals("A"));
        ExerciseChecker.check("candidates(shelf A, ..) : library", candidates(shelfA, "..").get(0).getNodeName().equals("library"));

        List<Node> books = candidates(shelfA, "book");
        ExerciseChecker.check("applyPredicate(last()) : Der Prozess",
                titles(applyPredicate(books, new Predicate(PredicateKind.LAST, null, null, 0))).equals(List.of("Der Prozess")));
        ExerciseChecker.check("applyPredicate(author='Brod') : Der Prozess (un des auteurs suffit)",
                titles(applyPredicate(books, new Predicate(PredicateKind.CHILD_EQUALS, "author", "Brod", 0))).equals(List.of("Der Prozess")));

        ExerciseChecker.check("descendantsOrSelf([document]) : document + 19 elements",
                descendantsOrSelf(List.of(doc)).size() == 20 &&descendantsOrSelf(List.of(doc)).get(0) == doc);
        List<Node> shuffled = new ArrayList<>(List.of(shelfA, doc.getDocumentElement(), shelfA));
        ExerciseChecker.check("sortDocumentOrder([shelfA, library, shelfA]) == [library, shelfA]",
                sortDocumentOrder(shuffled).equals(List.of(doc.getDocumentElement(), shelfA)));

        ExerciseChecker.check("//book[1]/title == [L'Etranger, La Peste, 1984] (regle R2)",
                texts(evaluate(doc, "//book[1]/title")).equals(List.of("L'Etranger", "La Peste", "1984")));
        ExerciseChecker.check("//book[@lang='fr'][2] == []", evaluate(doc, "//book[@lang='fr'][2]").isEmpty());
        ExerciseChecker.check("//book[2][@lang='de']/title == [Der Prozess]",
                texts(evaluate(doc, "//book[2][@lang='de']/title")).equals(List.of("Der Prozess")));

        List<String> expressions = List.of(
                "/library/shelf", "/library/shelf[2]/book/title", "//book", "//book[1]", "//book[last()]",
                "/library/shelf/book[@lang='fr']/title", "//book[author='Camus']/title", "//book[author]",
                "//*[@lang='de']", "//title/text()", "//book/@year", "/library/shelf[@id='B']/*", "//shelf//title",
                "//box/book[1]/author", "/library/shelf/book[2]/author[2]", "//book[@lang='fr'][2]/title",
                "//book[2][@lang='de']", "/library//magazine/title", "//nothing", "/library/shelf[3]",
                "//book/..", "//author/../title");
        for (String expr : expressions) {
            List<Node> mine = evaluate(doc, expr);
            List<Node> jdk = jdk(doc, expr);
            ExerciseChecker.check(expr + " -> " + jdk.size() + " noeud(s), identiques au JDK", mine.equals(jdk));
        }

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

    private static List<String> texts(List<Node> nodes) {
        return nodes.stream().map(Node::getTextContent).toList();
    }

    private static List<String> titles(List<Node> books) {
        return books.stream().map(b -> ((Element) b).getElementsByTagName("title").item(0).getTextContent()).toList();
    }

    /** L'arbitre : le moteur XPath 1.0 du JDK. */
    private static List<Node> jdk(Document doc, String expr) throws Exception {
        NodeList list = (NodeList) XPathFactory.newInstance().newXPath().evaluate(expr, doc, XPathConstants.NODESET);
        List<Node> out = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            out.add(list.item(i));
        }
        return out;
    }
}
