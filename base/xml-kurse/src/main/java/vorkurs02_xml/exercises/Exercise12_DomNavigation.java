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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EXERCICE 12 - DOM sans se faire pieger : noeuds blancs, textContent, NodeList vivante (niveau : avance)
 * ======================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex12/playlist.xml est indente "joliment". Pour DOM, chaque
 * retour a la ligne + indentation entre deux balises est un VRAI noeud
 * texte (0.2.17 S4). Resultats reels sur ce fichier :
 *   - 21 elements mais 24 noeuds texte, 2 commentaires, 1 CDATA, 1 PI ;
 *   - la racine <playlist> a 9 enfants (getChildNodes) pour 3 <track> ;
 *   - le 1er enfant de <track id="t1"> est le texte "\n    ", pas <title> ;
 *   - <title>Blue <em>in</em> Green</title> : getTextContent() rend
 *     "Blue in Green", mais ses textes DIRECTS ne font que "Blue  Green" ;
 *   - supprimer des elements en parcourant getChildNodes() par index, ou
 *     getElementsByTagName(...), en SAUTE : ces listes sont VIVANTES
 *     (verifie : 2 <ad/> supprimes sur 3, puis 1 sur 2).
 *
 * Tu vas ecrire la petite boite a outils qu'on reutilise ensuite dans
 * toute application DOM, puis lire la playlist en objets Java.
 *
 *
 * ==================================================================
 * TODO 1 : countNodeTypes(root)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Fais l'inventaire de TOUT ce qu'il y a dans l'arbre sous root (root
 * non compte) : ELEMENT, TEXT, CDATA, COMMENT, PI (autre : "OTHER").
 * Les attributs ne sont PAS des enfants (0.2.10 S6) : ils n'y sont pas.
 *
 * -- Essayons a la main --
 *
 *   countNodeTypes(document) == {CDATA=1, COMMENT=2, ELEMENT=21, PI=1, TEXT=24}
 *   (TreeMap : cles triees)
 *
 * -- Le plan --
 *
 *   1. Pour chaque enfant (getFirstChild / getNextSibling) : compter son
 *      type, puis recommencer sur lui (recursion).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "type numerique -> nom" (un switch) et la recursion elle-meme.
 *
 *
 * ==================================================================
 * TODO 2 : childElements(parent)
 * ==================================================================
 *
 * Les enfants DIRECTS de type element, dans l'ordre. <playlist> -> 3.
 *
 *
 * ==================================================================
 * TODO 3 : firstChildElement(parent, name)
 * ==================================================================
 *
 * Le premier enfant ELEMENT portant ce nom, ou Optional.empty().
 * (getFirstChild() rendrait l'indentation ; getElementsByTagName
 * descendrait dans les petits-enfants.)
 *
 *
 * ==================================================================
 * TODO 4 : ownText(element)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * getTextContent() vide TOUS les tiroirs du meuble, y compris ceux des
 * sous-meubles. ownText ne vide que les tiroirs de CE meuble : les
 * noeuds TEXT et CDATA enfants directs, recolles.
 *
 *   <title>Blue <em>in</em> Green</title>          -> "Blue  Green"
 *   <title><![CDATA[Rock & Roll <live>]]></title>  -> "Rock & Roll <live>"
 *
 *
 * ==================================================================
 * TODO 5 : pathOf(node)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Donne l'adresse postale exacte d'un noeud sous forme d'un chemin
 * XPath : "/playlist[1]/track[3]/tags[1]/tag[1]". La position compte
 * seulement les freres ELEMENTS de meme nom qui precedent (+1). Pour un
 * attribut : chemin de son element + "/@nom" (un Attr n'a PAS de parent :
 * getParentNode() rend null, il faut getOwnerElement()). main() verifie
 * qu'evaluer ce chemin avec javax.xml.xpath rend le MEME noeud.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : la methode s'appelle elle-meme sur le parent (le Document rend "").
 *
 *
 * ==================================================================
 * TODO 6 : removeChildrenNamed(parent, name)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Des enfants font la queue ; tu fais sortir tous les "ad". Si tu comptes
 * les places (i = 0, 1, 2...) pendant que la queue avance, tu en rates :
 * quand l'enfant n.1 sort, l'enfant n.2 prend sa place et tu passes
 * directement a la place 2. Solution : retenir le SUIVANT avant de faire
 * sortir le courant. Rends le nombre d'elements supprimes.
 *
 * -- Essayons a la main --
 *
 *   track t2 : ... <ad/><ad/><ad/> ... -> 3 supprimes, 0 restant
 *
 *
 * ==================================================================
 * TODO 7 : readTracks(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Transforme chaque <track> en Track(id, titre COMPLET, duree en
 * secondes, tags). Le titre est le texte de tout <title> (donc
 * "Blue in Green" : ici on VEUT getTextContent). Une duree "5:37" vaut
 * 337 s. <tags/> vide -> liste vide.
 *
 *   -> [Track(t1, Blue in Green, 337, [jazz, modal]),
 *       Track(t2, Rock & Roll <live>, 225, []),
 *       Track(t3, So What, 562, [jazz])]
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 2 et 3, plus parseDuration("m:ss") (petite recette seule).
 *
 *
 * Exemple a verifier : les valeurs ci-dessus, et les 2 pieges du contexte (deja ecrits dans main).
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling())
 *   - map.merge(nom, 1, Integer::sum) ; switch (c.getNodeType()) { case Node.TEXT_NODE -> ... }
 *   - if (c instanceof Element e) ...
 *   - c.getNodeType() == Node.CDATA_SECTION_NODE ; c.getNodeValue()
 *   - ((Attr) node).getOwnerElement() ; node.getPreviousSibling()
 *   - Node next = c.getNextSibling(); parent.removeChild(c); c = next;
 *   - "5:37".strip().split(":")
 */
