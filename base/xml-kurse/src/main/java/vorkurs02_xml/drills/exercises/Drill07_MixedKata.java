package vorkurs02_xml.drills.exercises;

import org.w3c.dom.Document;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.drills.Campus;

import java.util.List;
import java.util.Map;

/**
 * DRILL 7 - KATA MELANGE : 15 questions metier, SANS indiquer l'outil
 * =================================================================
 *
 * Mode d'emploi : c'est le drill final. Aucune methode n'est indiquee :
 * a toi de CHOISIR (DOM, DOM *NS, XPath, un peu de streams Java). C'est
 * exactement ce qu'on te demandera en vrai. Chrono conseille : 45 min.
 * Chaque methode recoit un Document TOUT NEUF (Campus.dom(),
 * namespace-aware). Rappel : elements dans urn:campus, <g:grade> dans
 * urn:campus:grades, attributs sans namespace.
 *
 *
 * -- Les TODO --
 *
 * TODO 1  : studentsOf(doc, courseId)        noms des etudiants d'un cours, dans l'ordre. C1 -> [Ana, Ben, Chloe].
 * TODO 2  : averageOf(doc, courseId)         moyenne des notes du cours, arrondie a 0.01. C1 -> 12.17 ; C2 -> 12.75.
 * TODO 3  : bestStudent(doc)                 nom de l'auteur de la meilleure note du campus. -> Ana.
 * TODO 4  : failing(doc, seuil)              noms (tries, sans doublon) ayant au moins une note < seuil. 10 -> [Ben, Dan].
 * TODO 5  : emailDomains(doc)                domaines des emails, tries, sans doublon. -> [campus.fr].
 * TODO 6  : creditsOf(doc, personId)         somme des credits des cours suivis. S1 -> 9 ; S2 -> 6.
 * TODO 7  : teachersWithCourseCount(doc)     Map triee enseignant -> nombre de cours. -> {Dupont=2, Martin=1}.
 * TODO 8  : closedCourses(doc)               ids des cours fermes. -> [C3].
 * TODO 9  : coursesWithoutStudents(doc)      ids des cours sans etudiant. -> [C3].
 * TODO 10 : renameTeacher(doc, from, to)     remplace le nom d'un enseignant partout, rend le nombre de changements.
 *                                            Dupont -> Durand : 2.
 * TODO 11 : addStudent(doc, course, person, note) inscrit une personne avec sa note, DANS LES BONS namespaces.
 *                                            (C2, S2, 11) -> studentsOf(C2) == [Ana, Dan, Ben] ; averageOf(C2) == 12.17.
 * TODO 12 : toCsv(doc)                       une ligne "id;titre;nbEtudiants" par cours.
 *                                            -> [C1;Algorithmique;3, C2;Bases de donnees;2, C3;Reseaux;0].
 * TODO 13 : personWithoutEmail(doc)          -> Ben.
 * TODO 14 : levelHistogram(doc)              Map triee niveau -> nombre de cours. -> {L1=1, L2=2}.
 * TODO 15 : transcript(doc, personId)        "Nom: Titre=note; Titre=note" (notes telles qu'ecrites).
 *                                            S1 -> "Ana: Algorithmique=15.5; Bases de donnees=18".
 *
 * Pas de carte memoire : relis celles des drills 1 a 6 si besoin.
 */
public class Drill07_MixedKata {

    public static List<String> studentsOf(Document doc, String courseId) throws Exception {
        throw new UnsupportedOperationException("TODO 1 : implementer studentsOf()");
    }

    public static double averageOf(Document doc, String courseId) throws Exception {
        throw new UnsupportedOperationException("TODO 2 : implementer averageOf()");
    }

    public static String bestStudent(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 3 : implementer bestStudent()");
    }

