package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * EXERCICE 13 - Migrer un export XML v1 -> v2 en modifiant le DOM, puis le serialiser (niveau : avance / capstone DOM)
 * ================================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Le CRM exporte fixtures/ex13/customers-v1.xml. Le nouveau systeme
 * attend le format v2 :
 *   - racine <clients version="2">, enfants <client> (renommes) ;
 *   - l'attribut vip devient le PREMIER element enfant <vip>..</vip> ;
 *   - textes nettoyes : blancs de bord retires, blancs internes fusionnes
 *     ("  Lea\n        Dubois " -> "Lea Dubois"), email en minuscules ;
 *   - elements devenus vides supprimes (<email></email>, <phone/>) ;
 *   - plus de commentaires ni de blancs d'indentation ;
 *   - clients tries par id.
 *
 * Resultat exact attendu (verifie), sans declaration XML, sans indentation :
 *
 *   <clients version="2"><client id="c1"><name>Adam Smith</name><email>adam@x.org</email></client>
 *   <client id="c2"><vip>no</vip><name>Lea Dubois</name></client><client id="c3"><vip>yes</vip>
 *   <name>Zoe Martin</name><email>zoe@mail.fr</email><phone>0601020304</phone></client></clients>
 *   (sur UNE seule ligne ; coupee ici pour la lecture)
 *
 * Faits verifies sur ce JDK :
 *   - doc.renameNode(element, null, "client") renomme SUR PLACE (rend le
 *     meme objet) ;
 *   - le fichier contient 18 noeuds "blancs + commentaires" a supprimer ;
 *   - appendChild(noeudDejaDansLArbre) le DEPLACE (pas de copie) ;
 *   - Transformer + INDENT=yes indente avec 4 espaces.
 *
 *
 * ==================================================================
 * TODO 1 : renameAll(doc, oldName, newName)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu changes l'etiquette de tous les tiroirs "customer" en "client".
 * getElementsByTagName rend une liste VIVANTE : prudence, recopie-la
 * dans une ArrayList AVANT de modifier (l'exercice 12 a montre ce qui
 * arrive sinon avec removeChild). Rends le nombre de renommages.
 *
 *
 * ==================================================================
 * TODO 2 : moveAttributeToFirstChild(element, attribute)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * L'etiquette collee sur la boite (attribut vip="yes") devient un petit
 * papier range EN PREMIER dans la boite (<vip>yes</vip>). Cree
 * l'element avec getOwnerDocument().createElement, insere-le avant le
 * premier enfant, supprime l'attribut. Pas d'attribut -> false, rien ne
 * change ; sinon true.
 *
 *
 * ==================================================================
 * TODO 3 : removeWhitespaceAndComments(node)
 * ==================================================================
 *
 * Supprime, dans tout le sous-arbre, les noeuds texte blancs et les
 * commentaires. Rends le nombre supprime (18 pour le fichier complet).
 * Retiens getNextSibling() AVANT removeChild.
 *
 *
 * ==================================================================
 * TODO 4 : normalizeText(leaf)
 * ==================================================================
 *
 * Remplace le texte de la feuille par sa version nettoyee (strip + blancs
 * internes -> un espace). setTextContent("") laisse l'element SANS enfant.
 *
 *
 * ==================================================================
 * TODO 5 : removeEmptyElements(parent)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Range la maison du bas vers le haut : d'abord les enfants (recursion),
 * puis on regarde chaque enfant : aucun enfant ET aucun attribut ->
 * supprime. Rends le nombre supprime.
 *
 *
 * ==================================================================
 * TODO 6 : sortChildElementsBy(parent, attribute)
 * ==================================================================
 *
 * Collecte les enfants elements, trie-les par la valeur de l'attribut,
 * puis re-ajoute-les un par un avec appendChild (qui DEPLACE).
 *
 *
 * ==================================================================
 * TODO 7 : serialize(doc, indent)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tant que tu n'as pas ecrit, tes modifications n'existent que dans la
 * memoire (0.2.17 S7). Un Transformer "identite" (sans feuille XSLT)
 * recopie le DOM dans un StringWriter. Sans declaration XML ; INDENT
 * selon le parametre.
 *
 *
 * ==================================================================
 * TODO 8 : migrate(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le chef d'orchestre. L'ORDRE compte : " ZOE@Mail.FR " doit etre
 * nettoye AVANT le test "vide", sinon <email></email> (qui contenait
 * juste rien) et un <email> "  " ne seraient pas traites pareil.
 *
 * -- Le plan --
 *
 *   1. Blancs et commentaires (TODO 3).
 *   2. Renommer racine et clients (TODO 1), ajouter version="2".
 *   3. Pour chaque client : vip (TODO 2), puis chaque champ : TODO 4,
 *      et l'email en minuscules (Locale.ROOT).
 *   4. Supprimer les vides (TODO 5), trier par id (TODO 6).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * C'est l'assemblage des 7 boites precedentes.
 *
 *
 * Exemple a verifier : voir main() (chaque TODO teste seul, puis la migration complete au caractere pres).
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - doc.renameNode(node, null, newName)
 *   - element.getOwnerDocument().createElement(n) ; child.setTextContent(v)
 *   - element.insertBefore(child, element.getFirstChild()) ; element.removeAttribute(a)
 *   - e.hasChildNodes(), e.hasAttributes()
 *   - children.sort(Comparator.comparing(e -> e.getAttribute("id")))
 *   - Transformer t = TransformerFactory.newInstance().newTransformer();
 *     t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); t.setOutputProperty(OutputKeys.INDENT, "yes");
 *     t.transform(new DOMSource(doc), new StreamResult(stringWriter));
 */