public class Exercise12_DomNavigation {

    public record Track(String id, String title, int seconds, List<String> tags) {
    }

    public static Map<String, Integer> countNodeTypes(Node root) {
        throw new UnsupportedOperationException("TODO 1 : implementer countNodeTypes()");
    }

    public static List<Element> childElements(Element parent) {
        throw new UnsupportedOperationException("TODO 2 : implementer childElements()");
    }

    public static Optional<Element> firstChildElement(Element parent, String name) {
        throw new UnsupportedOperationException("TODO 3 : implementer firstChildElement()");
    }

    public static String ownText(Element element) {
        throw new UnsupportedOperationException("TODO 4 : implementer ownText()");
    }

    public static String pathOf(Node node) {
        throw new UnsupportedOperationException("TODO 5 : implementer pathOf()");
    }

    public static int removeChildrenNamed(Element parent, String name) {
        throw new UnsupportedOperationException("TODO 6 : implementer removeChildrenNamed()");
    }

    public static List<Track> readTracks(Document doc) {
        throw new UnsupportedOperationException("TODO 7 : implementer readTracks()");
    }

    public static void main(String[] args) throws Exception {
        Document doc = parse();
        ExerciseChecker.check("countNodeTypes(document) == {CDATA=1, COMMENT=2, ELEMENT=21, PI=1, TEXT=24}",
                countNodeTypes(doc).equals(Map.of("CDATA", 1, "COMMENT", 2, "ELEMENT", 21, "PI", 1, "TEXT", 24)));

        Element root = doc.getDocumentElement();
        ExerciseChecker.check("le piege : racine -> 9 childNodes", root.getChildNodes().getLength() == 9);
        ExerciseChecker.check("childElements(racine) : 3 track",
                childElements(root).size() == 3 && childElements(root).stream().allMatch(e -> e.getTagName().equals("track")));

        Element t1 = childElements(root).get(0);
        ExerciseChecker.check("le piege : t1.getFirstChild() est le texte d'indentation",
                t1.getFirstChild().getNodeType() == Node.TEXT_NODE && t1.getFirstChild().getNodeValue().isBlank());
        ExerciseChecker.check("firstChildElement(t1, duration) == <duration>5:37",
                firstChildElement(t1, "duration").map(Element::getTextContent).equals(Optional.of("5:37")));
        ExerciseChecker.check("firstChildElement(t1, tag) vide (tag est un PETIT-enfant)", firstChildElement(t1, "tag").isEmpty());

        Element title1 = firstChildElement(t1, "title").orElseThrow();
        ExerciseChecker.check("ownText(Blue <em>in</em> Green) == 'Blue  Green'", ownText(title1).equals("Blue  Green"));
        ExerciseChecker.check("getTextContent du meme titre == 'Blue in Green'", title1.getTextContent().equals("Blue in Green"));
        Element t2 = childElements(root).get(1);
        ExerciseChecker.check("ownText(CDATA) == 'Rock & Roll <live>'",
                ownText(firstChildElement(t2, "title").orElseThrow()).equals("Rock & Roll <live>"));

        Element tag3 = (Element) doc.getElementsByTagName("tag").item(2);
        ExerciseChecker.check("pathOf(3e tag) == /playlist[1]/track[3]/tags[1]/tag[1]", pathOf(tag3).equals("/playlist[1]/track[3]/tags[1]/tag[1]"));
        ExerciseChecker.check("pathOf(@id de t1) == /playlist[1]/track[1]/@id", pathOf(t1.getAttributeNode("id")).equals("/playlist[1]/track[1]/@id"));
        NodeList all = doc.getElementsByTagName("*");
        boolean allRoundTrip = true;
        for (int i = 0; i < all.getLength(); i++) {
            Node n = all.item(i);
            allRoundTrip &= XPathFactory.newInstance().newXPath().evaluate(pathOf(n), doc, XPathConstants.NODE) == n;
        }
        ExerciseChecker.check("pour les 21 elements : XPath(pathOf(n)) == n", allRoundTrip);

        ExerciseChecker.check("readTracks == les 3 Track attendues", readTracks(doc).equals(List.of(
                new Track("t1", "Blue in Green", 337, List.of("jazz", "modal")),
                new Track("t2", "Rock & Roll <live>", 225, List.of()),
                new Track("t3", "So What", 562, List.of("jazz")))));

        ExerciseChecker.check("le piege : suppression naive par index -> 2 <ad/> sur 3", naiveRemoveByIndex(parse()) == 2);
        ExerciseChecker.check("removeChildrenNamed(t2, ad) == 3, il n'en reste aucun dans t2",
                removeChildrenNamed(t2, "ad") == 3 && t2.getElementsByTagName("ad").getLength() == 0);
        ExerciseChecker.check("removeChildrenNamed(t2, ad) une 2e fois == 0", removeChildrenNamed(t2, "ad") == 0);
        ExerciseChecker.check("le <ad/> de t3 est intact", doc.getElementsByTagName("ad").getLength() == 1);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Document parse() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Fixtures.path("ex12/playlist.xml").toFile());
    }

    /** Le MAUVAIS code : parcourir une NodeList vivante par index en supprimant. */
    private static int naiveRemoveByIndex(Document doc) {
        Element t2 = (Element) doc.getElementsByTagName("track").item(1);
        NodeList kids = t2.getChildNodes();
        int removed = 0;
        for (int i = 0; i < kids.getLength(); i++) {
            if (kids.item(i).getNodeName().equals("ad")) {
                t2.removeChild(kids.item(i));
                removed++;
            }
        }
        return removed;
    }
}
