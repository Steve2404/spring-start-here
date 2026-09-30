package vorkurs02_xml.drills.exercises;

import org.xml.sax.SAXException;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.parsers.SAXParser;
import java.nio.file.Path;
import java.util.List;

/**
 * DRILL 4 - L'API SAX (DefaultHandler, Attributes, Locator, prefixes, arret volontaire)
 * ===================================================================================
 *
 * Mode d'emploi : voir Drill01_DomApi. Donnees : Campus.file(). Chaque
 * TODO = un DefaultHandler anonyme qui redefinit UNE ou deux methodes
 * (entre crochets). Pour sortir un resultat d'une classe anonyme :
 * une List, un StringBuilder, ou un tableau a une case (int[] n = {0}).
 *
 *
 * -- Les TODO (methode visee entre crochets) --
 *
 * TODO 1  : newNamespaceAwareParser()        [SAXParserFactory.setNamespaceAware + newSAXParser]
 * TODO 2  : countElements(parser, xml)       [startElement] -> 25.
 * TODO 3  : firstGradeNames(parser, xml)     [qName, localName, uri] -> "g:grade|grade|urn:campus:grades".
 * TODO 4  : courseIds(parser, xml)           [Attributes.getValue(nom)] -> [C1, C2, C3].
 * TODO 5  : rootAttributeCount(parser, xml)  [Attributes.getLength] <campus> -> 2 (pas les xmlns !).
 * TODO 6  : attributeNames(parser, xml, id)  [getLocalName(i)] C3 -> [id, credits, level, status].
 * TODO 7  : personNames(parser, xml)         [characters + endElement] -> [Ana, Ben, Chloe, Dan].
 * TODO 8  : lineOfFirstGrade(parser, xml)    [setDocumentLocator + getLineNumber] -> 7.
 * TODO 9  : documentEvents(parser, xml)      [startDocument, endDocument] -> [startDocument, endDocument].
 * TODO 10 : prefixMappings(parser, xml)      [startPrefixMapping] -> ["=urn:campus", "g=urn:campus:grades"].
 * TODO 11 : ignorableWhitespaceCalls(p, xml) [ignorableWhitespace] -> 0 (sans DTD, tout va dans characters).
 * TODO 12 : averageGrade(parser, xml)        [characters dans g:grade] -> 12.4.
 * TODO 13 : firstElements(parser, xml, n)    [lancer Stop (deja ecrite)] n = 3 -> [campus, course, title].
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   SAXParserFactory f = SAXParserFactory.newInstance(); f.setNamespaceAware(true);
 *   SAXParser p = f.newSAXParser(); p.parse(file, handler);
 *   DefaultHandler (tout est vide par defaut, on redefinit ce qui sert) :
 *     setDocumentLocator(Locator)   startDocument()   endDocument()
 *     startPrefixMapping(prefix, uri)   endPrefixMapping(prefix)
 *     startElement(uri, localName, qName, Attributes)   endElement(uri, localName, qName)
 *     characters(char[] ch, int start, int length)   ignorableWhitespace(...)
 *     processingInstruction(target, data)   warning/error/fatalError(SAXParseException)
 *   Attributes : getLength()  getLocalName(i)  getQName(i)  getURI(i)  getValue(i)
 *                getValue(qName)  getValue(uri, localName)  getIndex(qName)
 *   Locator : getLineNumber()  getColumnNumber()  getSystemId()
 *   Arreter : throw new UneSousClasseDeSAXException() puis catch autour de parse(...)
 * ---------------------------------------------------------------------
 */
public class Drill04_SaxApi {

    public static final String GRADES = "urn:campus:grades";

    public static SAXParser newNamespaceAwareParser() throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer newNamespaceAwareParser()");
    }

    public static int countElements(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 2 : implementer countElements()");
    }

    public static String firstGradeNames(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 3 : implementer firstGradeNames()");
    }

    public static List<String> courseIds(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer courseIds()");
    }

    public static int rootAttributeCount(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer rootAttributeCount()");
    }

    public static List<String> attributeNames(SAXParser parser, Path xml, String courseId) throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer attributeNames()");
    }

    public static List<String> personNames(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer personNames()");
    }

    public static int lineOfFirstGrade(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 8 : implementer lineOfFirstGrade()");
    }

    public static List<String> documentEvents(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 9 : implementer documentEvents()");
    }

    public static List<String> prefixMappings(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 10 : implementer prefixMappings()");
    }

    public static int ignorableWhitespaceCalls(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 11 : implementer ignorableWhitespaceCalls()");
    }

    public static double averageGrade(SAXParser parser, Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 12 : implementer averageGrade()");
    }

    /** Deja ecrite : exception de controle pour interrompre le parseur. */
    public static class Stop extends SAXException {
        public Stop() {
            super("stop");
        }
    }

    public static List<String> firstElements(SAXParser parser, Path xml, int n) throws Exception {
        throw new UnsupportedOperationException("TODO 13 : implementer firstElements()");
    }

    public static void main(String[] args) throws Exception {
        SAXParser p = newNamespaceAwareParser();
        Path xml = Campus.file();
        ExerciseChecker.check("TODO 1 : parseur namespace-aware", p.isNamespaceAware());
        ExerciseChecker.check("TODO 2 : countElements == 25", countElements(p, xml) == 25);
        ExerciseChecker.check("TODO 3 : firstGradeNames == g:grade|grade|urn:campus:grades",
                firstGradeNames(p, xml).equals("g:grade|grade|urn:campus:grades"));
        ExerciseChecker.check("TODO 4 : courseIds == [C1, C2, C3]", courseIds(p, xml).equals(List.of("C1", "C2", "C3")));
        ExerciseChecker.check("TODO 5 : rootAttributeCount == 2", rootAttributeCount(p, xml) == 2);
        ExerciseChecker.check("TODO 6 : attributeNames(C3) == [id, credits, level, status]",
                attributeNames(p, xml, "C3").equals(List.of("id", "credits", "level", "status")));
        ExerciseChecker.check("TODO 7 : personNames == [Ana, Ben, Chloe, Dan]", personNames(p, xml).equals(List.of("Ana", "Ben", "Chloe", "Dan")));
        ExerciseChecker.check("TODO 8 : lineOfFirstGrade == 7", lineOfFirstGrade(p, xml) == 7);
        ExerciseChecker.check("TODO 9 : documentEvents == [startDocument, endDocument]",
                documentEvents(p, xml).equals(List.of("startDocument", "endDocument")));
        ExerciseChecker.check("TODO 10 : prefixMappings == [=urn:campus, g=urn:campus:grades]",
                prefixMappings(p, xml).equals(List.of("=urn:campus", "g=urn:campus:grades")));
        ExerciseChecker.check("TODO 11 : ignorableWhitespaceCalls == 0", ignorableWhitespaceCalls(p, xml) == 0);
        ExerciseChecker.check("TODO 12 : averageGrade == 12.4", Math.abs(averageGrade(p, xml) - 12.4) < 1e-9);
        ExerciseChecker.check("TODO 13 : firstElements(3) == [campus, course, title]",
                firstElements(p, xml, 3).equals(List.of("campus", "course", "title")));

        ExerciseChecker.summary();
    }
}
