package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * EXERCICE 18 - CAPSTONE : le labo XML, une chaine d'import couche par couche (niveau : capstone / entretien)
 * ========================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Le labo du campus recoit des commandes d'appareils (fixtures/ex18,
 * schema lab-order.xsd, namespace urn:campus:lab). Le cours 0.2.22 dit :
 * ne jamais "reparer au hasard", mais verifier COUCHE PAR COUCHE, dans
 * l'ordre, et s'arreter a la premiere couche en echec :
 *
 *   1. WELL_FORMED : lisible ? (SAX securise : DOCTYPE refuse ici aussi)
 *   2. NAMESPACE   : la racine est-elle bien {urn:campus:lab}order ?
 *   3. SCHEMA      : conforme au XSD ? (1re violation seulement)
 *   4. BUSINESS    : les regles que le XSD ne sait pas dire (XPath) :
 *        QUANTITY_LIMIT        somme des quantites > 50
 *        DUPLICATE_SERIAL      deux appareils avec le meme serial
 *        DELIVERY_BEFORE_ORDER livraison avant la date de commande
 *   5. DONE        : importe ; details = [devices=N, quantity=Q]
 *
 * Tu reutilises TOUT le chapitre : SAX (14), StAX (15), XSD (8), DOM
 * securise (17), XPath avec NamespaceContext (10).
 *
 * Rapport reel attendu (verifie) :
 *   01-ok.xml : OK [devices=2, quantity=22]
 *   02-ok-prefixed.xml : OK [devices=1, quantity=1]          (lab: au lieu du defaut : meme namespace)
 *   03-not-wellformed.xml : WELL_FORMED l.4                  (</Quantity>)
 *   04-doctype.xml : WELL_FORMED l.2                         (DOCTYPE interdit)
 *   05-wrong-namespace.xml : NAMESPACE l.2                   (urn:campus:labo)
 *   06-no-namespace.xml : NAMESPACE l.2
 *   07-schema-enum.xml : SCHEMA l.3 cvc-enumeration-valid    (type "laser")
 *   08-schema-missing.xml : SCHEMA l.5 cvc-complex-type.2.4.b (<delivery> manquant)
 *   09-business-quantity.xml : BUSINESS [QUANTITY_LIMIT]     (30 + 30)
 *   10-business-multi.xml : BUSINESS [DUPLICATE_SERIAL, DELIVERY_BEFORE_ORDER]
 *
 *
 * ==================================================================
 * TODO 1 : checkWellFormed(xml)
 * ==================================================================
 *
 * SAX namespace-aware, secure processing, disallow-doctype-decl,
 * ACCESS_EXTERNAL_DTD = "" ; parse avec un DefaultHandler. OK ->
 * Optional.empty() ; SAXParseException -> Optional.of(ligne).
 *
 *
 * ==================================================================
 * TODO 2 : checkNamespace(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Avant de lire toute la lettre, on regarde l'ADRESSE sur l'enveloppe.
 * StAX (DTD coupee) : nextTag() jusqu'a la racine, compare {URI}local a
 * {urn:campus:lab}order. Faux -> la ligne (reader.getLocation()
 * .getLineNumber()) ; juste -> vide. On ne lit RIEN d'autre.
 *
 *
 * ==================================================================
 * TODO 3 : schemaViolation(xml, schema)
 * ==================================================================
 *
 * La 1re violation XSD : Violation(ligne, code cvc-...). L'ErrorHandler
 * memorise la 1re erreur puis RELANCE pour arreter la validation.
 * Aucune -> vide.
 *
 *
 * ==================================================================
 * TODO 4 : loadDocument(xml)
 * ==================================================================
 *
 * DOM namespace-aware ET securise (secure processing, DOCTYPE interdit,
 * ACCESS_EXTERNAL_DTD / SCHEMA = ""), ErrorHandler silencieux.
 *
 *
 * ==================================================================
 * TODO 5 : businessViolations(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le XSD verifie la FORME ; le metier verifie le SENS. Trois questions
 * XPath 1.0 evaluees en BOOLEAN, avec un NamespaceContext l -> urn:campus:lab,
 * dans CET ordre :
 *   sum(//l:device/l:quantity) > 50                                -> QUANTITY_LIMIT
 *   count(//l:device[@serial = preceding::l:device/@serial]) > 0   -> DUPLICATE_SERIAL
 *   livraison < date de commande                                   -> DELIVERY_BEFORE_ORDER
 * Astuce pour les dates (XPath 1.0 ne connait pas xs:date) :
 * number(translate('2026-08-31', '-', '')) = 20260831, comparable.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "une XPath deja liee au namespace du labo" sert ici et au TODO 6.
 *
 *
 * ==================================================================
 * TODO 6 : importFile(xml, schema)
 * ==================================================================
 *
 * La chaine : TODO 1, 2, 3, puis 4 + 5, en s'arretant au 1er echec ;
 * ImportResult(nom du fichier, phase, ok, ligne (0 si sans objet),
 * details) : SCHEMA -> [code] ; BUSINESS -> la liste des regles ;
 * DONE -> ["devices=N", "quantity=Q"] (count() et sum() en XPath).
 *
 *
 * ==================================================================
 * TODO 7 : report(results)
 * ==================================================================
 *
 * Une ligne par resultat, jointes par "\n", exactement au format du
 * "Rapport reel attendu" ci-dessus. Un switch sur la phase.
 *
 *
 * Exemple a verifier : chaque couche testee seule, puis le rapport complet au caractere pres.
 *
 *
 * Indices techniques Java : voir les exercices 8, 10, 14, 15 et 17 ; et
 *   - reader.getLocation().getLineNumber()
 *   - (Boolean) xpath.evaluate(expr, doc, XPathConstants.BOOLEAN)
 *   - ((Double) xpath.evaluate("count(//l:device)", doc, XPathConstants.NUMBER)).intValue()
 */
public class Exercise18_XmlLabPipeline {

    public static final String LAB = "urn:campus:lab";

    public enum Phase { WELL_FORMED, NAMESPACE, SCHEMA, BUSINESS, DONE }

    public record Violation(int line, String code) {
    }

    public record ImportResult(String file, Phase phase, boolean ok, int line, List<String> details) {
    }

    public static Optional<Integer> checkWellFormed(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer checkWellFormed()");
    }

    public static Optional<Integer> checkNamespace(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 2 : implementer checkNamespace()");
    }

    public static Optional<Violation> schemaViolation(Path xml, Schema schema) throws Exception {
        throw new UnsupportedOperationException("TODO 3 : implementer schemaViolation()");
    }

    public static Document loadDocument(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer loadDocument()");
    }

    public static List<String> businessViolations(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer businessViolations()");
    }

    public static ImportResult importFile(Path xml, Schema schema) throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer importFile()");
    }

    public static String report(List<ImportResult> results) {
        throw new UnsupportedOperationException("TODO 7 : implementer report()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("checkWellFormed(01) vide", checkWellFormed(file("01-ok.xml")).isEmpty());
        ExerciseChecker.check("checkWellFormed(03) == ligne 4", checkWellFormed(file("03-not-wellformed.xml")).equals(Optional.of(4)));
        ExerciseChecker.check("checkWellFormed(04, DOCTYPE) == ligne 2", checkWellFormed(file("04-doctype.xml")).equals(Optional.of(2)));

        ExerciseChecker.check("checkNamespace(01) et (02, prefixe lab:) vides",
                checkNamespace(file("01-ok.xml")).isEmpty() && checkNamespace(file("02-ok-prefixed.xml")).isEmpty());
        ExerciseChecker.check("checkNamespace(05, urn:campus:labo) == ligne 2", checkNamespace(file("05-wrong-namespace.xml")).equals(Optional.of(2)));
        ExerciseChecker.check("checkNamespace(06, sans namespace) == ligne 2", checkNamespace(file("06-no-namespace.xml")).equals(Optional.of(2)));

        Schema schema = loadSchema();
        ExerciseChecker.check("schemaViolation(01) vide", schemaViolation(file("01-ok.xml"), schema).isEmpty());
        ExerciseChecker.check("schemaViolation(07) == (3, cvc-enumeration-valid)",
                schemaViolation(file("07-schema-enum.xml"), schema).equals(Optional.of(new Violation(3, "cvc-enumeration-valid"))));
        ExerciseChecker.check("schemaViolation(08) == (5, cvc-complex-type.2.4.b)",
                schemaViolation(file("08-schema-missing.xml"), schema).equals(Optional.of(new Violation(5, "cvc-complex-type.2.4.b"))));

        ExerciseChecker.check("loadDocument(02) : racine {urn:campus:lab}order",
                LAB.equals(loadDocument(file("02-ok-prefixed.xml")).getDocumentElement().getNamespaceURI()));
        ExerciseChecker.check("loadDocument(04) refuse le DOCTYPE", failsToLoad(file("04-doctype.xml")));

        ExerciseChecker.check("businessViolations(01) == []", businessViolations(loadDocument(file("01-ok.xml"))).isEmpty());
        ExerciseChecker.check("businessViolations(09) == [QUANTITY_LIMIT]",
                businessViolations(loadDocument(file("09-business-quantity.xml"))).equals(List.of("QUANTITY_LIMIT")));
        ExerciseChecker.check("businessViolations(10) == [DUPLICATE_SERIAL, DELIVERY_BEFORE_ORDER]",
                businessViolations(loadDocument(file("10-business-multi.xml"))).equals(List.of("DUPLICATE_SERIAL", "DELIVERY_BEFORE_ORDER")));

        ExerciseChecker.check("importFile(01) == DONE [devices=2, quantity=22]", importFile(file("01-ok.xml"), schema)
                .equals(new ImportResult("01-ok.xml", Phase.DONE, true, 0, List.of("devices=2", "quantity=22"))));
        ExerciseChecker.check("importFile(05) == NAMESPACE l.2", importFile(file("05-wrong-namespace.xml"), schema)
                .equals(new ImportResult("05-wrong-namespace.xml", Phase.NAMESPACE, false, 2, List.of())));

        List<ImportResult> results = new ArrayList<>();
        try (Stream<Path> s = Files.list(Fixtures.path("ex18"))) {
            for (Path p : s.filter(x -> x.toString().endsWith(".xml")).sorted().toList()) {
                results.add(importFile(p, schema));
            }
        }
        String expected = String.join("\n",
                "01-ok.xml : OK [devices=2, quantity=22]",
                "02-ok-prefixed.xml : OK [devices=1, quantity=1]",
                "03-not-wellformed.xml : WELL_FORMED l.4",
                "04-doctype.xml : WELL_FORMED l.2",
                "05-wrong-namespace.xml : NAMESPACE l.2",
                "06-no-namespace.xml : NAMESPACE l.2",
                "07-schema-enum.xml : SCHEMA l.3 cvc-enumeration-valid",
                "08-schema-missing.xml : SCHEMA l.5 cvc-complex-type.2.4.b",
                "09-business-quantity.xml : BUSINESS [QUANTITY_LIMIT]",
                "10-business-multi.xml : BUSINESS [DUPLICATE_SERIAL, DELIVERY_BEFORE_ORDER]");
        String report = report(results);
        ExerciseChecker.check("report == le rapport attendu (10 lignes)", report.equals(expected));
        if (!report.equals(expected)) {
            System.out.println("Obtenu :\n" + report);
        }
        ExerciseChecker.check("2 imports reussis sur 10", results.stream().filter(ImportResult::ok).count() == 2);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Path file(String name) {
        return Fixtures.path("ex18/" + name);
    }

    private static Schema loadSchema() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newSchema(file("lab-order.xsd").toFile());
    }

    private static boolean failsToLoad(Path xml) {
        try {
            loadDocument(xml);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
