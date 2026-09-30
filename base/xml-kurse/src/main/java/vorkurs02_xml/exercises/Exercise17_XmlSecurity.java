package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/**
 * EXERCICE 17 - Securite XML : voir une XXE fonctionner, puis fermer toutes les portes (niveau : challenge)
 * =======================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Tout se passe EN LOCAL, dans fixtures/ex17 (aucun acces reseau) :
 *   secret.txt          un faux secret "MOT-DE-PASSE-FACTICE-123"
 *   clean.xml           une commande normale (2 items)
 *   xxe-file.xml        <!ENTITY secret SYSTEM "secret.txt"> puis &secret; dans <note>
 *   lol-small.xml       "billion laughs" miniature : 3 niveaux, 1111 expansions, 3000 caracteres
 *   lol-big.xml         5 niveaux : 111111 expansions
 *   legacy-internal.xml un vieux fichier de confiance avec une entite INTERNE (&company;)
 *   external-dtd.xml    <!DOCTYPE order SYSTEM "order.dtd">
 *   param-entity.xml    <!ENTITY % remote SYSTEM "order.dtd"> %remote;
 *   main.xsd            fait <xs:include schemaLocation="part.xsd"/> ; standalone.xsd n'inclut rien
 *
 * Faits verifies sur ce JDK 17 (cours 0.2.21) :
 *   - un DocumentBuilderFactory PAR DEFAUT lit secret.txt : la <note>
 *     contient le secret (main() te le montre) ;
 *   - avec disallow-doctype-decl : refus EXPLICITE (SAXParseException) ;
 *   - avec seulement les entites externes desactivees : PAS d'erreur, la
 *     note devient "" (le contenu disparait en silence !) ;
 *   - la limite par defaut est 64000 expansions (lol-big refuse,
 *     JAXP00010001) ; avec jdk.xml.entityExpansionLimit = 1110, lol-small
 *     est refuse, avec 1111 il passe (meme compte qu'a l'exercice 3) ;
 *   - StAX avec SUPPORT_DTD = false : &secret; / &company; deviennent
 *     "referenced, but not declared" (XMLStreamException) ; un DOCTYPE
 *     SYSTEM sans entite est simplement ignore ;
 *   - SchemaFactory avec ACCESS_EXTERNAL_SCHEMA = "" refuse main.xsd
 *     ("'file' access is not allowed") et accepte standalone.xsd.
 *
 *
 * ==================================================================
 * TODO 1 : secureDocumentBuilder()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Un document XML peut contenir une "lettre" qui dit au facteur (le
 * parseur) : "va chercher le contenu du tiroir secret.txt et colle-le
 * ici". Un facteur trop obeissant le fait. Le tien refuse carrement
 * toute lettre qui commence par un DOCTYPE, et, par precaution, on lui
 * interdit AUSSI chaque porte une par une (defense en profondeur) :
 *   FEATURE_SECURE_PROCESSING = true ; disallow-doctype-decl = true ;
 *   external-general-entities = false ; external-parameter-entities = false ;
 *   ACCESS_EXTERNAL_DTD = "" ; ACCESS_EXTERNAL_SCHEMA = "" ;
 *   setXIncludeAware(false) ; setExpandEntityReferences(false).
 * Toujours sur la FACTORY, AVANT newDocumentBuilder().
 *
 *
 * ==================================================================
 * TODO 2 : legacyDocumentBuilder(maxExpansions)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Certains vieux fichiers DE CONFIANCE utilisent des entites INTERNES
 * (&company;). Pour eux : DOCTYPE permis, mais rien d'externe (entites
 * generales et parametres false, load-external-dtd false, les deux
 * ACCESS_EXTERNAL_* = ""), et un plafond :
 * setAttribute("jdk.xml.entityExpansionLimit", String.valueOf(max)).
 * Pose aussi un ErrorHandler silencieux (new DefaultHandler()).
 *
 *
 * ==================================================================
 * TODO 3 : secureSaxParser()
 * ==================================================================
 *
 * Namespace-aware, secure processing, disallow-doctype sur la factory ;
 * ACCESS_EXTERNAL_DTD = "" sur le SAXParser.
 *
 *
 * ==================================================================
 * TODO 4 : secureStaxFactory()
 * ==================================================================
 *
 * SUPPORT_DTD = false, IS_SUPPORTING_EXTERNAL_ENTITIES = false,
 * ACCESS_EXTERNAL_DTD = "".
 *
 *
 * ==================================================================
 * TODO 5 : secureSchemaFactory()
 * ==================================================================
 *
 * W3C XML Schema, secure processing, ACCESS_EXTERNAL_DTD = "" et
 * ACCESS_EXTERNAL_SCHEMA = "" (un XSD peut aussi aller chercher d'autres
 * fichiers : 0.2.21 S5).
 *
 *
 * ==================================================================
 * TODO 6 : risks(xmlText)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Avant d'ouvrir un colis, on le secoue pour savoir ce qu'il contient.
 * Sur le TEXTE brut, detecte (expressions regulieres) :
 *   DOCTYPE          "<!DOCTYPE"
 *   EXTERNAL_DTD     "<!DOCTYPE nom SYSTEM|PUBLIC"
 *   INTERNAL_ENTITY  "<!ENTITY nom 'valeur'" (general, valeur entre quotes)
 *   EXTERNAL_ENTITY  "<!ENTITY [%] nom SYSTEM|PUBLIC"
 *   PARAMETER_ENTITY "<!ENTITY %"
 * Ce tri n'est PAS une protection (le parseur securise l'est) : il sert
 * a EXPLIQUER un refus, sans dependre des messages d'erreur (traduits).
 *
 *   xxe-file         -> {DOCTYPE, EXTERNAL_ENTITY}
 *   param-entity     -> {DOCTYPE, EXTERNAL_ENTITY, PARAMETER_ENTITY}
 *   legacy-internal  -> {DOCTYPE, INTERNAL_ENTITY}
 *
 *
 * ==================================================================
 * TODO 7 : importOrder(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * La politique d'import complete :
 *   1. calculer les risques (TODO 6) ;
 *   2. aucun risque -> parseur strict (TODO 1) ;
 *      seulement DOCTYPE + INTERNAL_ENTITY -> parseur legacy (TODO 2)
 *      avec 1000 expansions max ;
 *      tout autre risque -> refus sans meme parser ;
 *   3. echec de parsing -> refus ;
 *   4. accepte -> ImportResult(true, nombre de <item>, texte de <note>, risques).
 * Refus -> ImportResult(false, 0, "", risques).
 *
 *   clean -> (true, 2, "RAS", {}) ; legacy-internal -> (true, 3, "Client : Acme & Fils", {DOCTYPE, INTERNAL_ENTITY})
 *   lol-small -> refuse (1111 > 1000) ; xxe-file, lol-big, external-dtd, param-entity -> refuses
 *
 *
 * Exemple a verifier : voir main() (et surtout : le secret ne doit JAMAIS sortir de tes parseurs).
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
 *   - f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
 *   - f.setFeature("http://xml.org/sax/features/external-general-entities", false);
 *   - f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
 *   - f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 *   - f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "") (DOM) ; parser.setProperty(...) (SAX) ;
 *     xmlInputFactory.setProperty(...) (StAX) ; schemaFactory.setProperty(...) (XSD)
 *   - Pattern.compile("<!ENTITY\\s+(%\\s+)?\\S+\\s+(SYSTEM|PUBLIC)").matcher(text).find()
 *   - EnumSet.noneOf(Risk.class) ; EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY).containsAll(risks)
 */
