package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EXERCICE 9 - Ecrire le XSD soi-meme : 7 morceaux de schema, 18 documents pour les juger (niveau : challenge)
 * ===========================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Ici, le code a ecrire est du XSD. Chaque TODO est une methode qui rend
 * un MORCEAU de schema (un text block Java """ ... """). main() colle
 * les 7 morceaux dans cet en-tete deja ecrit :
 *
 *   <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *              xmlns:c="urn:lib:catalog"
 *              targetNamespace="urn:lib:catalog"
 *              elementFormDefault="qualified">
 *     ...tes 7 morceaux...
 *   </xs:schema>
 *
 * puis valide les 18 fichiers de fixtures/ex09 : good-1 et good-2
 * doivent passer, et chaque bad-*.xml doit etre refuse avec, comme
 * PREMIER code d'erreur, exactement celui du tableau plus bas. Chaque
 * bad-*.xml ne viole qu'UNE regle : ouvre-les pour comprendre ce qui
 * est attendu. Consequence : il faut les facettes EXACTES demandees
 * (ex. minExclusive 0 et pas minInclusive 0.01, qui donnerait un autre code).
 *
 * Rappels du cours (0.2.14 / 0.2.15) :
 *   - pour referencer un type que TU declares : type="c:NomDuType"
 *     (c est lie au targetNamespace) ; type predefini : type="xs:string" ;
 *   - dans un complexType, les xs:attribute viennent APRES le modele de
 *     contenu (sequence/choice/all) ;
 *   - minOccurs/maxOccurs valent 1 par defaut ; maxOccurs="unbounded".
 *
 *
 * ==================================================================
 * TODO 1 : isbnType()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Un ISBN-13 moderne : 978 ou 979, puis exactement 10 chiffres, sans
 * tirets. Declare le simpleType NOMME "IsbnType" : restriction de
 * xs:string avec un pattern. Un pattern XSD est TOUJOURS ancre sur la
 * valeur entiere (pas besoin de ^ et $).
 *
 * -- Essayons a la main --
 *
 *   9782070368228 -> ok ; 978-2-07-036822-8 -> refuse (cvc-pattern-valid)
 *
 *
 * ==================================================================
 * TODO 2 : languageType()
 * ==================================================================
 *
 * simpleType "LanguageType" : une enumeration de, fr, en (sensible a la
 * casse : "FR" est refuse, cvc-enumeration-valid).
 *
 *
 * ==================================================================
 * TODO 3 : amountType()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Un prix est un nombre decimal STRICTEMENT positif (0 refuse), au plus
 * 9999.99, avec au plus 2 decimales. simpleType "AmountType",
 * restriction de xs:decimal avec 3 facettes : minExclusive,
 * maxInclusive, fractionDigits.
 *
 * -- Essayons a la main --
 *
 *   8.50, 19.9 -> ok ; 0 -> cvc-minExclusive-valid ;
 *   10000 -> cvc-maxInclusive-valid ; 8.505 -> cvc-fractionDigits-valid
 *
 *
 * ==================================================================
 * TODO 4 : priceType()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * <price currency="CHF">19.9</price> : du TEXTE (un AmountType) ET un
 * attribut. C'est un complexType "PriceType" a contenu simple :
 * xs:simpleContent > xs:extension base="c:AmountType" > xs:attribute.
 * L'attribut currency : 3 lettres MAJUSCULES (pattern, type anonyme
 * inline), facultatif, valeur par defaut "EUR".
 *
 * -- Essayons a la main --
 *
 *   currency="eur" -> cvc-pattern-valid ; pas de currency -> le DOM
 *   valide par le schema verra currency="EUR" (verifie dans main()).
 *
 *
 * ==================================================================
 * TODO 5 : metaType()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * <meta> contient publisher (obligatoire) et year (facultatif, type
 * xs:gYear), dans N'IMPORTE QUEL ordre, chacun au plus une fois :
 * c'est exactement xs:all. complexType "MetaType".
 *
 * -- Essayons a la main --
 *
 *   <year>1942</year><publisher>..</publisher> -> ok (ordre libre)
 *   2 publisher -> cvc-complex-type.2.4.a ; year "deux mille" -> cvc-datatype-valid.1.2.1
 *
 *
 * ==================================================================
 * TODO 6 : bookType()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * complexType "BookType", une SEQUENCE (ordre impose) :
 *   title (xs:string) ; author (xs:string, 1 a 3 fois) ;
 *   price (c:PriceType) ; meta (c:MetaType, facultatif) ;
 *   puis UN CHOIX entre ebookUrl (xs:anyURI) et shelf (pattern
 *   lettre majuscule + 2 chiffres, type anonyme).
 * Attributs : isbn (c:IsbnType, obligatoire), lang (c:LanguageType,
 * defaut "fr"), status (xs:string, valeur FIXEE "catalogued").
 *
 * -- Essayons a la main --
 *
 *   4 auteurs -> cvc-complex-type.2.4.e ; 0 auteur -> 2.4.a ;
 *   price avant title -> 2.4.a ; shelf ET ebookUrl -> 2.4.d ;
 *   pas d'isbn -> cvc-complex-type.4 ; status="lost" -> cvc-complex-type.3.1
 *
 *
 * ==================================================================
 * TODO 7 : catalogElement()
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * La seule porte d'entree : l'element GLOBAL "catalog" (type anonyme)
 * qui contient de 0 a une infinite de book (c:BookType). good-2.xml est
 * un catalogue vide : il doit passer. bad-no-namespace.xml ecrit
 * <catalog> sans namespace : ce n'est PAS {urn:lib:catalog}catalog ->
 * cvc-elt.1.a.
 *
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * C'est tout l'esprit de l'exercice : chaque type NOMME est une boite
 * reutilisable (AmountType sert dans PriceType, PriceType dans BookType...).
 * Un type utilise une seule fois peut rester anonyme (inline).
 *
 *
 * Exemple a verifier (premier code d'erreur attendu) :
 *
 *   good-1, good-2 -> aucune erreur
 *   bad-isbn, bad-currency, bad-shelf -> cvc-pattern-valid
 *   bad-lang -> cvc-enumeration-valid
 *   bad-price-zero -> cvc-minExclusive-valid ; bad-price-max -> cvc-maxInclusive-valid
 *   bad-price-digits -> cvc-fractionDigits-valid ; bad-year -> cvc-datatype-valid.1.2.1
 *   bad-authors -> cvc-complex-type.2.4.e ; bad-both-places -> cvc-complex-type.2.4.d
 *   bad-meta-twice, bad-no-author, bad-order -> cvc-complex-type.2.4.a
 *   bad-missing-isbn -> cvc-complex-type.4 ; bad-status -> cvc-complex-type.3.1
 *   bad-no-namespace -> cvc-elt.1.a
 *   DOM avec setSchema(schema) sur good-1 : 1er livre lang="fr" (non specifie !),
 *   status="catalogued", currency="EUR" ; sans schema, lang == ""
 *
 *
 * Indices techniques XSD (a lire seulement si le plan est clair mais
 * que l'ecriture bloque) :
 *
 *   <xs:simpleType name="X"><xs:restriction base="xs:string"><xs:pattern value="..."/></xs:restriction></xs:simpleType>
 *   <xs:enumeration value="de"/>   <xs:minExclusive value="0"/>   <xs:fractionDigits value="2"/>
 *   <xs:complexType name="PriceType"><xs:simpleContent><xs:extension base="c:AmountType">
 *       <xs:attribute name="currency" default="EUR"><xs:simpleType>...</xs:simpleType></xs:attribute>
 *   </xs:extension></xs:simpleContent></xs:complexType>
 *   <xs:all> ... </xs:all>   <xs:choice> ... </xs:choice>
 *   <xs:attribute name="isbn" type="c:IsbnType" use="required"/>   fixed="catalogued"
 *   <xs:element name="catalog"><xs:complexType><xs:sequence>
 *       <xs:element name="book" type="c:BookType" minOccurs="0" maxOccurs="unbounded"/>
 *   </xs:sequence></xs:complexType></xs:element>
 */
public class Exercise09_XsdAuthoring {

    public static String isbnType() {
        throw new UnsupportedOperationException("TODO 1 : implementer isbnType()");
    }

    public static String languageType() {
        throw new UnsupportedOperationException("TODO 2 : implementer languageType()");
    }

    public static String amountType() {
        throw new UnsupportedOperationException("TODO 3 : implementer amountType()");
    }

    public static String priceType() {
        throw new UnsupportedOperationException("TODO 4 : implementer priceType()");
    }

    public static String metaType() {
        throw new UnsupportedOperationException("TODO 5 : implementer metaType()");
    }

    public static String bookType() {
        throw new UnsupportedOperationException("TODO 6 : implementer bookType()");
    }

    public static String catalogElement() {
        throw new UnsupportedOperationException("TODO 7 : implementer catalogElement()");
    }

    public static void main(String[] args) throws Exception {
        String xsd = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"urn:lib:catalog\"\n"
                + "           targetNamespace=\"urn:lib:catalog\" elementFormDefault=\"qualified\">\n"
                + isbnType() + languageType() + amountType() + priceType() + metaType() + bookType() + catalogElement()
                + "</xs:schema>\n";
        Schema schema = compile(xsd);
        ExerciseChecker.check("les 7 morceaux forment un schema valide", schema != null);

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("good-1.xml", "");
        expected.put("good-2.xml", "");
        expected.put("bad-isbn.xml", "cvc-pattern-valid");
        expected.put("bad-lang.xml", "cvc-enumeration-valid");
        expected.put("bad-price-zero.xml", "cvc-minExclusive-valid");
        expected.put("bad-price-max.xml", "cvc-maxInclusive-valid");
        expected.put("bad-price-digits.xml", "cvc-fractionDigits-valid");
        expected.put("bad-currency.xml", "cvc-pattern-valid");
        expected.put("bad-meta-twice.xml", "cvc-complex-type.2.4.a");
        expected.put("bad-year.xml", "cvc-datatype-valid.1.2.1");
        expected.put("bad-authors.xml", "cvc-complex-type.2.4.e");
        expected.put("bad-no-author.xml", "cvc-complex-type.2.4.a");
        expected.put("bad-order.xml", "cvc-complex-type.2.4.a");
        expected.put("bad-both-places.xml", "cvc-complex-type.2.4.d");
        expected.put("bad-shelf.xml", "cvc-pattern-valid");
        expected.put("bad-missing-isbn.xml", "cvc-complex-type.4");
        expected.put("bad-status.xml", "cvc-complex-type.3.1");
        expected.put("bad-no-namespace.xml", "cvc-elt.1.a");
        for (Map.Entry<String, String> e : expected.entrySet()) {
            List<String> codes = errorCodes(schema, Fixtures.path("ex09/" + e.getKey()));
            String first = codes.isEmpty() ? "" : codes.get(0);
            ExerciseChecker.check(e.getKey() + " -> " + (e.getValue().isEmpty() ? "valide" : e.getValue())
                    + " (obtenu " + (codes.isEmpty() ? "valide" : codes) + ")", first.equals(e.getValue()));
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(schema);
        Document doc = factory.newDocumentBuilder().parse(Fixtures.path("ex09/good-1.xml").toFile());
        NodeList books = doc.getElementsByTagNameNS("urn:lib:catalog", "book");
        Element first = (Element) books.item(0);
        Element firstPrice = (Element) first.getElementsByTagNameNS("urn:lib:catalog", "price").item(0);
        ExerciseChecker.check("DOM + schema : lang par defaut == fr, et getSpecified() == false",
                first.getAttribute("lang").equals("fr") && !first.getAttributeNode("lang").getSpecified());
        ExerciseChecker.check("DOM + schema : status fixe == catalogued", first.getAttribute("status").equals("catalogued"));
        ExerciseChecker.check("DOM + schema : currency par defaut == EUR", firstPrice.getAttribute("currency").equals("EUR"));
        DocumentBuilderFactory plain = DocumentBuilderFactory.newInstance();
        plain.setNamespaceAware(true);
        Element noSchema = (Element) plain.newDocumentBuilder().parse(Fixtures.path("ex09/good-1.xml").toFile())
                .getElementsByTagNameNS("urn:lib:catalog", "book").item(0);
        ExerciseChecker.check("DOM SANS schema : lang == \"\" (les defauts XSD n'existent que si on valide)",
                noSchema.getAttribute("lang").isEmpty());

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Schema compile(String xsd) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try {
            return factory.newSchema(new StreamSource(new StringReader(xsd)));
        } catch (SAXParseException e) {
            System.out.println("Ton XSD ne compile pas (ligne " + e.getLineNumber() + " du schema assemble) : " + e.getMessage());
            throw e;
        }
    }

    /** Codes cvc-... de toutes les erreurs de validation, dans l'ordre. */
    private static List<String> errorCodes(Schema schema, Path xml) throws Exception {
        Validator validator = schema.newValidator();
        List<String> codes = new ArrayList<>();
        validator.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) {
                codes.add(e.getMessage().substring(0, e.getMessage().indexOf(':')));
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        validator.validate(new StreamSource(xml.toFile()));
        return codes;
    }
}
