package vorkurs02_xml.solutions;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Corrige de l'exercice 15. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise15_StaxPullParsing.
 */
public class Solution15_StaxPullParsing {

    public static final String NS = "urn:logistics";

    public record Item(String sku, int qty, double weightKg) {
    }

    public record Shipment(String id, String destination, List<Item> items) {
        public double totalKg() {
            return items.stream().mapToDouble(i -> i.qty() * i.weightKg()).sum();
        }
    }

    private static XMLInputFactory secureFactory() {
        // Pas de DTD, pas d'entites externes, aucun protocole externe : les 3 reglages du cours 0.2.21 S4.
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        return factory;
    }

    public static XMLStreamReader newReader(InputStream input) throws XMLStreamException {
        // La factory est configuree AVANT de creer le reader ; le reader est positionne sur START_DOCUMENT.
        return secureFactory().createXMLStreamReader(input);
    }

    public static List<String> eventNames(XMLStreamReader reader) throws XMLStreamException {
        // next() AVANCE puis rend le nouvel etat : l'etat initial START_DOCUMENT n'est jamais rendu.
        // getLocalName() n'est permis que sur START/END_ELEMENT (sinon IllegalStateException).
        List<String> names = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            String name = switch (event) {
                case XMLStreamConstants.START_ELEMENT -> "START:" + reader.getLocalName();
                case XMLStreamConstants.END_ELEMENT -> "END:" + reader.getLocalName();
                case XMLStreamConstants.CHARACTERS -> "TEXT";
                case XMLStreamConstants.COMMENT -> "COMMENT";
                case XMLStreamConstants.END_DOCUMENT -> "END_DOCUMENT";
                default -> "OTHER";
            };
            names.add(name);
        }
        return names;
    }

    public static void skipElement(XMLStreamReader reader) throws XMLStreamException {
        // On compte la profondeur : +1 a chaque START, -1 a chaque END ; a 0, on est sur le END
        // qui ferme l'element de depart (les enfants de meme nom ne trompent pas le compteur).
        int depth = 1;
        while (depth > 0) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    public static Shipment readShipment(XMLStreamReader reader) throws XMLStreamException {
        // nextTag() saute blancs, commentaires et PI jusqu'au prochain START/END ; getElementText()
        // lit un element TEXTE et laisse le curseur sur son END_ELEMENT.
        String id = reader.getAttributeValue(null, "id");
        String destination = reader.getAttributeValue(null, "destination");
        List<Item> items = new ArrayList<>();
        while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
            String sku = reader.getAttributeValue(null, "sku");
            int qty = 0;
            double kg = 0;
            while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("qty")) {
                    qty = Integer.parseInt(reader.getElementText().strip());
                } else {
                    String unit = reader.getAttributeValue(null, "unit");
                    double value = Double.parseDouble(reader.getElementText().strip());
                    kg = "g".equals(unit) ? value / 1000 : value;
                }
            }
            items.add(new Item(sku, qty, kg));
        }
        return new Shipment(id, destination, items);
    }

    public static List<Shipment> readAll(Path xml) throws Exception {
        // La boucle principale choisit quoi faire a chaque START de 1er niveau : lire un shipment,
        // ou SAUTER un bloc inconnu (audit) sans en lire le contenu.
        List<Shipment> result = new ArrayList<>();
        try (InputStream in = Files.newInputStream(xml)) {
            XMLStreamReader reader = newReader(in);
            try {
                reader.nextTag();
                while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                    if (NS.equals(reader.getNamespaceURI()) && reader.getLocalName().equals("shipment")) {
                        result.add(readShipment(reader));
                    } else {
                        skipElement(reader);
                    }
                }
            } finally {
                reader.close();
            }
        }
        return result;
    }

    public static Optional<String> firstHeavierThan(Path xml, double limitKg) throws Exception {
        // Pull = l'application decide : des qu'on a la reponse, on sort de la boucle et on ferme.
        try (InputStream in = Files.newInputStream(xml)) {
            XMLStreamReader reader = newReader(in);
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("shipment")) {
                        Shipment s = readShipment(reader);
                        if (s.totalKg() > limitKg) {
                            return Optional.of(s.id());
                        }
                    }
                }
                return Optional.empty();
            } finally {
                reader.close();
            }
        }
    }

    public static List<String> destinations(Path xml) throws Exception {
        // L'API evenements rend des OBJETS : on peut les garder, les passer ailleurs, ou peek()
        // le suivant sans le consommer. getAttributeByName attend un QName (ici sans namespace).
        List<String> result = new ArrayList<>();
        try (InputStream in = Files.newInputStream(xml)) {
            XMLEventReader events = secureFactory().createXMLEventReader(in);
            try {
                while (events.hasNext()) {
                    XMLEvent e = events.nextEvent();
                    if (e.isStartElement()) {
                        StartElement start = e.asStartElement();
                        if (start.getName().equals(new QName(NS, "shipment"))) {
                            result.add(start.getAttributeByName(new QName("destination")).getValue());
                        }
                    }
                }
            } finally {
                events.close();
            }
        }
        return result;
    }
}