public class Exercise17_XmlSecurity {

    public enum Risk { DOCTYPE, EXTERNAL_DTD, INTERNAL_ENTITY, EXTERNAL_ENTITY, PARAMETER_ENTITY }

    public record ImportResult(boolean accepted, int items, String note, Set<Risk> risks) {
    }

    public static DocumentBuilder secureDocumentBuilder() throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer secureDocumentBuilder()");
    }

    public static DocumentBuilder legacyDocumentBuilder(int maxExpansions) throws Exception {
        throw new UnsupportedOperationException("TODO 2 : implementer legacyDocumentBuilder()");
    }

    public static SAXParser secureSaxParser() throws Exception {
        throw new UnsupportedOperationException("TODO 3 : implementer secureSaxParser()");
    }

    public static XMLInputFactory secureStaxFactory() {
        throw new UnsupportedOperationException("TODO 4 : implementer secureStaxFactory()");
    }

    public static SchemaFactory secureSchemaFactory() throws SAXException {
        throw new UnsupportedOperationException("TODO 5 : implementer secureSchemaFactory()");
    }

    public static Set<Risk> risks(String xmlText) {
        throw new UnsupportedOperationException("TODO 6 : implementer risks()");
    }

    public static ImportResult importOrder(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer importOrder()");
    }

    public static void main(String[] args) throws Exception {
        String secret = Fixtures.read("ex17/secret.txt").strip();

        DocumentBuilder strict = secureDocumentBuilder();
        strict.setErrorHandler(new DefaultHandler());
        ExerciseChecker.check("strict : clean.xml passe (2 items)", strict.parse(file("clean.xml")).getElementsByTagName("item").getLength() == 2);
        ExerciseChecker.check("le piege : le parseur PAR DEFAUT lit le secret via l'entite externe",
                naiveNote("xxe-file.xml").contains(secret));
        for (String bad : new String[]{"xxe-file.xml", "lol-small.xml", "lol-big.xml", "legacy-internal.xml", "external-dtd.xml", "param-entity.xml"}) {
            ExerciseChecker.check("strict : " + bad + " refuse", fails(() -> strict.parse(file(bad))));
        }

        ExerciseChecker.check("legacy(1000) : legacy-internal -> note 'Client : Acme & Fils'",
                legacyDocumentBuilder(1000).parse(file("legacy-internal.xml")).getElementsByTagName("note").item(0)
                        .getTextContent().equals("Client : Acme & Fils"));
        Document silent = legacyDocumentBuilder(1000).parse(file("xxe-file.xml"));
        ExerciseChecker.check("legacy(1000) : xxe-file -> note VIDE en silence, pas de secret",
                silent.getElementsByTagName("note").item(0).getTextContent().isEmpty());
        ExerciseChecker.check("legacy(1110) : lol-small refuse (1111 expansions)", fails(() -> legacyDocumentBuilder(1110).parse(file("lol-small.xml"))));
        ExerciseChecker.check("legacy(1111) : lol-small passe (3000 caracteres)",
                legacyDocumentBuilder(1111).parse(file("lol-small.xml")).getElementsByTagName("note").item(0).getTextContent().length() == 3000);
        ExerciseChecker.check("legacy(64000) : lol-big refuse", fails(() -> legacyDocumentBuilder(64000).parse(file("lol-big.xml"))));

        SAXParser sax = secureSaxParser();
        ExerciseChecker.check("SAX securise : namespace-aware, clean passe", sax.isNamespaceAware() && !fails(() -> sax.parse(file("clean.xml"), new DefaultHandler())));
        ExerciseChecker.check("SAX securise : xxe-file refuse", fails(() -> sax.parse(file("xxe-file.xml"), new DefaultHandler())));

        XMLInputFactory stax = secureStaxFactory();
        ExerciseChecker.check("StAX securise : clean -> 2 items", staxItems(stax, "clean.xml") == 2);
        ExerciseChecker.check("StAX securise : xxe-file -> XMLStreamException", staxItems(stax, "xxe-file.xml") == -1);
        ExerciseChecker.check("StAX securise : external-dtd -> DTD ignoree, 1 item", staxItems(stax, "external-dtd.xml") == 1);

        SchemaFactory schemas = secureSchemaFactory();
        ExerciseChecker.check("XSD securise : standalone.xsd accepte", !fails(() -> schemas.newSchema(file("standalone.xsd"))));
        ExerciseChecker.check("XSD securise : main.xsd (xs:include) refuse", fails(() -> schemas.newSchema(file("main.xsd"))));

        ExerciseChecker.check("risks(clean) == {}", risks(Fixtures.read("ex17/clean.xml")).isEmpty());
        ExerciseChecker.check("risks(xxe-file) == {DOCTYPE, EXTERNAL_ENTITY}",
                risks(Fixtures.read("ex17/xxe-file.xml")).equals(EnumSet.of(Risk.DOCTYPE, Risk.EXTERNAL_ENTITY)));
        ExerciseChecker.check("risks(param-entity) == {DOCTYPE, EXTERNAL_ENTITY, PARAMETER_ENTITY}",
                risks(Fixtures.read("ex17/param-entity.xml")).equals(EnumSet.of(Risk.DOCTYPE, Risk.EXTERNAL_ENTITY, Risk.PARAMETER_ENTITY)));
        ExerciseChecker.check("risks(external-dtd) == {DOCTYPE, EXTERNAL_DTD}",
                risks(Fixtures.read("ex17/external-dtd.xml")).equals(EnumSet.of(Risk.DOCTYPE, Risk.EXTERNAL_DTD)));
        ExerciseChecker.check("risks(legacy-internal) == risks(lol-big) == {DOCTYPE, INTERNAL_ENTITY}",
                risks(Fixtures.read("ex17/legacy-internal.xml")).equals(EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY))
                        && risks(Fixtures.read("ex17/lol-big.xml")).equals(EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY)));

        ExerciseChecker.check("importOrder(clean) == (true, 2, RAS, {})",
                importOrder(Fixtures.path("ex17/clean.xml")).equals(new ImportResult(true, 2, "RAS", EnumSet.noneOf(Risk.class))));
        ExerciseChecker.check("importOrder(legacy-internal) == (true, 3, Client : Acme & Fils, ...)",
                importOrder(Fixtures.path("ex17/legacy-internal.xml")).equals(new ImportResult(true, 3, "Client : Acme & Fils",
                        EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY))));
        for (String bad : new String[]{"xxe-file.xml", "lol-small.xml", "lol-big.xml", "external-dtd.xml", "param-entity.xml"}) {
            ImportResult r = importOrder(Fixtures.path("ex17/" + bad));
            ExerciseChecker.check("importOrder(" + bad + ") refuse, sans fuite du secret",
                    !r.accepted() && r.items() == 0 && !r.note().contains(secret));
        }

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private interface Action {
        void run() throws Exception;
    }

    private static boolean fails(Action action) {
        try {
            action.run();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static java.io.File file(String name) {
        return Fixtures.path("ex17/" + name).toFile();
    }

    /** Le MAUVAIS code : une factory par defaut, sans aucune protection. A ne jamais copier. */
    private static String naiveNote(String name) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file(name));
        return doc.getElementsByTagName("note").item(0).getTextContent();
    }

    /** Nombre de <item> lus en StAX, ou -1 si le lecteur leve une XMLStreamException. */
    private static int staxItems(XMLInputFactory factory, String name) throws Exception {
        try (InputStream in = Files.newInputStream(Fixtures.path("ex17/" + name))) {
            XMLStreamReader r = factory.createXMLStreamReader(in);
            int items = 0;
            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("item")) {
                    items++;
                }
            }
            return items;
        } catch (XMLStreamException e) {
            return -1;
        }
    }
}
