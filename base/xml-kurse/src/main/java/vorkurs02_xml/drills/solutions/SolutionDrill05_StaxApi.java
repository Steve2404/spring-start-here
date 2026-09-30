package vorkurs02_xml.drills.solutions;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Corrige du drill 5. A ne consulter qu'apres avoir essaye par vous-meme
 * dans vorkurs02_xml.drills.exercises.Drill05_StaxApi.
 */
public class SolutionDrill05_StaxApi {

    public static XMLStreamReader reader(String xml) throws XMLStreamException {
        return XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml));
    }

    public static int initialEventType(String xml) throws XMLStreamException {
        // Avant tout next(), le curseur est DEJA sur START_DOCUMENT.
        return reader(xml).getEventType();
    }

    public static int countStartElements(String xml) throws XMLStreamException {
        XMLStreamReader r = reader(xml);
        int count = 0;
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.START_ELEMENT) {
                count++;
            }
        }
        return count;
    }

    public static String rootLocalName(String xml) throws XMLStreamException {
        // nextTag() saute le commentaire et les blancs du prologue jusqu'au 1er START_ELEMENT.
        XMLStreamReader r = reader(xml);
        r.nextTag();
        return r.getLocalName();
    }

    public static String rootNamespace(String xml) throws XMLStreamException {
        XMLStreamReader r = reader(xml);
        r.nextTag();
        return r.getNamespaceURI();
    }

    public static String rootAttribute(String xml, String name) throws XMLStreamException {
        // null comme namespace = attribut sans namespace (le cas de presque tous les attributs).
        XMLStreamReader r = reader(xml);
        r.nextTag();
        return r.getAttributeValue(null, name);
    }

    public static int rootAttributeCount(String xml) throws XMLStreamException {
        // Les xmlns ne comptent pas ici : ce sont des "namespaces", avec leur propre API.
        XMLStreamReader r = reader(xml);
        r.nextTag();
        return r.getAttributeCount();
    }

    public static List<String> rootNamespaceDeclarations(String xml) throws XMLStreamException {
        // getNamespacePrefix(i) vaut null pour le namespace par defaut.
        XMLStreamReader r = reader(xml);
        r.nextTag();
        List<String> out = new ArrayList<>();
        for (int i = 0; i < r.getNamespaceCount(); i++) {
            String prefix = r.getNamespacePrefix(i);
            out.add((prefix == null ? "" : prefix) + "=" + r.getNamespaceURI(i));
        }
        return out;
    }

    public static String firstTitle(String xml) throws XMLStreamException {
        XMLStreamReader r = reader(xml);
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("title")) {
                return r.getElementText();
            }
        }
        return null;
    }

    public static String firstGradePrefix(String xml) throws XMLStreamException {
        XMLStreamReader r = reader(xml);
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("grade")) {
                return r.getPrefix();
            }
        }
        return null;
    }

    public static int whitespaceTextEvents(String xml) throws XMLStreamException {
        // isWhiteSpace() : l'evenement CHARACTERS courant ne contient-il que des blancs ?
        XMLStreamReader r = reader(xml);
        int count = 0;
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.CHARACTERS && r.isWhiteSpace()) {
                count++;
            }
        }
        return count;
    }

    public static int lineOf(String xml, String localName) throws XMLStreamException {
        XMLStreamReader r = reader(xml);
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals(localName)) {
                return r.getLocation().getLineNumber();
            }
        }
        return -1;
    }

    public static boolean startsWithDocument(String xml) throws XMLStreamException {
        // peek() regarde le prochain evenement SANS le consommer : deux peek() rendent le meme.
        XMLEventReader events = XMLInputFactory.newFactory().createXMLEventReader(new StringReader(xml));
        XMLEvent first = events.peek();
        return first.isStartDocument() && events.peek() == first && events.nextEvent() == first;
    }

    public static List<String> courseAttributeNames(String xml, String courseId) throws XMLStreamException {
        // StartElement.getAttributes() rend un Iterator d'Attribute ; on trie pour un ordre stable.
        XMLEventReader events = XMLInputFactory.newFactory().createXMLEventReader(new StringReader(xml));
        while (events.hasNext()) {
            XMLEvent e = events.nextEvent();
            if (e.isStartElement()) {
                StartElement s = e.asStartElement();
                Attribute id = s.getAttributeByName(new javax.xml.namespace.QName("id"));
                if (s.getName().getLocalPart().equals("course") && id != null && id.getValue().equals(courseId)) {
                    List<String> names = new ArrayList<>();
                    for (Iterator<Attribute> it = s.getAttributes(); it.hasNext(); ) {
                        names.add(it.next().getName().getLocalPart());
                    }
                    names.sort(null);
                    return names;
                }
            }
        }
        return List.of();
    }

    public static String writeNote(String text) throws XMLStreamException {
        // Le writer ECHAPPE lui-meme < et & dans writeCharacters : on lui donne le texte brut.
        StringWriter out = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        w.writeStartDocument();
        w.writeStartElement("note");
        w.writeAttribute("lang", "fr");
        w.writeCharacters(text);
        w.writeEndElement();
        w.writeEndDocument();
        w.close();
        return out.toString();
    }
}
