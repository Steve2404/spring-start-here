package vorkurs02_xml.exercises;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.SAXParser;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EXERCICE 14 - SAX en flux : agreger des releves de capteurs et s'arreter au bon moment (niveau : avance / entretien)
 * =================================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex14/readings.xml : 103 releves de temperature (namespace
 * urn:sensors, prefixe m:), dont un en Fahrenheit, des notes avec
 * references et CDATA, et 2 alertes. Dans la vraie vie ce fichier ferait
 * 2 Go : on le lit en SAX, sans jamais construire d'arbre (0.2.18).
 *
 * SAX est un narrateur qui RACONTE le document sans s'arreter :
 * "debut de reading... des caracteres... fin de temp...". Il ne se
 * souvient de rien : c'est TON handler qui garde le contexte.
 *
 * Faits verifies sur ce JDK :
 *   - avec setNamespaceAware(true) : uri = "urn:sensors", localName =
 *     "reading", qName = "m:reading" ;
 *   - <m:note>Tom &amp; Jerry</m:note> arrive en 3 appels a characters() :
 *     "Tom ", "&", " Jerry" (un element != un appel, 0.2.18 S4) ;
 *   - le CDATA "capteur <nettoye>" arrive comme du texte normal ;
 *   - 213 elements au total ; la 1re alerte est complete apres 11 elements.
 *
 *
 * ==================================================================
 * TODO 1 : newParser()
 * ==================================================================
 *
 * SAXParserFactory namespace-aware, FEATURE_SECURE_PROCESSING active,
 * DOCTYPE interdit (feature "http://apache.org/xml/features/disallow-doctype-decl"),
 * puis sur le SAXParser : ACCESS_EXTERNAL_DTD = "". main() verifie que
 * with-doctype.xml est refuse (SAXParseException).
 *
 *
 * ==================================================================
 * TODO 2 : ReadingsHandler.startElement(uri, localName, qName, attributes)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Quand le narrateur dit "une nouvelle boite commence" :
 *   - tu comptes la profondeur (et retiens la plus grande dans maxDepth) ;
 *   - tu VIDES ton carnet de texte (sinon le texte d'avant s'y colle) et
 *     remets le compteur de morceaux a 0 ;
 *   - si c'est {urn:sensors}reading : tu retiens le capteur (attribut
 *     sensor) et remets la note a "" ;
 *   - si c'est {urn:sensors}temp : tu retiens l'unite (attribut unit).
 * Compare uri ET localName, jamais qName (le prefixe peut changer).
 *
 *
 * ==================================================================
 * TODO 3 : ReadingsHandler.characters(ch, start, length)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le narrateur peut te dicter un mot en plusieurs bouts. AJOUTE le bout
 * au carnet (jamais remplacer) et compte les bouts (chunks).
 *
 *
 * ==================================================================
 * TODO 4 : ReadingsHandler.endElement(uri, localName, qName)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "La boite se ferme" : maintenant seulement, son texte est complet.
 * Profondeur - 1, puis (namespace urn:sensors seulement) :
 *   temp    -> la valeur (strip) ; si unit == "F" : (F - 32) * 5 / 9 ;
 *   note    -> la note = le texte ; si c'est la 1re note du fichier
 *              (aucun releve encore enregistre), retiens son nombre de
 *              morceaux dans firstNoteChunks ;
 *   reading -> ajoute Reading(capteur, celsius arrondi a 0.1, note) ;
 *   msg     -> ajoute le texte (strip) aux alertes.
 *
 * -- Essayons a la main --
 *
 *   s2 : 70.7 F -> (70.7 - 32) * 5 / 9 = 21.5 C
 *
 *
 * ==================================================================
 * TODO 5 : parseReadings(xml)
 * ==================================================================
 *
 * Un nouveau ReadingsHandler, newParser().parse(fichier, handler), et on
 * rend le handler (ses champs publics sont le resultat).
 *
 *
 * ==================================================================
 * TODO 6 : averageBySensor(readings)
 * ==================================================================
 *
 * Moyenne des celsius par capteur, arrondie a 0.01, Map triee par capteur.
 *   -> {s1=22.5, s2=21.5, s3=14.5}
 *
 *
 * ==================================================================
 * TODO 7 : firstAlert(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * On veut juste la 1re alerte. Le narrateur SAX ne s'arrete jamais de
 * lui-meme : pour l'interrompre, ton handler LANCE une exception a toi
 * (StopParsingException, deja ecrite) des la fin du 1er {urn:sensors}msg,
 * et tu l'attrapes autour de parse(). Compte aussi les elements
 * commences, pour PROUVER qu'on n'a pas tout lu.
 *
 *   -> FirstAlert(Optional["Surchauffe salle B"], 11)   (11 elements sur 213)
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Un DefaultHandler anonyme suffit. Les variables modifiees depuis la
 * classe anonyme doivent etre "effectivement finales" : utilise des
 * tableaux a une case (int[1], String[1]) ou un StringBuilder.
 *
 *
 * Exemple a verifier : voir main().
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - factory.setNamespaceAware(true); factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
 *   - parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
 *   - text.setLength(0) ; text.append(ch, start, length)
 *   - attributes.getValue("sensor")
 *   - Math.round(x * 10) / 10.0
 *   - Collectors.groupingBy(Reading::sensor, TreeMap::new,
 *         Collectors.collectingAndThen(Collectors.averagingDouble(Reading::celsius), a -> ...))
 *   - try { parser.parse(f, h); } catch (StopParsingException expected) { }
 */
public class Exercise14_SaxStreaming {

    public static final String NS = "urn:sensors";

    public record Reading(String sensor, double celsius, String note) {
    }

    public record FirstAlert(Optional<String> message, int elementsSeen) {
    }

    public static SAXParser newParser() throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer newParser()");
    }

    public static class ReadingsHandler extends DefaultHandler {
        public final List<Reading> readings = new ArrayList<>();
        public final List<String> alerts = new ArrayList<>();
        public int maxDepth;
        public int firstNoteChunks;

        // Etat interne a ta disposition (tu peux en ajouter) :
        private final StringBuilder text = new StringBuilder();
        private int depth;
        private int chunks;
        private String sensor;
        private String unit;
        private double celsius;
        private String note;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            throw new UnsupportedOperationException("TODO 2 : implementer startElement()");
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            throw new UnsupportedOperationException("TODO 3 : implementer characters()");
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            throw new UnsupportedOperationException("TODO 4 : implementer endElement()");
        }
    }

    public static ReadingsHandler parseReadings(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer parseReadings()");
    }

    public static Map<String, Double> averageBySensor(List<Reading> readings) {
        throw new UnsupportedOperationException("TODO 6 : implementer averageBySensor()");
    }

    /** Deja ecrite : exception "de controle", sert seulement a arreter le parseur. */
    public static class StopParsingException extends SAXException {
        public StopParsingException() {
            super("arret volontaire");
        }
    }

    public static FirstAlert firstAlert(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer firstAlert()");
    }

    public static void main(String[] args) throws Exception {
        SAXParser parser = newParser();
        ExerciseChecker.check("newParser : namespace-aware", parser.isNamespaceAware());
        ExerciseChecker.check("newParser : with-doctype.xml refuse (DOCTYPE interdit)",
                throwsParseException(parser, Fixtures.path("ex14/with-doctype.xml")));

        ReadingsHandler manual = new ReadingsHandler();
        AttributesImpl sensorAttr = new AttributesImpl();
        sensorAttr.addAttribute("", "sensor", "sensor", "CDATA", "s9");
        AttributesImpl unitAttr = new AttributesImpl();
        unitAttr.addAttribute("", "unit", "unit", "CDATA", "C");
        manual.startElement(NS, "reading", "m:reading", sensorAttr);
        manual.startElement(NS, "temp", "m:temp", unitAttr);
        manual.characters("21.".toCharArray(), 0, 3);
        manual.characters("xx5yy".toCharArray(), 2, 1);
        manual.endElement(NS, "temp", "m:temp");
        manual.endElement(NS, "reading", "m:reading");
        ExerciseChecker.check("handler a la main : '21.' + '5' en 2 morceaux -> Reading(s9, 21.5, \"\")",
                manual.readings.equals(List.of(new Reading("s9", 21.5, ""))));
        ExerciseChecker.check("handler a la main : maxDepth == 2", manual.maxDepth == 2);

        ReadingsHandler h = parseReadings(Fixtures.path("ex14/readings.xml"));
        ExerciseChecker.check("103 releves", h.readings.size() == 103);
        ExerciseChecker.check("1er releve == Reading(s1, 21.5, Tom & Jerry)", h.readings.get(0).equals(new Reading("s1", 21.5, "Tom & Jerry")));
        ExerciseChecker.check("2e releve : 70.7 F -> 21.5 C, note vide", h.readings.get(1).equals(new Reading("s2", 21.5, "")));
        ExerciseChecker.check("3e releve : note CDATA 'capteur <nettoye>'", h.readings.get(2).note().equals("capteur <nettoye>"));
        ExerciseChecker.check("la 1re note est arrivee en 3 morceaux", h.firstNoteChunks == 3);
        ExerciseChecker.check("alertes == [Surchauffe salle B, Batterie faible]",
                h.alerts.equals(List.of("Surchauffe salle B", "Batterie faible")));
        ExerciseChecker.check("maxDepth == 3 (readings > reading > temp)", h.maxDepth == 3);

        ExerciseChecker.check("averageBySensor == {s1=22.5, s2=21.5, s3=14.5}",
                averageBySensor(h.readings).equals(Map.of("s1", 22.5, "s2", 21.5, "s3", 14.5))
                        && averageBySensor(h.readings).keySet().iterator().next().equals("s1"));

        FirstAlert first = firstAlert(Fixtures.path("ex14/readings.xml"));
        ExerciseChecker.check("firstAlert == Surchauffe salle B", first.message().equals(Optional.of("Surchauffe salle B")));
        ExerciseChecker.check("firstAlert : arrete apres 11 elements (sur 213)", first.elementsSeen() == 11);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static boolean throwsParseException(SAXParser parser, Path xml) {
        try {
            parser.parse(xml.toFile(), new DefaultHandler());
            return false;
        } catch (SAXParseException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
