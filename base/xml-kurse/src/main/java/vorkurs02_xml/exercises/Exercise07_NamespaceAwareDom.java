package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * EXERCICE 7 - Meme commande, prefixes differents : comparer par noms etendus (niveau : avance / capstone namespaces)
 * ===============================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Deux systemes envoient LA MEME commande (fixtures/ex07) :
 *   order-a.xml : tout est prefixe (inv:, c:, xsi:, m:) ;
 *   order-b.xml : namespace par defaut pour la facture, k: pour le client,
 *                 s: au lieu de xsi:, euro: au lieu de m:, texte "  Lea  "
 *                 entoure d'espaces, un commentaire en plus ;
 *   order-c.xml : comme a, MAIS xmlns:c="urn:shop:Customer" (majuscule) :
 *                 une URI se compare caractere par caractere, c'est donc
 *                 un AUTRE namespace.
 *
 * Le piege verifie : dans un DOM namespace-aware, getElementsByTagName(
 * "inv:amount") compare le nom ECRIT : 2 resultats dans a, 0 dans b.
 * Un code qui depend des prefixes casse des que l'expediteur change de
 * surnom. Tu vas ecrire les outils qui ne regardent que {URI}local.
 *
 * Cette fois, pas de resolution a la main : le DOM namespace-aware fait
 * le travail de l'exercice 6 (getNamespaceURI, getLocalName,
 * lookupNamespaceURI).
 *
 *
 * ==================================================================
 * TODO 1 : newNamespaceAwareBuilder()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * La DocumentBuilderFactory est livree avec les namespaces ETEINTS
 * (0.2.17 S5). Allume-les avant de fabriquer le builder, sinon
 * getNamespaceURI() et getLocalName() rendent null.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : trois lignes.
 *
 *
 * ==================================================================
 * TODO 2 : expandedName(node)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "{URI}local" si le noeud (element ou attribut) a un namespace, sinon
 * juste "local". Jamais le prefixe.
 *
 * -- Essayons a la main --
 *
 *   racine de a (inv:order) et de b (order) -> "{urn:shop:invoice}order"
 *   attribut sku -> "sku"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 3 : xsiType(element)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * L'attribut s'ecrit xsi:type dans a et s:type dans b : cherche-le par
 * NAMESPACE (getAttributeNS(XSI, "type")). Sa valeur "m:Euro" /
 * "euro:Euro" est un QName : lookupNamespaceURI(prefixe) remonte les
 * portees pour toi (prefixe null = namespace par defaut). Rends le type
 * etendu, ou null si l'element n'a pas de xsi:type.
 *
 * -- Essayons a la main --
 *
 *   amount de a -> "m:Euro" -> m = urn:shop:money -> "{urn:shop:money}Euro"
 *   amount de b -> "euro:Euro" -> meme resultat
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 4 : signature(element)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu fabriques la "photo d'identite" d'un sous-arbre, qui ne depend PAS
 * de la facon de l'ecrire :
 *     nomEtendu + "{" + attributs + "}[" + enfants + "]"
 *   - attributs : "nomEtendu=valeur" tries, joints par ",", SANS les
 *     declarations xmlns (leur namespace est XMLNS_ATTRIBUTE_NS_URI) ;
 *     pour xsi:type, la valeur est xsiType(element) (resolue) ;
 *   - enfants, dans l'ordre : un element -> sa signature (recursion) ;
 *     un texte non blanc -> '...' avec le texte strip() ; le reste
 *     (commentaires, blancs d'indentation) est ignore.
 *
 * -- Essayons a la main --
 *
 *   <p:x xmlns:p='urn:p' b='2' p:a='1'>  hi <!--c--><y/></p:x>
 *   attributs : b=2 et {urn:p}a=1 -> tries -> b=2,{urn:p}a=1
 *   enfants   : 'hi' puis y{}[]
 *   -> "{urn:p}x{b=2,{urn:p}a=1}['hi'y{}[]]"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 2 et TODO 3, et la signature s'appelle elle-meme.
 *
 *
 * ==================================================================
 * TODO 5 : sameIgnoringPrefixes(a, b)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Deux fichiers disent-ils la meme chose ? Compare les signatures de
 * leurs racines.
 *
 * -- Essayons a la main --
 *
 *   (a, b) -> true ; (a, c) -> false (Customer != customer) ; (a, a) -> true
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 1 et TODO 4.
 *
 *
 * ==================================================================
 * TODO 6 : prefixesByNamespace(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Preuve que le prefixe n'est qu'un surnom : pour chaque URI UTILISEE
 * dans un nom d'element ou d'attribut, liste les prefixes employes
 * ("" pour le namespace par defaut). Les declarations xmlns ne comptent
 * pas, et une URI qui n'apparait que dans une VALEUR (urn:shop:money,
 * dans xsi:type) non plus.
 *
 * -- Essayons a la main --
 *
 *   a -> {XSI=[xsi], urn:shop:customer=[c], urn:shop:invoice=[inv]}
 *   b -> {XSI=[s], urn:shop:customer=[k], urn:shop:invoice=[, inv]}
 *        (dans b, les elements utilisent le defaut "", l'attribut id inv:)
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "ranger le prefixe d'un noeud sous son URI" sert pour les
 * elements ET pour les attributs.
 *
 *
 * ==================================================================
 * TODO 7 : totalAmount(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Additionne tous les {urn:shop:invoice}amount, quel que soit leur
 * prefixe. En BigDecimal (12.50 + 7.25 = 19.75 pile).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : getElementsByTagNameNS + une boucle.
 *
 *
 * Exemple a verifier :
 *
 *   naif : getElementsByTagName("inv:amount") -> 2 dans a, 0 dans b (deja ecrit, le piege)
 *   expandedName, xsiType : voir TODO 2 et 3 ; xsiType(customer) == null
 *   signature de l'exemple du TODO 4
 *   sameIgnoringPrefixes : (a,b) true, (a,c) false, (b,a) true
 *   prefixesByNamespace : voir TODO 6
 *   totalAmount(a) == totalAmount(b) == 19.75
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - factory.setNamespaceAware(true);
 *   - node.getNamespaceURI(), node.getLocalName(), node.getPrefix()
 *   - element.hasAttributeNS(XSI, "type"), element.getAttributeNS(XSI, "type")
 *   - element.lookupNamespaceURI(prefixOuNull)
 *   - XMLConstants.XMLNS_ATTRIBUTE_NS_URI ("http://www.w3.org/2000/xmlns/")
 *   - doc.getElementsByTagNameNS("*", "*") : tous les elements
 *   - Map<String, SortedSet<String>> m = new TreeMap<>(); m.computeIfAbsent(uri, u -> new TreeSet<>()).add(p)
 *   - new BigDecimal(text.strip()), total = total.add(...)
 */
public class Exercise07_NamespaceAwareDom {

    public static final String XSI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
    public static final String INVOICE = "urn:shop:invoice";

    public static DocumentBuilder newNamespaceAwareBuilder() throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer newNamespaceAwareBuilder()");
    }

    public static String expandedName(Node node) {
        throw new UnsupportedOperationException("TODO 2 : implementer expandedName()");
    }

    public static String xsiType(Element element) {
        throw new UnsupportedOperationException("TODO 3 : implementer xsiType()");
    }

    public static String signature(Element element) {
        throw new UnsupportedOperationException("TODO 4 : implementer signature()");
    }

    public static boolean sameIgnoringPrefixes(Path a, Path b) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer sameIgnoringPrefixes()");
    }

    public static Map<String, SortedSet<String>> prefixesByNamespace(Document doc) {
        throw new UnsupportedOperationException("TODO 6 : implementer prefixesByNamespace()");
    }

    public static BigDecimal totalAmount(Document doc) {
        throw new UnsupportedOperationException("TODO 7 : implementer totalAmount()");
    }

    public static void main(String[] args) throws Exception {
        DocumentBuilder builder = newNamespaceAwareBuilder();
        ExerciseChecker.check("newNamespaceAwareBuilder().isNamespaceAware()", builder.isNamespaceAware());
        Path pa = Fixtures.path("ex07/order-a.xml");
        Path pb = Fixtures.path("ex07/order-b.xml");
        Path pc = Fixtures.path("ex07/order-c.xml");
        Document a = builder.parse(pa.toFile());
        Document b = builder.parse(pb.toFile());

        ExerciseChecker.check("le piege : getElementsByTagName(\"inv:amount\") -> 2 dans a, 0 dans b",
                a.getElementsByTagName("inv:amount").getLength() == 2 && b.getElementsByTagName("inv:amount").getLength() == 0);

        ExerciseChecker.check("expandedName(racine a) == expandedName(racine b) == {urn:shop:invoice}order",
                expandedName(a.getDocumentElement()).equals("{urn:shop:invoice}order")
                        && expandedName(b.getDocumentElement()).equals("{urn:shop:invoice}order"));
        Element lineA = (Element) a.getElementsByTagNameNS(INVOICE, "line").item(0);
        ExerciseChecker.check("expandedName(attribut sku) == sku", expandedName(lineA.getAttributeNode("sku")).equals("sku"));
        ExerciseChecker.check("expandedName(attribut inv:id) == {urn:shop:invoice}id",
                expandedName(a.getDocumentElement().getAttributeNodeNS(INVOICE, "id")).equals("{urn:shop:invoice}id"));

        Element amountA = (Element) a.getElementsByTagNameNS(INVOICE, "amount").item(0);
        Element amountB = (Element) b.getElementsByTagNameNS(INVOICE, "amount").item(0);
        ExerciseChecker.check("xsiType(amount de a) == {urn:shop:money}Euro", "{urn:shop:money}Euro".equals(xsiType(amountA)));
        ExerciseChecker.check("xsiType(amount de b) == {urn:shop:money}Euro (s:type, euro:)", "{urn:shop:money}Euro".equals(xsiType(amountB)));
        ExerciseChecker.check("xsiType(customer) == null",
                xsiType((Element) a.getElementsByTagNameNS("urn:shop:customer", "customer").item(0)) == null);

        Element sample = builder.parse(new InputSource(new StringReader(
                "<p:x xmlns:p='urn:p' b='2' p:a='1'>  hi <!--c--><y/></p:x>"))).getDocumentElement();
        ExerciseChecker.check("signature de l'exemple == {urn:p}x{b=2,{urn:p}a=1}['hi'y{}[]]",
                signature(sample).equals("{urn:p}x{b=2,{urn:p}a=1}['hi'y{}[]]"));
        ExerciseChecker.check("signature(a) ne contient aucun prefixe ecrit (inv:, c:, m:)",
                !signature(a.getDocumentElement()).matches(".*\\b(inv|c|m):.*"));

        ExerciseChecker.check("sameIgnoringPrefixes(a, b) == true", sameIgnoringPrefixes(pa, pb));
        ExerciseChecker.check("sameIgnoringPrefixes(b, a) == true", sameIgnoringPrefixes(pb, pa));
        ExerciseChecker.check("sameIgnoringPrefixes(a, c) == false (Customer != customer)", !sameIgnoringPrefixes(pa, pc));

        ExerciseChecker.check("prefixesByNamespace(a)", prefixesByNamespace(a).equals(Map.of(
                XSI, set("xsi"), "urn:shop:customer", set("c"), INVOICE, set("inv"))));
        ExerciseChecker.check("prefixesByNamespace(b) : urn:shop:invoice ecrit avec \"\" ET inv",
                prefixesByNamespace(b).equals(Map.of(XSI, set("s"), "urn:shop:customer", set("k"), INVOICE, set("", "inv"))));
        ExerciseChecker.check("prefixesByNamespace : cles triees (TreeMap)",
                List.copyOf(prefixesByNamespace(a).keySet()).equals(List.of(XSI, "urn:shop:customer", INVOICE)));

        ExerciseChecker.check("totalAmount(a) == 19.75", totalAmount(a).compareTo(new BigDecimal("19.75")) == 0);
        ExerciseChecker.check("totalAmount(b) == 19.75 (prefixe different, meme resultat)", totalAmount(b).compareTo(new BigDecimal("19.75")) == 0);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static SortedSet<String> set(String... values) {
        return new TreeSet<>(List.of(values));
    }
}
