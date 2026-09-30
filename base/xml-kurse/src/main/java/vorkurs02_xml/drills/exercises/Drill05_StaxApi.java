package vorkurs02_xml.drills.exercises;

import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * DRILL 5 - L'API StAX (XMLStreamReader, XMLEventReader, XMLStreamWriter)
 * =====================================================================
 *
 * Mode d'emploi : voir Drill01_DomApi. Donnees : Campus.text() (le fichier
 * en String). Chaque TODO cree SON reader avec reader(xml) (TODO 1).
 *
 *
 * -- Les TODO (methode visee entre crochets) --
 *
 * TODO 1  : reader(xml)                  [XMLInputFactory.newFactory + createXMLStreamReader(Reader)]
 * TODO 2  : initialEventType(xml)        [getEventType] -> START_DOCUMENT, sans next().
 * TODO 3  : countStartElements(xml)      [hasNext + next] -> 25.
 * TODO 4  : rootLocalName(xml)           [nextTag + getLocalName] -> campus (le commentaire est saute).
 * TODO 5  : rootNamespace(xml)           [getNamespaceURI] -> urn:campus.
 * TODO 6  : rootAttribute(xml, name)     [getAttributeValue(null, name)] name -> Campus Lyon.
 * TODO 7  : rootAttributeCount(xml)      [getAttributeCount] -> 2 (les xmlns n'en sont pas).
 * TODO 8  : rootNamespaceDeclarations(x) [getNamespaceCount, getNamespacePrefix(i) (null = defaut),
 *                                         getNamespaceURI(i)] -> ["=urn:campus", "g=urn:campus:grades"].
 * TODO 9  : firstTitle(xml)              [getElementText] -> Algorithmique.
 * TODO 10 : firstGradePrefix(xml)        [getPrefix] -> g.
 * TODO 11 : whitespaceTextEvents(xml)    [isWhiteSpace] -> 24 (sur 39 evenements CHARACTERS).
 * TODO 12 : lineOf(xml, localName)       [getLocation().getLineNumber()] people -> 21.
 * TODO 13 : startsWithDocument(xml)      [XMLEventReader.peek] le 1er evenement est START_DOCUMENT,
 *                                         peek() ne le consomme pas, nextEvent() rend le meme objet.
 * TODO 14 : courseAttributeNames(xml, id)[StartElement.getAttributes()] C3 -> [credits, id, level, status] (tries).
 * TODO 15 : writeNote(text)              [XMLStreamWriter] -> <?xml version="1.0" ?><note lang="fr">...</note>
 *                                         (le writer echappe lui-meme ; "a < b & \"c\"" -> a &lt; b &amp; "c").
 *
 *
 * ---------------------------------------------------------------------
 * CARTE MEMOIRE (a ne lire qu'en cas de blocage, puis a cacher) :
 *
 *   XMLInputFactory f = XMLInputFactory.newFactory();
 *   XMLStreamReader r = f.createXMLStreamReader(reader | inputStream);
 *     hasNext()  next() -> int  nextTag()  getEventType()  getElementText()
 *     getLocalName()  getNamespaceURI()  getPrefix()  getName() (QName)  getText()
 *     getAttributeCount()  getAttributeLocalName(i)  getAttributeValue(i)
 *     getAttributeValue(nsOuNull, local)  getNamespaceCount()  getNamespacePrefix(i)
 *     getNamespaceURI(i)  isStartElement()  isEndElement()  isCharacters()  isWhiteSpace()
 *     getLocation().getLineNumber()  close()
 *   XMLStreamConstants : START_DOCUMENT  START_ELEMENT  END_ELEMENT  CHARACTERS
 *                        COMMENT  PROCESSING_INSTRUCTION  END_DOCUMENT
 *   XMLEventReader e = f.createXMLEventReader(...) : hasNext() nextEvent() peek() nextTag()
 *     XMLEvent : isStartElement() asStartElement() isCharacters() asCharacters() isStartDocument()
 *     StartElement : getName() getAttributeByName(QName) getAttributes() (Iterator<Attribute>)
 *   XMLStreamWriter w = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
 *     writeStartDocument() writeStartElement(n) writeAttribute(n, v) writeCharacters(t)
 *     writeEmptyElement(n) writeEndElement() writeEndDocument() close()
 * ---------------------------------------------------------------------
 */
public class Drill05_StaxApi {

    public static XMLStreamReader reader(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 1 : implementer reader()");
    }

    public static int initialEventType(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 2 : implementer initialEventType()");
    }

    public static int countStartElements(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 3 : implementer countStartElements()");
    }

    public static String rootLocalName(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 4 : implementer rootLocalName()");
    }

    public static String rootNamespace(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 5 : implementer rootNamespace()");
    }

    public static String rootAttribute(String xml, String name) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 6 : implementer rootAttribute()");
    }

    public static int rootAttributeCount(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 7 : implementer rootAttributeCount()");
    }

    public static List<String> rootNamespaceDeclarations(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 8 : implementer rootNamespaceDeclarations()");
    }

    public static String firstTitle(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 9 : implementer firstTitle()");
    }

    public static String firstGradePrefix(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 10 : implementer firstGradePrefix()");
    }

    public static int whitespaceTextEvents(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 11 : implementer whitespaceTextEvents()");
    }

    public static int lineOf(String xml, String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 12 : implementer lineOf()");
    }

    public static boolean startsWithDocument(String xml) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 13 : implementer startsWithDocument()");
    }

    public static List<String> courseAttributeNames(String xml, String courseId) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 14 : implementer courseAttributeNames()");
    }

    public static String writeNote(String text) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 15 : implementer writeNote()");
    }

    public static void main(String[] args) throws Exception {
        String xml = Campus.text();
        ExerciseChecker.check("TODO 1 : reader() rend un XMLStreamReader utilisable", reader(xml).hasNext());
        ExerciseChecker.check("TODO 2 : initialEventType == START_DOCUMENT", initialEventType(xml) == XMLStreamConstants.START_DOCUMENT);
        ExerciseChecker.check("TODO 3 : countStartElements == 25", countStartElements(xml) == 25);
        ExerciseChecker.check("TODO 4 : rootLocalName == campus", "campus".equals(rootLocalName(xml)));
        ExerciseChecker.check("TODO 5 : rootNamespace == urn:campus", Campus.NS.equals(rootNamespace(xml)));
        ExerciseChecker.check("TODO 6 : rootAttribute(name) == Campus Lyon", "Campus Lyon".equals(rootAttribute(xml, "name")));
        ExerciseChecker.check("TODO 7 : rootAttributeCount == 2", rootAttributeCount(xml) == 2);
        ExerciseChecker.check("TODO 8 : rootNamespaceDeclarations == [=urn:campus, g=urn:campus:grades]",
                rootNamespaceDeclarations(xml).equals(List.of("=urn:campus", "g=urn:campus:grades")));
        ExerciseChecker.check("TODO 9 : firstTitle == Algorithmique", "Algorithmique".equals(firstTitle(xml)));
        ExerciseChecker.check("TODO 10 : firstGradePrefix == g", "g".equals(firstGradePrefix(xml)));
        ExerciseChecker.check("TODO 11 : whitespaceTextEvents == 24", whitespaceTextEvents(xml) == 24);
        ExerciseChecker.check("TODO 12 : lineOf(people) == 21", lineOf(xml, "people") == 21);
        ExerciseChecker.check("TODO 13 : startsWithDocument == true", startsWithDocument(xml));
        ExerciseChecker.check("TODO 14 : courseAttributeNames(C3) == [credits, id, level, status]",
                courseAttributeNames(xml, "C3").equals(List.of("credits", "id", "level", "status")));
        ExerciseChecker.check("TODO 15 : writeNote echappe < et &",
                writeNote("a < b & \"c\"").equals("<?xml version=\"1.0\" ?><note lang=\"fr\">a &lt; b &amp; \"c\"</note>"));

        ExerciseChecker.summary();
    }
}
