package vorkurs02_xml.exercises;

import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * EXERCICE 15 - StAX : piloter le curseur soi-meme (nextTag, getElementText, sauter, s'arreter) (niveau : avance / entretien)
 * =======================================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex15/manifest.xml : un manifeste logistique (namespace par
 * defaut urn:logistics), 3 envois, des commentaires au milieu, un bloc
 * <audit> qui ne nous interesse pas (et qui contient du texte MIXTE),
 * des poids en kg ou en g, et un envoi vide <shipment .../>.
 *
 * Avec SAX, le parseur pousse et tu subis. Avec StAX (0.2.19), c'est TOI
 * qui tires : "suivant !". Tu decides d'avancer, de sauter un bloc, ou
 * d'arreter. En echange, tu dois savoir A CHAQUE INSTANT ou est le
 * curseur (quel evenement est "courant").
 *
 * Faits verifies sur ce JDK :
 *   - a la creation, le reader est sur START_DOCUMENT ; next() rend le
 *     nouvel etat (START_DOCUMENT n'est jamais rendu par next()) ;
 *   - "<a><!--c-->x<b/></a>" donne : START:a, COMMENT, TEXT, START:b,
 *     END:b, END:a, END_DOCUMENT (<b/> produit bien START puis END) ;
 *   - nextTag() saute les blancs ET les commentaires ; il LEVE une
 *     XMLStreamException s'il rencontre du vrai texte ;
 *   - getElementText() sur <entry>controle <b>ok</b></entry> leve une
 *     XMLStreamException (element non textuel) : on doit SAUTER <audit>.
 *   Poids : S1 = 2 x 1.5 + 1 x 0.250 = 3.25 kg ; S2 = 10 x 0.8 = 8.0 kg ; S3 = 0.
 *
 *
 * ==================================================================
 * TODO 1 : newReader(input)
 * ==================================================================
 *
 * XMLInputFactory securisee (0.2.21 S4) : SUPPORT_DTD = false,
 * IS_SUPPORTING_EXTERNAL_ENTITIES = false, ACCESS_EXTERNAL_DTD = "" ;
 * puis createXMLStreamReader(input).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "la factory securisee" resservira pour l'API evenements (TODO 7).
 *
 *
 * ==================================================================
 * TODO 2 : eventNames(reader)
 * ==================================================================
 *
 * Tant que hasNext() : next(), et note "START:nom", "END:nom", "TEXT"
 * (CHARACTERS), "COMMENT", "END_DOCUMENT", sinon "OTHER". Attention :
 * getLocalName() n'est permis que sur START/END_ELEMENT.
 *
 *
 * ==================================================================
 * TODO 3 : skipElement(reader)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le curseur est sur un START_ELEMENT dont on se moque. Avance jusqu'au
 * END_ELEMENT QUI LE FERME, meme s'il contient des enfants de meme nom.
 * Compte les etages : +1 a chaque START, -1 a chaque END ; stop a 0.
 *
 *
 * ==================================================================
 * TODO 4 : readShipment(reader)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le curseur est sur <shipment>. Lis id et destination, puis, tant que
 * nextTag() rend un START_ELEMENT (un <item>) : son sku, puis ses
 * enfants (tant que nextTag() rend START) : <qty> -> getElementText ;
 * <weight> -> unite + getElementText (g -> kg : / 1000). A la sortie, le
 * curseur est sur </shipment>.
 *
 * -- Essayons a la main --
 *
 *   S2 : <item sku="C3"> <qty>10</qty> <!-- poids unitaire --> <weight unit="kg">0.8</weight> </item>
 *     nextTag saute blancs ET commentaire -> Item(C3, 10, 0.8)
 *   S3 : <shipment .../> -> le 1er nextTag() rend deja END_ELEMENT -> 0 item
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non, deux boucles "while (reader.nextTag() == START_ELEMENT)" imbriquees.
 *
 *
 * ==================================================================
 * TODO 5 : readAll(xml)
 * ==================================================================
 *
 * Ouvre le fichier (try-with-resources), un reader, nextTag() une fois
 * pour entrer dans <manifest>, puis pour chaque START de 1er niveau :
 * {urn:logistics}shipment -> readShipment ; autre (audit) -> skipElement.
 * Ferme le reader (reader.close() ne ferme PAS l'InputStream).
 *
 *   -> [S1 (2 items, Lyon), S2 (1 item, Paris), S3 (0 item, Nice)]
 *
 *
 * ==================================================================
 * TODO 6 : firstHeavierThan(xml, limitKg)
 * ==================================================================
 *
 * Le 1er envoi dont le poids total depasse limitKg (Shipment.totalKg(),
 * deja ecrit). Des qu'on l'a : return (le finally ferme le reader). Aucun
 * -> vide.   5.0 -> S2 ; 3.0 -> S1 ; 100 -> vide
 *
 *
 * ==================================================================
 * TODO 7 : destinations(xml)
 * ==================================================================
 *
 * Meme fichier, mais avec l'API EVENEMENTS (XMLEventReader, 0.2.19 S6) :
 * chaque evenement est un objet. Pour chaque StartElement dont le nom est
 * QName(urn:logistics, shipment), prends getAttributeByName(new QName(
 * "destination")) (attribut sans namespace).  -> [Lyon, Paris, Nice]
 *
 *
 * Exemple a verifier : voir main().
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
 *   - int ev = reader.next(); ev == XMLStreamConstants.START_ELEMENT
 *   - reader.getAttributeValue(null, "id") ; reader.getElementText() ; reader.nextTag()
 *   - reader.getNamespaceURI(), reader.getLocalName()
 *   - XMLEventReader events = factory.createXMLEventReader(in); XMLEvent e = events.nextEvent();
 *     e.isStartElement() ; e.asStartElement().getName() ; getAttributeByName(new QName("destination")).getValue()
 */
public class Exercise15_StaxPullParsing {

    public static final String NS = "urn:logistics";

    public record Item(String sku, int qty, double weightKg) {
    }

    public record Shipment(String id, String destination, List<Item> items) {
        public double totalKg() {
            return items.stream().mapToDouble(i -> i.qty() * i.weightKg()).sum();
        }
    }

    public static XMLStreamReader newReader(InputStream input) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 1 : implementer newReader()");
    }

    public static List<String> eventNames(XMLStreamReader reader) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 2 : implementer eventNames()");
    }

    public static void skipElement(XMLStreamReader reader) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 3 : implementer skipElement()");
    }

    public static Shipment readShipment(XMLStreamReader reader) throws XMLStreamException {
        throw new UnsupportedOperationException("TODO 4 : implementer readShipment()");
    }

    public static List<Shipment> readAll(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer readAll()");
    }

    public static Optional<String> firstHeavierThan(Path xml, double limitKg) throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer firstHeavierThan()");
    }

    public static List<String> destinations(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer destinations()");
    }

    public static void main(String[] args) throws Exception {
        XMLStreamReader r = newReader(stream("<a><!--c-->x<b/></a>"));
        ExerciseChecker.check("newReader : positionne sur START_DOCUMENT", r.getEventType() == XMLStreamConstants.START_DOCUMENT);
        ExerciseChecker.check("newReader : SUPPORT_DTD == false", Boolean.FALSE.equals(r.getProperty(XMLInputFactory.SUPPORT_DTD)));
        ExerciseChecker.check("eventNames(<a><!--c-->x<b/></a>)", eventNames(r).equals(
                List.of("START:a", "COMMENT", "TEXT", "START:b", "END:b", "END:a", "END_DOCUMENT")));

        XMLStreamReader s = newReader(stream("<r><skip><skip><x/></skip>t</skip><next/></r>"));
        s.nextTag();
        s.nextTag();
        skipElement(s);
        ExerciseChecker.check("skipElement : curseur sur le END du 1er <skip> (pas l'interieur)",
                s.getEventType() == XMLStreamConstants.END_ELEMENT && s.getLocalName().equals("skip")
                        && s.nextTag() == XMLStreamConstants.START_ELEMENT && s.getLocalName().equals("next"));

        XMLStreamReader one = newReader(stream("<shipment xmlns='urn:logistics' id='X' destination='Metz'>"
                + "<item sku='K'><qty>3</qty><!-- c --><weight unit='g'>500</weight></item></shipment>"));
        one.nextTag();
        Shipment x = readShipment(one);
        ExerciseChecker.check("readShipment : Shipment(X, Metz, [Item(K, 3, 0.5)])",
                x.equals(new Shipment("X", "Metz", List.of(new Item("K", 3, 0.5)))));
        ExerciseChecker.check("readShipment : curseur laisse sur </shipment>",
                one.getEventType() == XMLStreamConstants.END_ELEMENT && one.getLocalName().equals("shipment"));

        Path manifest = Fixtures.path("ex15/manifest.xml");
        List<Shipment> all = readAll(manifest);
        ExerciseChecker.check("readAll : 3 envois S1, S2, S3", all.stream().map(Shipment::id).toList().equals(List.of("S1", "S2", "S3")));
        ExerciseChecker.check("readAll : S1 = [A1 x2 1.5kg, B7 x1 0.25kg]", all.get(0).items().equals(
                List.of(new Item("A1", 2, 1.5), new Item("B7", 1, 0.25))));
        ExerciseChecker.check("readAll : S2 = [C3 x10 0.8kg] (commentaire saute)", all.get(1).items().equals(List.of(new Item("C3", 10, 0.8))));
        ExerciseChecker.check("readAll : S3 vide, destination Nice", all.get(2).items().isEmpty() && all.get(2).destination().equals("Nice"));
        ExerciseChecker.check("poids : S1 3.25 kg, S2 8.0 kg",
                Math.abs(all.get(0).totalKg() - 3.25) < 1e-9 && Math.abs(all.get(1).totalKg() - 8.0) < 1e-9);

        ExerciseChecker.check("firstHeavierThan(5.0) == S2", firstHeavierThan(manifest, 5.0).equals(Optional.of("S2")));
        ExerciseChecker.check("firstHeavierThan(3.0) == S1", firstHeavierThan(manifest, 3.0).equals(Optional.of("S1")));
        ExerciseChecker.check("firstHeavierThan(100) vide", firstHeavierThan(manifest, 100).isEmpty());

        ExerciseChecker.check("destinations (API evenements) == [Lyon, Paris, Nice]",
                destinations(manifest).equals(List.of("Lyon", "Paris", "Nice")));

        ExerciseChecker.check("le piege : getElementText() sur un element MIXTE leve XMLStreamException", mixedTextThrows());

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static InputStream stream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean mixedTextThrows() throws XMLStreamException {
        XMLStreamReader r = newReader(stream("<entry>controle <b>ok</b></entry>"));
        r.nextTag();
        try {
            r.getElementText();
            return false;
        } catch (XMLStreamException expected) {
            return true;
        }
    }
}
