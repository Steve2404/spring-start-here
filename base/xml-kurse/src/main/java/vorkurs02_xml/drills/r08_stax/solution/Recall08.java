package vorkurs02_xml.drills.r08_stax.solution;

import vorkurs02_xml.drills.Data;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Corrige du drill 8 : l'API StAX (0.2.19), sur campus.xml.
 */
public class Recall08 {

    static final XMLInputFactory FACTORY = XMLInputFactory.newFactory();

    static {
        FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    static XMLStreamReader open(InputStream in) throws Exception {
        return FACTORY.createXMLStreamReader(in);
    }

    public static void main(String[] args) throws Exception {
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLStreamReader r = open(in);
            // D01 : le lecteur commence SUR START_DOCUMENT.
            boolean startDoc = r.getEventType() == XMLStreamConstants.START_DOCUMENT;
            r.nextTag();
            System.out.println("D01 : " + startDoc + " " + r.getLocalName());
            // D02 : namespaces declares sur la racine, et un attribut sans namespace.
            System.out.println("D02 : " + r.getNamespaceCount() + " " + r.getNamespaceURI("g") + " " + r.getAttributeValue(null, "year"));
            // D03 : compter les ouvertures (la racine est deja passee : +1).
            int starts = 1;
            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT) {
                    starts++;
                }
            }
            System.out.println("D03 : " + starts);
            r.close();
        }
        List<String> ids = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        int emails = 0;
        int personLine = 0;
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLStreamReader r = open(in);
            while (r.hasNext()) {
                if (r.next() != XMLStreamConstants.START_ELEMENT) {
                    continue;
                }
                switch (r.getLocalName()) {
                    case "course" -> ids.add(r.getAttributeValue(null, "id"));
                    // getElementText lit un element texte et laisse le curseur sur sa fermeture.
                    case "title" -> titles.add(r.getElementText());
                    case "person" -> {
                        if (personLine == 0) {
                            personLine = r.getLocation().getLineNumber();
                        }
                        if (r.getAttributeValue(null, "email") != null) {
                            emails++;
                        }
                    }
                    default -> {
                    }
                }
            }
            r.close();
        }
        System.out.println("D04 : " + ids);
        System.out.println("D05 : " + titles);
        System.out.println("D06 : " + emails);
        System.out.println("D07 : " + personLine);
        // D08 : une methode appelee dans le mauvais etat.
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLStreamReader r = open(in);
            r.nextTag();
            r.next(); // un texte blanc
            String verdict;
            try {
                r.getAttributeValue(null, "id");
                verdict = "aucune";
            } catch (IllegalStateException e) {
                verdict = e.getClass().getSimpleName();
            }
            System.out.println("D08 : " + verdict + " " + r.isWhiteSpace());
            r.close();
        }
        // D09 : l'API evenements : peek sans consommer, puis des objets.
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLEventReader events = FACTORY.createXMLEventReader(in);
            events.nextEvent(); // StartDocument
            XMLEvent peeked = events.peek();
            List<String> grades = new ArrayList<>();
            QName grade = new QName("urn:campus:grades", "grade");
            while (events.hasNext()) {
                XMLEvent e = events.nextEvent();
                if (e.isStartElement() && e.asStartElement().getName().equals(grade)) {
                    grades.add(events.nextEvent().asCharacters().getData());
                }
            }
            events.close();
            System.out.println("D09 : " + (peeked.getEventType() == XMLStreamConstants.COMMENT ? "commentaire" : peeked.isStartElement() ? "racine" : "autre") + " " + grades);
        }
        // D10 : s'arreter des la premiere note sous 10 : c'est notre boucle qui decide.
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLStreamReader r = open(in);
            String course = null;
            String student = null;
            String answer = "aucune";
            while (r.hasNext()) {
                if (r.next() != XMLStreamConstants.START_ELEMENT) {
                    continue;
                }
                if (r.getLocalName().equals("course")) {
                    course = r.getAttributeValue(null, "id");
                } else if (r.getLocalName().equals("student")) {
                    student = r.getAttributeValue(null, "ref");
                } else if (r.getLocalName().equals("grade") && Double.parseDouble(r.getElementText()) < 10) {
                    answer = student + " en " + course;
                    break;
                }
            }
            r.close();
            System.out.println("D10 : " + answer);
        }
    }
}
