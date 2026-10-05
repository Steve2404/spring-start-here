package vorkurs04_annotations.projects.p05_console;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 5 (ne pas modifier). Il execute TON Console.main, compare sa sortie a
 * EXPECTED, puis verifie la pratique de la pipeline de metadonnees et de Method.invoke.
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "REJET BrokenCommands.blank : nom vide",
            "REJET BrokenCommands.dump : type non gere Object",
            "CONFLIT add : BrokenCommands.plus, MathCommands.add",
            "COMMANDES : count() div(int,int) ls() mul(long,long) pct(double,double) ping(boolean) rm(String) [admin] secret() [admin] [prive] sum(int...) touch(String)",
            "guest touch a.txt : OK",
            "guest touch b.txt : OK",
            "guest ls : a.txt,b.txt",
            "guest count : 2",
            "guest rm a.txt : REFUS reserve aux admins",
            "admin rm a.txt : true",
            "admin rm a.txt : false",
            "guest ls : b.txt",
            "guest mul 6 7 : 42",
            "guest add 2 40 : INCONNU",
            "guest sum 1 2 3 4 : 10",
            "guest sum : 0",
            "guest div 7 2 : 3",
            "guest div 7 0 : ERREUR COMMANDE ArithmeticException (/ by zero)",
            "guest pct 1 8 : 12.5",
            "guest ping true : false",
            "guest mul 6 x : ERREUR ARGUMENT",
            "guest touch : ERREUR APPEL",
            "guest dump x : INCONNU",
            "guest secret : REFUS reserve aux admins",
            "admin secret : 42",
            "AIDE ls : liste les fichiers",
            "AIDE mul : (sans description)",
            "AIDE count : (sans description)",
            "AIDE div : (sans description)",
            "AIDE pct : (sans description)",
            "AIDE ping : (sans description)",
            "AIDE rm : supprime un fichier",
            "AIDE secret : (sans description)",
            "AIDE sum : additionne tout",
            "AIDE touch : cree un fichier");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Retention(", "default", "Javac.compile(", "Javac.load(", "Data.HANDLERS", "Data.SCRIPT",
            ".getDeclaredMethods()", ".getDeclaredMethod(", ".isAnnotationPresent(", ".getAnnotation(", ".isBlank()",
            ".getParameterTypes()", ".isVarArgs()", ".getComponentType()", ".getReturnType()", "void.class",
            "Modifier.isStatic(", "Modifier.isPrivate(", ".canAccess(", ".trySetAccessible()",
            ".invoke(null", ".invoke(", "InvocationTargetException", ".getCause()", "IllegalArgumentException",
            "Comparator",
            // trySetAccessible dit "non" sans exception ; setAccessible(true) leverait InaccessibleObjectException.
            "!.setAccessible(",
            // Crescendo : instanciation par constructeur (0.4.12), generiques (0.4.11), processors (0.4.13).
            "!newInstance", "!getGenericType", "!getGenericReturnType", "!getGenericParameterTypes", "!AbstractProcessor");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Console", args, EXPECTED, API);
    }
}
