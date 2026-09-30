package vorkurs02_xml.exercises;

import org.xml.sax.SAXException;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.validation.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * EXERCICE 8 - Valider un lot de commandes contre un XSD et produire un rapport de causes (niveau : avance / entretien)
 * =================================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex08/order.xsd decrit une commande (lis-le en entier : pattern,
 * positiveInteger + maxInclusive, enumeration, choice, minOccurs /
 * maxOccurs, attribut required, simpleContent avec attribut, namespace
 * cible urn:shop:order). Huit commandes arrivent (order-01 a 08). Le
 * service client veut UN rapport lisible : pour chaque fichier, les
 * VRAIES causes, avec leur ligne.
 *
 * Deux faits verifies sur ce JDK, qui rendent l'exercice interessant :
 *
 *   1. Les messages sont TRADUITS (sur cette machine : en allemand !),
 *      mais ils commencent toujours par un code stable :
 *        "cvc-pattern-valid: Wert 'a-1' ist nicht Facet-gueltig ..."
 *        "cvc-pattern-valid: Value 'a-1' is not facet-valid ..."
 *      -> on ne teste JAMAIS le texte d'un message, seulement le code.
 *
 *   2. UNE faute de facette produit DEUX messages sur la meme ligne :
 *        l.2 cvc-pattern-valid  puis  l.2 cvc-attribute.3
 *        l.6 cvc-minInclusive-valid  puis  l.6 cvc-type.3.1.3
 *      Le 2e ne fait que dire "donc la valeur est invalide" : ce n'est
 *      pas une nouvelle cause.
 *
 * Messages bruts obtenus (codes seulement) :
 *   01 : aucun
 *   02 : l.2 pattern-valid, l.2 attribute.3                      (id="a-1")
 *   03 : l.6 minInclusive-valid, l.6 type.3.1.3,                 (quantity 0)
 *        l.11 maxInclusive-valid, l.11 type.3.1.3,               (quantity 120)
 *        l.12 datatype-valid.1.2.1, l.12 complex-type.2.2        (price "abc")
 *   04 : l.2 enumeration-valid + attribute.3 (status="lost"), l.7 idem (YEN),
 *        l.9 minLength-valid + type.3.1.3 (pickup "AB")
 *   05 : l.29 complex-type.2.4.e   (6e <line> : maxOccurs=5 depasse)
 *   06 : l.2 complex-type.4 (id manquant), l.10 complex-type.2.4.a
 *        (delivery ET pickup : le choice n'en veut qu'un)
 *   07 : l.2 elt.1.a (pas de namespace : "order" sans URI n'est PAS
 *        {urn:shop:order}order, le schema ne le connait pas)
 *   08 : FATAL l.3 (</Customer> : pas well-formed, la validation s'arrete)
 *
 *
 * ==================================================================
 * TODO 1 : loadSchema(xsd)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu apprends le reglement (le XSD) UNE fois, et tu le gardes sous le
 * bras (un objet Schema reutilisable, et sans danger entre threads).
 * Comme dans le cours 0.2.22, tu interdis au lecteur du reglement
 * d'aller chercher des pages ailleurs (DTD ou schemas externes).
 *
 * -- Le plan --
 *
 *   1. SchemaFactory pour W3C XML Schema.
 *   2. ACCESS_EXTERNAL_DTD = "" et ACCESS_EXTERNAL_SCHEMA = "".
 *   3. newSchema(fichier).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 2 : codeOf(message)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu gardes l'etiquette du message, pas la phrase : tout ce qui est
 * avant le premier ':' si le message commence par "cvc-", sinon
 * "FATAL" (les erreurs de bonne formation n'ont pas de code cvc).
 *
 * -- Essayons a la main --
 *
 *   "cvc-type.3.1.3: Wert '0' ..."       -> "cvc-type.3.1.3"
 *   "The element type \"customer\" ..."  -> "FATAL"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 3 : validate(schema, xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Par defaut, le Validator s'arrete a la premiere faute (il LANCE
 * l'exception). Toi, tu veux la liste COMPLETE : tu lui donnes un
 * ErrorHandler qui note chaque error() et laisse continuer. Pour un
 * fatalError(), on note Violation(ligne, "FATAL", message) puis on
 * relance (impossible de continuer) ; le rapport dit wellFormed = false.
 *
 * -- Le plan --
 *
 *   1. schema.newValidator() (un par fichier : pas thread-safe).
 *   2. ErrorHandler : warning ignore, error note, fatalError note + relance.
 *   3. validate(new StreamSource(fichier)) ; attraper SAXParseException.
 *   4. FileReport(nom du fichier, wellFormed, violations).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 2.
 *
 *
 * ==================================================================
 * TODO 4 : rootCauses(report)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Quand un enfant tombe, la maitresse note "Tom est tombe" puis "Tom
 * pleure" : une seule cause. Garde, pour chaque ligne, SEULEMENT la
 * premiere violation (dans l'ordre d'arrivee).
 *
 * -- Essayons a la main --
 *
 *   03 : [l.6 minInc, l.6 type, l.11 maxInc, l.11 type, l.12 datatype, l.12 complex-type.2.2]
 *     -> [l.6 minInclusive-valid, l.11 maxInclusive-valid, l.12 datatype-valid.1.2.1]
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : un filter avec un Set des lignes deja vues.
 *
 *
 * ==================================================================
 * TODO 5 : batch(schema, files)
 * ==================================================================
 *
 * Valider chaque fichier (TODO 3), trier les rapports par nom de fichier.
 *
 *
 * ==================================================================
 * TODO 6 : countByCode(reports)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le chef veut savoir quelles fautes reviennent le plus : compte les
 * CAUSES (TODO 4) par code, dans une Map triee par code.
 *
 * -- Essayons a la main --
 *
 *   cvc-enumeration-valid : 2 (status de 04, currency de 04) ; tous les autres : 1 ;
 *   FATAL : 1 (08). En tout 12 causes pour 19 messages bruts.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 4. Le reste est un groupingBy.
 *
 *
 * ==================================================================
 * TODO 7 : formatReport(reports)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Une ligne par fichier, lignes separees par "\n" (pas de "\n" final) :
 *   "order-01.xml : OK"
 *   "order-03.xml : 3 cause(s) -> l.6 cvc-minInclusive-valid, l.11 cvc-maxInclusive-valid, l.12 cvc-datatype-valid.1.2.1"
 *   "order-08.xml : NON WELL-FORMED (l.3)"   (la ligne de la derniere violation)
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 4.
 *
 *
 * Exemple a verifier :
 *
 *   loadSchema OK ; codeOf : 3 cas
 *   validate : 01 valide ; 02 -> 2 messages [pattern-valid, attribute.3] ligne 2 ;
 *              03 -> 6 messages ; 08 -> wellFormed == false, [FATAL l.3]
 *   rootCauses(03) : 3 causes lignes [6, 11, 12]
 *   batch : 8 rapports tries ; 19 messages bruts, 12 causes
 *   countByCode : voir TODO 6
 *   formatReport : les 8 lignes exactes ecrites dans main()
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
 *   - factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
 *   - Validator v = schema.newValidator(); v.setErrorHandler(...); v.validate(new StreamSource(file))
 *   - Set<Integer> seen = new HashSet<>(); list.stream().filter(v -> seen.add(v.line())).toList()
 *   - Collectors.groupingBy(Violation::code, TreeMap::new, Collectors.counting())
 *   - Collectors.joining(", ")
 */
public class Exercise08_XsdBatchValidation {

    public record Violation(int line, String code, String message) {
    }

    public record FileReport(String file, boolean wellFormed, List<Violation> violations) {
    }

    public static Schema loadSchema(Path xsd) throws SAXException {
        throw new UnsupportedOperationException("TODO 1 : implementer loadSchema()");
    }

    public static String codeOf(String message) {
        throw new UnsupportedOperationException("TODO 2 : implementer codeOf()");
    }

    public static FileReport validate(Schema schema, Path xml) {
        throw new UnsupportedOperationException("TODO 3 : implementer validate()");
    }

    public static List<Violation> rootCauses(FileReport report) {
        throw new UnsupportedOperationException("TODO 4 : implementer rootCauses()");
    }

    public static List<FileReport> batch(Schema schema, List<Path> files) {
        throw new UnsupportedOperationException("TODO 5 : implementer batch()");
    }

    public static Map<String, Long> countByCode(List<FileReport> reports) {
        throw new UnsupportedOperationException("TODO 6 : implementer countByCode()");
    }

    public static String formatReport(List<FileReport> reports) {
        throw new UnsupportedOperationException("TODO 7 : implementer formatReport()");
    }

    public static void main(String[] args) throws Exception {
        Schema schema = loadSchema(Fixtures.path("ex08/order.xsd"));
        ExerciseChecker.check("loadSchema : un Schema est rendu", schema != null);

        ExerciseChecker.check("codeOf(cvc-type.3.1.3: ...) == cvc-type.3.1.3", codeOf("cvc-type.3.1.3: Wert '0' ...").equals("cvc-type.3.1.3"));
        ExerciseChecker.check("codeOf(cvc-elt.1.a: ...) == cvc-elt.1.a", codeOf("cvc-elt.1.a: Cannot find ...").equals("cvc-elt.1.a"));
        ExerciseChecker.check("codeOf(message sans cvc) == FATAL", codeOf("The element type \"customer\": boom").equals("FATAL"));

        FileReport r01 = validate(schema, Fixtures.path("ex08/order-01.xml"));
        ExerciseChecker.check("validate(01) : well-formed, aucune violation",
                r01.equals(new FileReport("order-01.xml", true, List.of())));
        FileReport r02 = validate(schema, Fixtures.path("ex08/order-02.xml"));
        ExerciseChecker.check("validate(02) : [cvc-pattern-valid, cvc-attribute.3] ligne 2",
                r02.violations().stream().map(Violation::code).toList().equals(List.of("cvc-pattern-valid", "cvc-attribute.3"))
                        && r02.violations().stream().allMatch(v -> v.line() == 2));
        FileReport r03 = validate(schema, Fixtures.path("ex08/order-03.xml"));
        ExerciseChecker.check("validate(03) : 6 messages bruts", r03.violations().size() == 6);
        FileReport r08 = validate(schema, Fixtures.path("ex08/order-08.xml"));
        ExerciseChecker.check("validate(08) : wellFormed == false, [FATAL l.3]", !r08.wellFormed()
                && r08.violations().size() == 1 && r08.violations().get(0).code().equals("FATAL") && r08.violations().get(0).line() == 3);

        ExerciseChecker.check("rootCauses(03) : lignes [6, 11, 12]",
                rootCauses(r03).stream().map(Violation::line).toList().equals(List.of(6, 11, 12)));
        ExerciseChecker.check("rootCauses(03) : codes des causes",
                rootCauses(r03).stream().map(Violation::code).toList()
                        .equals(List.of("cvc-minInclusive-valid", "cvc-maxInclusive-valid", "cvc-datatype-valid.1.2.1")));

        List<Path> files;
        try (Stream<Path> s = Files.list(Fixtures.path("ex08"))) {
            files = s.filter(p -> p.toString().endsWith(".xml")).toList();
        }
        List<FileReport> reports = batch(schema, files);
        ExerciseChecker.check("batch : 8 rapports tries par nom",
                reports.stream().map(FileReport::file).toList().equals(
                        List.of("order-01.xml", "order-02.xml", "order-03.xml", "order-04.xml",
                                "order-05.xml", "order-06.xml", "order-07.xml", "order-08.xml")));
        ExerciseChecker.check("batch : 19 messages bruts en tout",
                reports.stream().mapToInt(r -> r.violations().size()).sum() == 19);
        ExerciseChecker.check("batch : 12 causes en tout", reports.stream().mapToInt(r -> rootCauses(r).size()).sum() == 12);

        Map<String, Long> expectedCounts = new TreeMap<>(Map.ofEntries(
                Map.entry("FATAL", 1L), Map.entry("cvc-complex-type.2.4.a", 1L), Map.entry("cvc-complex-type.2.4.e", 1L),
                Map.entry("cvc-complex-type.4", 1L), Map.entry("cvc-datatype-valid.1.2.1", 1L), Map.entry("cvc-elt.1.a", 1L),
                Map.entry("cvc-enumeration-valid", 2L), Map.entry("cvc-maxInclusive-valid", 1L),
                Map.entry("cvc-minInclusive-valid", 1L), Map.entry("cvc-minLength-valid", 1L), Map.entry("cvc-pattern-valid", 1L)));
        Map<String, Long> counts = countByCode(reports);
        ExerciseChecker.check("countByCode == les 11 codes attendus (enumeration x2)", counts.equals(expectedCounts));
        ExerciseChecker.check("countByCode : Map triee par code", counts instanceof TreeMap);

        String expectedReport = String.join("\n",
                "order-01.xml : OK",
                "order-02.xml : 1 cause(s) -> l.2 cvc-pattern-valid",
                "order-03.xml : 3 cause(s) -> l.6 cvc-minInclusive-valid, l.11 cvc-maxInclusive-valid, l.12 cvc-datatype-valid.1.2.1",
                "order-04.xml : 3 cause(s) -> l.2 cvc-enumeration-valid, l.7 cvc-enumeration-valid, l.9 cvc-minLength-valid",
                "order-05.xml : 1 cause(s) -> l.29 cvc-complex-type.2.4.e",
                "order-06.xml : 2 cause(s) -> l.2 cvc-complex-type.4, l.10 cvc-complex-type.2.4.a",
                "order-07.xml : 1 cause(s) -> l.2 cvc-elt.1.a",
                "order-08.xml : NON WELL-FORMED (l.3)");
        String report = formatReport(reports);
        ExerciseChecker.check("formatReport == le rapport attendu (8 lignes)", report.equals(expectedReport));
        if (!report.equals(expectedReport)) {
            System.out.println("Obtenu :\n" + report);
        }

        ExerciseChecker.summary();
    }
}