public class Exercise13_DomMigration {

    public static int renameAll(Document doc, String oldName, String newName) {
        throw new UnsupportedOperationException("TODO 1 : implementer renameAll()");
    }

    public static boolean moveAttributeToFirstChild(Element element, String attribute) {
        throw new UnsupportedOperationException("TODO 2 : implementer moveAttributeToFirstChild()");
    }

    public static int removeWhitespaceAndComments(Node node) {
        throw new UnsupportedOperationException("TODO 3 : implementer removeWhitespaceAndComments()");
    }

    public static void normalizeText(Element leaf) {
        throw new UnsupportedOperationException("TODO 4 : implementer normalizeText()");
    }

    public static int removeEmptyElements(Element parent) {
        throw new UnsupportedOperationException("TODO 5 : implementer removeEmptyElements()");
    }

    public static void sortChildElementsBy(Element parent, String attribute) {
        throw new UnsupportedOperationException("TODO 6 : implementer sortChildElementsBy()");
    }

    public static String serialize(Document doc, boolean indent) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer serialize()");
    }

    public static Document migrate(Document doc) {
        throw new UnsupportedOperationException("TODO 8 : implementer migrate()");
    }

    public static void main(String[] args) throws Exception {
        Document d1 = load();
        Element firstCustomer = (Element) d1.getElementsByTagName("customer").item(0);
        ExerciseChecker.check("renameAll(customer -> client) == 3", renameAll(d1, "customer", "client") == 3);
        ExerciseChecker.check("renameAll : plus aucun customer, 3 client, meme objet Java renomme",
                d1.getElementsByTagName("customer").getLength() == 0 && d1.getElementsByTagName("client").getLength() == 3
                        && firstCustomer.getTagName().equals("client"));

        Element c3 = (Element) load().getElementsByTagName("customer").item(0);
        ExerciseChecker.check("moveAttributeToFirstChild(c3, vip) == true", moveAttributeToFirstChild(c3, "vip"));
        ExerciseChecker.check("... <vip>yes</vip> est le 1er enfant, l'attribut a disparu",
                c3.getFirstChild().getNodeName().equals("vip") && c3.getFirstChild().getTextContent().equals("yes") && !c3.hasAttribute("vip"));
        ExerciseChecker.check("moveAttributeToFirstChild une 2e fois == false", !moveAttributeToFirstChild(c3, "vip"));
        Element lonely = parseString("<c vip='no'/>").getDocumentElement();
        ExerciseChecker.check("moveAttributeToFirstChild sur un element SANS enfant",
                moveAttributeToFirstChild(lonely, "vip") && lonely.getFirstChild().getNodeName().equals("vip"));

        Document d3 = load();
        ExerciseChecker.check("removeWhitespaceAndComments(document) == 18", removeWhitespaceAndComments(d3) == 18);
        ExerciseChecker.check("... la racine n'a plus que ses 3 elements", d3.getDocumentElement().getChildNodes().getLength() == 3);

        Element name = parseString("<name>  Lea\n        Dubois </name>").getDocumentElement();
        normalizeText(name);
        ExerciseChecker.check("normalizeText -> 'Lea Dubois'", name.getTextContent().equals("Lea Dubois"));
        Element blank = parseString("<email>   </email>").getDocumentElement();
        normalizeText(blank);
        ExerciseChecker.check("normalizeText('   ') -> element sans enfant", !blank.hasChildNodes());

        Element tree = parseString("<r><a/><b x='1'/><c><d></d></c><e>t</e></r>").getDocumentElement();
        ExerciseChecker.check("removeEmptyElements : a, d puis c (devenu vide) -> 3",
                removeEmptyElements(tree) == 3 && tree.getChildNodes().getLength() == 2);

        Element list = parseString("<l><i id='c3'/><i id='c1'/><i id='c2'/></l>").getDocumentElement();
        Node formerFirst = list.getFirstChild();
        sortChildElementsBy(list, "id");
        ExerciseChecker.check("sortChildElementsBy(id) -> c1, c2, c3 (memes objets deplaces)",
                ((Element) list.getFirstChild()).getAttribute("id").equals("c1") && list.getLastChild() == formerFirst
                        && list.getChildNodes().getLength() == 3);

        ExerciseChecker.check("serialize(<r a='1'><x/></r>, false) == <r a=\"1\"><x/></r>",
                serialize(parseString("<r a='1'><x/></r>"), false).equals("<r a=\"1\"><x/></r>"));

        String expected = "<clients version=\"2\"><client id=\"c1\"><name>Adam Smith</name><email>adam@x.org</email></client>"
                + "<client id=\"c2\"><vip>no</vip><name>Lea Dubois</name></client>"
                + "<client id=\"c3\"><vip>yes</vip><name>Zoe Martin</name><email>zoe@mail.fr</email><phone>0601020304</phone></client>"
                + "</clients>";
        Document migrated = migrate(load());
        String compact = serialize(migrated, false);
        ExerciseChecker.check("migrate + serialize == le format v2 attendu, au caractere pres", compact.equals(expected));
        if (!compact.equals(expected)) {
            System.out.println("Obtenu : " + compact);
        }
        String pretty = serialize(migrated, true);
        ExerciseChecker.check("serialize(indent) : indente avec 4 espaces", pretty.contains("\n    <client id=\"c1\">"));
        ExerciseChecker.check("serialize(indent) relu == meme nombre d'elements (12)",
                parseString(pretty).getElementsByTagName("*").getLength() == 12);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Document load() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Fixtures.path("ex13/customers-v1.xml").toFile());
    }

    private static Document parseString(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
