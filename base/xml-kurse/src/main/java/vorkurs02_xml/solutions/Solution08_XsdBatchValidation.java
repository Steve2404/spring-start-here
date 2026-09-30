package vorkurs02_xml.solutions;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Corrige de l'exercice 8. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise08_XsdBatchValidation.
 */
public class Solution08_XsdBatchValidation {

    public record Violation(int line, String code, String message) {
    }

    public record FileReport(String file, boolean wellFormed, List<Violation> violations) {
    }

    public static Schema loadSchema(Path xsd) throws SAXException {
        // Le schema est lu UNE fois (couteux) puis reutilise ; aucun acces externe (DTD, xs:import
        // distant) n'est permis : la config se fait avant newSchema().
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newSchema(xsd.toFile());
    }

    public static String codeOf(String message) {
        // Le message est traduit (allemand ici !) mais le code "cvc-..." devant ':' ne l'est jamais :
        // c'est lui qu'on utilise pour trier et compter.
        int colon = message.indexOf(':');
        return message.startsWith("cvc-") && colon > 0 ? message.substring(0, colon) : "FATAL";
    }

    public static FileReport validate(Schema schema, Path xml) {
        // Un Validator par fichier (il n'est pas thread-safe). error() note et laisse continuer :
        // on obtient TOUTES les violations d'un fichier en un seul passage.
        Validator validator = schema.newValidator();
        List<Violation> violations = new ArrayList<>();
        validator.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) {
                violations.add(new Violation(e.getLineNumber(), codeOf(e.getMessage()), e.getMessage()));
            }

            public void fatalError(SAXParseException e) throws SAXException {
                violations.add(new Violation(e.getLineNumber(), "FATAL", e.getMessage()));
                throw e;
            }
        });
        boolean wellFormed = true;
        try {
            validator.validate(new StreamSource(xml.toFile()));
        } catch (SAXParseException e) {
            wellFormed = false;
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new FileReport(xml.getFileName().toString(), wellFormed, List.copyOf(violations));
    }

    public static List<Violation> rootCauses(FileReport report) {
        // Une faute de facette produit 2 messages sur la MEME ligne (la facette, puis
        // cvc-type/cvc-attribute "valeur invalide") : on ne garde que le premier de chaque ligne.
        Set<Integer> seen = new HashSet<>();
        return report.violations().stream().filter(v -> seen.add(v.line())).toList();
    }

    public static List<FileReport> batch(Schema schema, List<Path> files) {
        return files.stream()
                .map(f -> validate(schema, f))
                .sorted(Comparator.comparing(FileReport::file))
                .toList();
    }

    public static Map<String, Long> countByCode(List<FileReport> reports) {
        // On compte des CAUSES, pas des messages : sinon chaque faute de facette compterait double.
        return reports.stream()
                .flatMap(r -> rootCauses(r).stream())
                .collect(Collectors.groupingBy(Violation::code, TreeMap::new, Collectors.counting()));
    }

    public static String formatReport(List<FileReport> reports) {
        // Trois formes de ligne selon l'etat du fichier ; String.join garde le rapport stable.
        List<String> lines = new ArrayList<>();
        for (FileReport r : reports) {
            List<Violation> causes = rootCauses(r);
            if (!r.wellFormed()) {
                lines.add(r.file() + " : NON WELL-FORMED (l." + causes.get(causes.size() - 1).line() + ")");
            } else if (causes.isEmpty()) {
                lines.add(r.file() + " : OK");
            } else {
                lines.add(r.file() + " : " + causes.size() + " cause(s) -> " + causes.stream()
                        .map(v -> "l." + v.line() + " " + v.code())
                        .collect(Collectors.joining(", ")));
            }
        }
        return String.join("\n", lines);
    }
}
