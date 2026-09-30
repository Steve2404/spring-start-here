package vorkurs02_xml.drills;

import org.w3c.dom.Document;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

/**
 * Les donnees partagees par TOUS les drills : fixtures/drills/campus.xml
 * (lis-le une fois et garde-le ouvert a cote).
 *
 *   <campus xmlns="urn:campus" xmlns:g="urn:campus:grades" name="Campus Lyon" year="2026">
 *     3 <course> (C1 Algorithmique 6 ECTS L1, C2 Bases de donnees 3 ECTS L2,
 *                 C3 Reseaux 6 ECTS L2 status="closed", sans etudiant)
 *       chacun : <title>, <teacher>, puis des <student ref="Sx"><g:grade>note</g:grade></student>
 *     <people> : 4 <person id="S1..S4" [email]>Prenom</person> (Ben n'a pas d'email)
 *
 * Notes : C1 -> S1 15.5, S2 9, S3 12 ; C2 -> S1 18, S4 7.5.
 * Tous les elements sont dans urn:campus (namespace par DEFAUT), sauf
 * <g:grade> dans urn:campus:grades. Les attributs n'ont PAS de namespace.
 * Deja ecrit : rien a coder ici.
 */
public final class Campus {

    public static final String NS = "urn:campus";
    public static final String GRADES = "urn:campus:grades";

    private Campus() {
    }

    /** Le chemin du fichier. */
    public static Path file() {
        return Fixtures.path("drills/campus.xml");
    }

    /** Le texte brut du fichier. */
    public static String text() {
        return Fixtures.read("drills/campus.xml");
    }

    /** Un DOM TOUT NEUF (namespace-aware) a chaque appel : tu peux le modifier sans risque. */
    public static Document dom() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(file().toFile());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
