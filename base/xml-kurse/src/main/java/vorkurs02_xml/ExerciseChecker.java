package vorkurs02_xml;

/**
 * Petit helper d'auto-verification pour les exercices, sans dependance
 * a un framework de test. Chaque exercice appelle check(...) dans son
 * main() et affiche PASS/FAIL avec le libelle donne.
 */
public final class ExerciseChecker {

    private static int total = 0;
    private static int passed = 0;

    private ExerciseChecker() {
    }

    public static void check(String label, boolean condition) {
        total++;
        if (condition) {
            passed++;
            System.out.println("[PASS] " + label);
        } else {
            System.out.println("[FAIL] " + label);
        }
    }

    public static void summary() {
        System.out.println("\n--- Resultat : " + passed + "/" + total + " tests passes ---\n");
        total = 0;
        passed = 0;
    }
}
