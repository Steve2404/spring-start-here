package vorkurs02_xml.solutions;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Corrige de l'exercice 4. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise04_DtdValidationReport.
 */
public class Solution04_DtdValidationReport {

    public enum Severity { WARNING, ERROR, FATAL }

    public enum Status { VALID, INVALID, NOT_WELL_FORMED, WELL_FORMED_NO_DTD }

    public record Problem(Severity severity, int line, String message) {
    }

    public record Report(Status status, List<Problem> problems) {
    }

    public record Book(String id, String title, String lang, Optional<String> sequelTitle) {
    }

    public static DocumentBuilder newValidatingBuilder(List<Problem> sink) throws ParserConfigurationException {
        // setValidating(true) active les contraintes VC de la DTD ; ACCESS_EXTERNAL_DTD = "file"
        // autorise SEULEMENT les DTD locales (pas http). La config se fait AVANT newDocumentBuilder().
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
                sink.add(new Problem(Severity.WARNING, e.getLineNumber(), e.getMessage()));
            }

            @Override
            public void error(SAXParseException e) {
                // error() = violation de validite : on NOTE et on laisse le parseur continuer,
                // c'est ce qui permet de collecter TOUTES les erreurs d'un fichier.
                sink.add(new Problem(Severity.ERROR, e.getLineNumber(), e.getMessage()));
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                // fatalError() = document pas well-formed : aucun moyen de continuer, on relance.
                sink.add(new Problem(Severity.FATAL, e.getLineNumber(), e.getMessage()));
                throw e;
            }
        });
        return builder;
    }

    public static Report check(Path xml) {
        // L'ordre des tests suit la hierarchie du cours : pas well-formed > pas de DTD > invalide.
        // Sans DOCTYPE, le parseur validant emet 2 erreurs "no grammar" : ce ne sont pas des
        // violations de NOTRE DTD, on les jette.
        List<Problem> problems = new ArrayList<>();
        Document doc;
        try {
            doc = newValidatingBuilder(problems).parse(xml.toFile());
        } catch (SAXParseException e) {
            return new Report(Status.NOT_WELL_FORMED, List.copyOf(problems));
        } catch (SAXException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (doc.getDoctype() == null) {
            return new Report(Status.WELL_FORMED_NO_DTD, List.of());
        }
        boolean invalid = problems.stream().anyMatch(p -> p.severity() == Severity.ERROR);
        return new Report(invalid ? Status.INVALID : Status.VALID, List.copyOf(problems));
    }

    public static List<String> defaultedAttributes(Document doc) {
        // Attr.getSpecified() == false : l'attribut n'etait PAS dans le fichier, c'est la DTD
        // qui l'a ajoute (valeur par defaut ou #FIXED). On trie par nom pour un ordre stable.
        List<String> result = new ArrayList<>();
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element e = (Element) all.item(i);
            NamedNodeMap attrs = e.getAttributes();
            List<Attr> defaulted = new ArrayList<>();
            for (int k = 0; k < attrs.getLength(); k++) {
                Attr a = (Attr) attrs.item(k);
                if (!a.getSpecified()) {
                    defaulted.add(a);
                }
            }
            defaulted.sort(Comparator.comparing(Attr::getName));
            for (Attr a : defaulted) {
                result.add(e.getTagName() + "@" + a.getName() + "=" + a.getValue());
            }
        }
        return result;
    }

    public static Map<String, Book> readLibrary(Path xml) throws Exception {
        // Parseur NON validant, mais il lit quand meme la DTD : les valeurs par defaut (lang)
        // arrivent dans le DOM et getElementById marche, car la DTD declare id comme type ID.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        Document doc = factory.newDocumentBuilder().parse(xml.toFile());
        Map<String, Book> result = new LinkedHashMap<>();
        NodeList books = doc.getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {
            Element book = (Element) books.item(i);
            String sequel = book.getAttribute("sequel");
            Optional<String> sequelTitle = sequel.isEmpty()
                    ? Optional.empty()
                    : Optional.ofNullable(doc.getElementById(sequel)).map(Solution04_DtdValidationReport::titleOf);
            result.put(book.getAttribute("id"),
                    new Book(book.getAttribute("id"), titleOf(book), book.getAttribute("lang"), sequelTitle));
        }
        return result;
    }

    private static String titleOf(Element book) {
        return book.getElementsByTagName("title").item(0).getTextContent();
    }

    public static Map<Status, List<String>> summarize(List<Path> files) {
        // EnumMap garde l'ordre de declaration des Status : le rapport sort toujours pareil.
        Map<Status, List<String>> result = new EnumMap<>(Status.class);
        files.stream()
                .sorted()
                .forEach(f -> result.computeIfAbsent(check(f).status(), s -> new ArrayList<>())
                        .add(f.getFileName().toString()));
        return result;
    }
}