    public static List<String> failing(Document doc, double threshold) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer failing()");
    }

    public static List<String> emailDomains(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer emailDomains()");
    }

    public static int creditsOf(Document doc, String personId) throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer creditsOf()");
    }

    public static Map<String, Long> teachersWithCourseCount(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 7 : implementer teachersWithCourseCount()");
    }

    public static List<String> closedCourses(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 8 : implementer closedCourses()");
    }

    public static List<String> coursesWithoutStudents(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 9 : implementer coursesWithoutStudents()");
    }

    public static int renameTeacher(Document doc, String from, String to) {
        throw new UnsupportedOperationException("TODO 10 : implementer renameTeacher()");
    }

    public static void addStudent(Document doc, String courseId, String personId, String grade) throws Exception {
        throw new UnsupportedOperationException("TODO 11 : implementer addStudent()");
    }

    public static List<String> toCsv(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 12 : implementer toCsv()");
    }

    public static String personWithoutEmail(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 13 : implementer personWithoutEmail()");
    }

    public static Map<String, Long> levelHistogram(Document doc) throws Exception {
        throw new UnsupportedOperationException("TODO 14 : implementer levelHistogram()");
    }

    public static String transcript(Document doc, String personId) throws Exception {
        throw new UnsupportedOperationException("TODO 15 : implementer transcript()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("TODO 1 : studentsOf(C1) == [Ana, Ben, Chloe]", studentsOf(Campus.dom(), "C1").equals(List.of("Ana", "Ben", "Chloe")));
        ExerciseChecker.check("TODO 2 : averageOf C1 == 12.17, C2 == 12.75",
                averageOf(Campus.dom(), "C1") == 12.17 && averageOf(Campus.dom(), "C2") == 12.75);
        ExerciseChecker.check("TODO 3 : bestStudent == Ana", bestStudent(Campus.dom()).equals("Ana"));
        ExerciseChecker.check("TODO 4 : failing(10) == [Ben, Dan]", failing(Campus.dom(), 10).equals(List.of("Ben", "Dan")));
        ExerciseChecker.check("TODO 5 : emailDomains == [campus.fr]", emailDomains(Campus.dom()).equals(List.of("campus.fr")));
        ExerciseChecker.check("TODO 6 : creditsOf S1 == 9, S2 == 6", creditsOf(Campus.dom(), "S1") == 9 && creditsOf(Campus.dom(), "S2") == 6);
        ExerciseChecker.check("TODO 7 : teachersWithCourseCount == {Dupont=2, Martin=1}",
                teachersWithCourseCount(Campus.dom()).equals(Map.of("Dupont", 2L, "Martin", 1L))
                        && teachersWithCourseCount(Campus.dom()).keySet().iterator().next().equals("Dupont"));
        ExerciseChecker.check("TODO 8 : closedCourses == [C3]", closedCourses(Campus.dom()).equals(List.of("C3")));
        ExerciseChecker.check("TODO 9 : coursesWithoutStudents == [C3]", coursesWithoutStudents(Campus.dom()).equals(List.of("C3")));
        Document renamed = Campus.dom();
        ExerciseChecker.check("TODO 10 : renameTeacher(Dupont -> Durand) == 2, plus de Dupont",
                renameTeacher(renamed, "Dupont", "Durand") == 2 && teachersWithCourseCount(renamed).equals(Map.of("Durand", 2L, "Martin", 1L)));
        Document enrolled = Campus.dom();
        addStudent(enrolled, "C2", "S2", "11");
        ExerciseChecker.check("TODO 11 : addStudent(C2, S2, 11) -> [Ana, Dan, Ben], moyenne 12.17",
                studentsOf(enrolled, "C2").equals(List.of("Ana", "Dan", "Ben")) && averageOf(enrolled, "C2") == 12.17);
        ExerciseChecker.check("TODO 12 : toCsv", toCsv(Campus.dom()).equals(List.of("C1;Algorithmique;3", "C2;Bases de donnees;2", "C3;Reseaux;0")));
        ExerciseChecker.check("TODO 13 : personWithoutEmail == Ben", personWithoutEmail(Campus.dom()).equals("Ben"));
        ExerciseChecker.check("TODO 14 : levelHistogram == {L1=1, L2=2}", levelHistogram(Campus.dom()).equals(Map.of("L1", 1L, "L2", 2L)));
        ExerciseChecker.check("TODO 15 : transcript(S1)", transcript(Campus.dom(), "S1").equals("Ana: Algorithmique=15.5; Bases de donnees=18"));

        ExerciseChecker.summary();
    }
}
