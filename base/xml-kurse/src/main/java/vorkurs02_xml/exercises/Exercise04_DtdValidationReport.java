package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * EXERCICE 4 - Rapport de validation DTD : well-formed, valid, et ce que la DTD ajoute en douce (niveau : avance)
 * ============================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Une equipe recoit des exports de bibliotheque (fixtures/ex04/). Le
 * contrat est ecrit dans library.dtd (ouvre-le). Tu dois construire
 * l'outil de controle qui classe chaque fichier :
 *
 *   NOT_WELL_FORMED     : le parseur s'arrete (fatalError). Une DTD ne
 *                         peut JAMAIS rattraper ca (cours 0.2.9 S4).
 *   WELL_FORMED_NO_DTD  : pas de DOCTYPE, donc rien a valider.
 *   INVALID             : well-formed, mais au moins une regle de la DTD
 *                         est violee (error()).
 *   VALID               : well-formed ET conforme a sa DTD.
 *
 * Faits verifies avec le parseur du JDK 17 sur CES fichiers :
 *   - en mode validant, error() est appele pour CHAQUE violation et le
 *     parseur continue : 04-invalid-attributes.xml en donne 3, toutes
 *     ligne 4 (id manquant, lang="it" hors liste, isbn non declare) ;
 *   - 05-invalid-ids.xml : ID "b1" en double (ligne 8) + IDREF "b9"
 *     sans cible (signale ligne 12, a la FIN du document : le parseur
 *     ne peut savoir qu'a la fin qu'aucun id="b9" n'existe) ;
 *   - 09-no-dtd.xml en mode validant : 2 erreurs parasites ("root ...
 *     must match DOCTYPE root null", "no grammar found") ;
 *   - un parseur NON validant lit QUAND MEME la DTD externe : valeurs par
 *     defaut des attributs et getElementById fonctionnent ;
 *   - 10-valid-override.xml redeclare lang dans son internal subset avec
 *     le defaut "de" : c'est lui qui gagne (le subset interne est lu en
 *     premier, et pour un attribut la 1re declaration fait foi).
 *
 *
 * ==================================================================
 * TODO 1 : newValidatingBuilder(sink)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Par defaut, le parseur est un correcteur qui s'arrete a la premiere
 * faute d'orthographe grave (fatalError) et IGNORE le reglement de la
 * classe (la DTD). Tu dois lui dire : "verifie aussi le reglement"
 * (validating), "tu peux lire le reglement s'il est dans un fichier
 * LOCAL" (ACCESS_EXTERNAL_DTD = "file"), et "note chaque faute dans ce
 * cahier (sink) au lieu de crier dans la console" (ErrorHandler).
 * Regle du cahier : warning -> WARNING, error -> ERROR (on continue),
 * fatalError -> FATAL puis on RELANCE l'exception (on ne peut pas
 * continuer un texte illisible).
 *
 * -- Le plan --
 *
 *   1. Creer la factory, activer la validation, autoriser "file".
 *   2. Creer le builder (APRES la configuration).
 *   3. Lui donner un ErrorHandler qui remplit sink avec
 *      Problem(severite, ligne, message).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une classe anonyme ErrorHandler avec 3 methodes courtes.
 *
 *
 * ==================================================================
 * TODO 2 : check(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu donnes un seul verdict par fichier, dans l'ordre de gravite :
 * illisible d'abord, puis "pas de reglement", puis "fautes au
 * reglement", sinon "tout est bon". Pour "pas de reglement", la liste
 * de problemes rendue est VIDE (les 2 erreurs parasites ne concernent
 * pas le contrat).
 *
 * -- Essayons a la main --
 *
 *   03-invalid-order.xml : <author> avant <title> -> INVALID, [ERROR ligne 7]
 *     (ligne 7 = la fin de </book>, la ou le parseur constate que le
 *     contenu ne colle pas a (title,author+,year?))
 *   08-not-wellformed.xml : </Author> -> NOT_WELL_FORMED, [FATAL ligne 6]
 *   09-no-dtd.xml -> WELL_FORMED_NO_DTD, []
 *
 * -- Le plan --
 *
 *   1. Parser avec le builder du TODO 1.
 *   2. SAXParseException -> NOT_WELL_FORMED avec les problemes notes.
 *   3. getDoctype() == null -> WELL_FORMED_NO_DTD, liste vide.
 *   4. Au moins un ERROR -> INVALID ; sinon VALID.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : le TODO 1.
 *
 *
 * ==================================================================
 * TODO 3 : defaultedAttributes(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * La DTD peut glisser des attributs que l'auteur n'a jamais ecrits :
 * lang="fr" par defaut, status="active" (#FIXED). Dans le DOM, ils
 * ressemblent aux autres... sauf Attr.getSpecified(), qui vaut false.
 * Liste-les sous la forme "element@attribut=valeur", element par
 * element dans l'ordre du document, et par nom d'attribut dans un meme
 * element.
 *
 * -- Essayons a la main --
 *
 *   01-valid-external.xml :
 *     book b1 : lang ecrit, status ajoute        -> book@status=active
 *     book b2 : lang et status ajoutes           -> book@lang=fr, book@status=active
 *
 * -- Le plan --
 *
 *   1. Parcourir tous les elements (getElementsByTagName("*")).
 *   2. Pour chacun, garder les Attr non specifies, les trier par nom.
 *   3. Formater et ajouter.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 4 : readLibrary(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu lis les livres avec un parseur NON validant (juste
 * ACCESS_EXTERNAL_DTD = "file"). Surprise du cours 0.2.11 S7 : il lit
 * quand meme la DTD, donc :
 *   - getAttribute("lang") rend "fr" meme si le fichier ne l'ecrit pas ;
 *   - doc.getElementById("b1") trouve le livre, parce que la DTD dit que
 *     id est de type ID (sans DTD, getElementById rend null !).
 * Pour chaque livre : Book(id, titre, lang, titre de la suite via
 * sequel -> getElementById, ou Optional.empty()).
 *
 * -- Essayons a la main --
 *
 *   01-valid-external.xml ->
 *     b1 = Book(b1, "Der Prozess", "de", empty)
 *     b2 = Book(b2, "Le Chateau", "fr", Optional["Der Prozess"])
 *   10-valid-override.xml -> b1.lang() == "de" (defaut du subset interne)
 *
 * -- Le plan --
 *
 *   1. Factory non validante + acces "file", parser.
 *   2. Pour chaque <book> : id, titre, lang, puis sequel.
 *   3. Map id -> Book, dans l'ordre du document.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "le titre d'un element book" sert 2 fois (le livre et sa suite) :
 * titleOf(Element).
 *
 *
 * ==================================================================
 * TODO 5 : summarize(files)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le chef veut un tableau : pour chaque verdict, la liste triee des
 * fichiers. Les verdicts dans l'ordre de l'enum (EnumMap), seulement
 * ceux qui ont au moins un fichier.
 *
 * -- Le plan --
 *
 *   1. Trier les fichiers.
 *   2. Pour chacun, check(...).status() et ranger le nom du fichier.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : computeIfAbsent fait tout.
 *
 *
 * Exemple a verifier :
 *
 *   01, 02, 10 -> VALID sans probleme
 *   03 -> INVALID [ERROR l.7] ; 04 -> INVALID, 3 ERROR ligne 4 ; 05 -> INVALID lignes [8, 12]
 *   06 -> INVALID [l.3] (racine book au lieu de library) ; 07 -> INVALID lignes [4, 9]
 *   08 -> NOT_WELL_FORMED [FATAL l.6] ; 09 -> WELL_FORMED_NO_DTD []
 *   defaultedAttributes(01) == [book@status=active, book@lang=fr, book@status=active]
 *   defaultedAttributes(02) == [memo@priority=low] ; (10) == [book@lang=de, book@status=active]
 *   readLibrary(01) et (10) : voir TODO 4
 *   summarize(tous) == {VALID=[01,02,10], INVALID=[03..07], NOT_WELL_FORMED=[08], WELL_FORMED_NO_DTD=[09]}
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - factory.setValidating(true); factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
 *   - builder.setErrorHandler(new ErrorHandler() { public void warning(SAXParseException e) {...}
 *       public void error(SAXParseException e) {...}
 *       public void fatalError(SAXParseException e) throws SAXException {...; throw e;} });
 *   - e.getLineNumber(), e.getMessage()
 *   - doc.getDoctype() (null sans DOCTYPE)
 *   - NamedNodeMap attrs = element.getAttributes(); Attr a = (Attr) attrs.item(k); a.getSpecified()
 *   - doc.getElementById("b1") ; book.getElementsByTagName("title").item(0).getTextContent()
 *   - Map<Status, List<String>> m = new EnumMap<>(Status.class); m.computeIfAbsent(s, k -> new ArrayList<>()).add(...)
 */
public class Exercise04_DtdValidationReport {

    public enum Severity { WARNING, ERROR, FATAL }

    public enum Status { VALID, INVALID, NOT_WELL_FORMED, WELL_FORMED_NO_DTD }

    public record Problem(Severity severity, int line, String message) {
    }

    public record Report(Status status, List<Problem> problems) {
    }

    public record Book(String id, String title, String lang, Optional<String> sequelTitle) {
    }

    public static DocumentBuilder newValidatingBuilder(List<Problem> sink) throws ParserConfigurationException {
        throw new UnsupportedOperationException("TODO 1 : implementer newValidatingBuilder()");
    }

    public static Report check(Path xml) {
        throw new UnsupportedOperationException("TODO 2 : implementer check()");
    }

    public static List<String> defaultedAttributes(Document doc) {
        throw new UnsupportedOperationException("TODO 3 : implementer defaultedAttributes()");
    }

    public static Map<String, Book> readLibrary(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer readLibrary()");
    }

    public static Map<Status, List<String>> summarize(List<Path> files) {
        throw new UnsupportedOperationException("TODO 5 : implementer summarize()");
    }

    public static void main(String[] args) throws Exception {
        List<Problem> sink = new ArrayList<>();
        DocumentBuilder validating = newValidatingBuilder(sink);
        ExerciseChecker.check("newValidatingBuilder : builder validant", validating.isValidating());
        validating.parse(Fixtures.path("ex04/03-invalid-order.xml").toFile());
        ExerciseChecker.check("newValidatingBuilder : l'erreur de 03 est notee dans sink (ERROR ligne 7)",
                sink.size() == 1 && sink.get(0).severity() == Severity.ERROR && sink.get(0).line() == 7);

        for (String ok : List.of("01-valid-external.xml", "02-valid-internal.xml", "10-valid-override.xml")) {
            Report r = check(Fixtures.path("ex04/" + ok));
            ExerciseChecker.check(ok + " -> VALID sans probleme", r.status() == Status.VALID && r.problems().isEmpty());
        }
        expectInvalid("03-invalid-order.xml", List.of(7));
        expectInvalid("04-invalid-attributes.xml", List.of(4, 4, 4));
        expectInvalid("05-invalid-ids.xml", List.of(8, 12));
        expectInvalid("06-invalid-root.xml", List.of(3));
        expectInvalid("07-invalid-fixed.xml", List.of(4, 9));
        Report broken = check(Fixtures.path("ex04/08-not-wellformed.xml"));
        ExerciseChecker.check("08 -> NOT_WELL_FORMED, [FATAL ligne 6]", broken.status() == Status.NOT_WELL_FORMED
                && broken.problems().size() == 1 && broken.problems().get(0).severity() == Severity.FATAL
                && broken.problems().get(0).line() == 6);
        ExerciseChecker.check("09 -> WELL_FORMED_NO_DTD, aucun probleme",
                check(Fixtures.path("ex04/09-no-dtd.xml")).equals(new Report(Status.WELL_FORMED_NO_DTD, List.of())));

        ExerciseChecker.check("defaultedAttributes(01) == [book@status=active, book@lang=fr, book@status=active]",
                defaultedAttributes(parsePlain("01-valid-external.xml"))
                        .equals(List.of("book@status=active", "book@lang=fr", "book@status=active")));
        ExerciseChecker.check("defaultedAttributes(02) == [memo@priority=low]",
                defaultedAttributes(parsePlain("02-valid-internal.xml")).equals(List.of("memo@priority=low")));
        ExerciseChecker.check("defaultedAttributes(10) == [book@lang=de, book@status=active] (subset interne gagne)",
                defaultedAttributes(parsePlain("10-valid-override.xml")).equals(List.of("book@lang=de", "book@status=active")));
        ExerciseChecker.check("defaultedAttributes(09, sans DTD) == []",
                defaultedAttributes(parsePlain("09-no-dtd.xml")).isEmpty());

        Map<String, Book> lib = readLibrary(Fixtures.path("ex04/01-valid-external.xml"));
        ExerciseChecker.check("readLibrary(01) : b1 = Book(b1, Der Prozess, de, vide)",
                lib.get("b1").equals(new Book("b1", "Der Prozess", "de", Optional.empty())));
        ExerciseChecker.check("readLibrary(01) : b2 = lang par defaut fr + suite trouvee par getElementById",
                lib.get("b2").equals(new Book("b2", "Le Chateau", "fr", Optional.of("Der Prozess"))));
        ExerciseChecker.check("readLibrary(01) : ordre b1, b2", List.copyOf(lib.keySet()).equals(List.of("b1", "b2")));
        ExerciseChecker.check("readLibrary(10) : lang == de",
                readLibrary(Fixtures.path("ex04/10-valid-override.xml")).get("b1").lang().equals("de"));

        List<Path> all;
        try (Stream<Path> s = Files.list(Fixtures.path("ex04"))) {
            all = s.filter(p -> p.toString().endsWith(".xml")).toList();
        }
        Map<Status, List<String>> summary = summarize(all);
        ExerciseChecker.check("summarize : 4 verdicts, dans l'ordre de l'enum",
                List.copyOf(summary.keySet()).equals(List.of(Status.VALID, Status.INVALID, Status.NOT_WELL_FORMED, Status.WELL_FORMED_NO_DTD)));
        ExerciseChecker.check("summarize : VALID == [01, 02, 10]", summary.get(Status.VALID)
                .equals(List.of("01-valid-external.xml", "02-valid-internal.xml", "10-valid-override.xml")));
        ExerciseChecker.check("summarize : INVALID == 03 a 07", summary.get(Status.INVALID).equals(List.of(
                "03-invalid-order.xml", "04-invalid-attributes.xml", "05-invalid-ids.xml", "06-invalid-root.xml", "07-invalid-fixed.xml")));
        ExerciseChecker.check("summarize : NOT_WELL_FORMED == [08], WELL_FORMED_NO_DTD == [09]",
                summary.get(Status.NOT_WELL_FORMED).equals(List.of("08-not-wellformed.xml"))
                        && summary.get(Status.WELL_FORMED_NO_DTD).equals(List.of("09-no-dtd.xml")));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static void expectInvalid(String file, List<Integer> lines) {
        Report r = check(Fixtures.path("ex04/" + file));
        List<Integer> got = r.problems().stream().map(Problem::line).toList();
        ExerciseChecker.check(file + " -> INVALID, ERROR aux lignes " + lines + " (obtenu " + r.status() + " " + got + ")",
                r.status() == Status.INVALID && got.equals(lines)
                        && r.problems().stream().allMatch(p -> p.severity() == Severity.ERROR));
    }

    private static Document parsePlain(String file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        return factory.newDocumentBuilder().parse(Fixtures.path("ex04/" + file).toFile());
    }
}
