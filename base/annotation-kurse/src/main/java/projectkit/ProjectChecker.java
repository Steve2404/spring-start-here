package projectkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Le correcteur des PROJETS et des DRILLS (ne pas modifier).
 *
 * Dans un projet, c'est TOI qui ecris toute la structure (classes, methodes,
 * main...). Le correcteur ne connait donc rien de ton code sauf le nom de la
 * classe qui contient main. Il verifie :
 *
 *   1. le RESULTAT : il lance ton main (avec des arguments si le projet en
 *      prevoit), capture ce qu'il affiche, et compare ligne par ligne avec la
 *      sortie attendue (il montre la 1re difference) ;
 *   2. l'API : il lit tes fichiers .java (sous-paquets compris, sauf solution/)
 *      et verifie que chaque element vise y apparait, et qu'aucun element
 *      interdit (notion d'un chapitre suivant) n'y apparait ;
 *   3. pour certains projets, un SCRIPT de commandes (javac, java, jar...) que
 *      tu ecris toi-meme : il l'execute avec bash et compare sa sortie.
 */
public final class ProjectChecker {

    private ProjectChecker() {
    }

    /**
     * @param mainClass   nom complet de la classe qui contient main
     * @param programArgs les arguments passes a ton main
     * @param sourceDir   le dossier des .java a analyser (sous-dossiers compris, sauf solution/)
     * @param ignored     les fichiers donnes, a ne pas analyser (Data.java, Check.java...)
     * @param expected    la sortie attendue, ligne par ligne
     * @param requiredApi les elements que tes sources doivent contenir (ex. ".flatMap(") ;
     *                    prefixe "!" = element INTERDIT (ex. "!.get()") ;
     *                    prefixe "3x" = au moins 3 occurrences (ex. "3x.reduce(") ;
     *                    prefixe "re:" = expression reguliere (ex. "re:\\n\\s*\\{")
     */
    public static boolean check(String mainClass, String[] programArgs, Path sourceDir, List<String> ignored,
                                List<String> expected, List<String> requiredApi) throws IOException {
        System.out.println("=== Verification de " + mainClass + " ===");
        List<String> actual = runMain(mainClass, programArgs);
        boolean outputOk = compare("sortie", expected, actual);
        boolean apiOk = checkApi(sourceDir, ignored, requiredApi);
        System.out.println();
        if (outputOk && apiOk) {
            System.out.println("*** PROJET REUSSI : sortie identique et toute l'API pratiquee. ***");
        } else {
            System.out.println("*** Pas encore : " + (outputOk ? "" : "la sortie differe. ") + (apiOk ? "" : "il manque des elements de l'API (ou un element interdit est present).") + " ***");
        }
        return outputOk && apiOk;
    }

    public static boolean check(String mainClass, Path sourceDir, List<String> ignored,
                                List<String> expected, List<String> requiredApi) throws IOException {
        return check(mainClass, new String[0], sourceDir, ignored, expected, requiredApi);
    }

    /**
     * Raccourci pour le Check d'un projet : la classe main s'appelle mainName
     * (ex. "LoanDesk" ou "app.Main") dans le paquet de checkClass ; avec
     * l'argument "solution", on verifie le sous-paquet solution.
     */
    public static boolean check(Class<?> checkClass, String mainName, String[] args, String[] programArgs,
                                List<String> expected, List<String> requiredApi) throws IOException {
        String pkg = packageOf(checkClass, args);
        return check(pkg + "." + mainName, programArgs, dirOf(pkg), List.of("Data.java", "Check.java"), expected, requiredApi);
    }

    public static boolean check(Class<?> checkClass, String mainName, String[] args,
                                List<String> expected, List<String> requiredApi) throws IOException {
        return check(checkClass, mainName, args, new String[0], expected, requiredApi);
    }

    /**
     * Execute ton script de commandes (ex. commandes.sh) avec bash, depuis la
     * RACINE du depot, et compare sa sortie a la sortie attendue.
     */
    public static boolean checkScript(Class<?> checkClass, String scriptName, String[] args, List<String> expected) throws IOException {
        Path script = dirOf(packageOf(checkClass, args)).resolve(scriptName);
        System.out.println("=== Verification du script " + script.toString().replace('\\', '/') + " ===");
        if (!Files.exists(script)) {
            System.out.println("[ERREUR] script introuvable : cree " + script.toString().replace('\\', '/'));
            System.out.println("*** Pas encore : le script n'existe pas. ***");
            return false;
        }
        List<String> actual;
        try {
            ProcessBuilder builder = new ProcessBuilder(bash(), script.toString().replace('\\', '/')).redirectErrorStream(true);
            // javac / java / jar du MEME JDK que celui qui lance Check, places en tete du PATH.
            Path jdkBin = Path.of(System.getProperty("java.home"), "bin");
            builder.environment().merge("PATH", jdkBin.toString(), (old, jdk) -> jdk + java.io.File.pathSeparator + old);
            Process process = builder.start();
            byte[] out = process.getInputStream().readAllBytes();
            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                System.out.println("[ERREUR] le script ne s'est pas termine en 120 s");
            }
            actual = new String(out, StandardCharsets.UTF_8).lines().map(l -> l.replace("\r", "").stripTrailing()).toList();
        } catch (IOException e) {
            System.out.println("[SAUTE] bash introuvable sur cette machine (" + e.getMessage() + ") : lance le script a la main.");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        boolean ok = compare("script", expected, actual);
        System.out.println();
        System.out.println(ok ? "*** SCRIPT REUSSI : les commandes produisent la sortie attendue. ***"
                : "*** Pas encore : la sortie du script differe (relis tes commandes javac / java / jar). ***");
        return ok;
    }

    // Sous Windows, "bash" dans le PATH peut etre celui de WSL : on prefere Git Bash s'il est installe.
    private static String bash() {
        return Stream.of("C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe")
                .filter(p -> Files.exists(Path.of(p)))
                .findFirst()
                .orElse("bash");
    }

    private static String packageOf(Class<?> checkClass, String[] args) {
        boolean solution = args.length > 0 && args[0].equals("solution");
        return checkClass.getPackageName() + (solution ? ".solution" : "");
    }

    private static Path dirOf(String pkg) {
        Path direct = Path.of("src/main/java", pkg.replace('.', '/'));
        if (Files.isDirectory(direct)) {
            return direct;
        }
        // Lance depuis la racine du depot (IntelliJ) : on cherche le module plus bas.
        try (Stream<Path> walk = Files.walk(Path.of(""), 3)) {
            return walk.map(p -> p.resolve(direct)).filter(Files::isDirectory).findFirst().orElse(direct);
        } catch (IOException e) {
            return direct;
        }
    }

    private static List<String> runMain(String mainClass, String[] programArgs) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        String crash = null;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            Class.forName(mainClass).getMethod("main", String[].class).invoke(null, (Object) programArgs.clone());
        } catch (ClassNotFoundException e) {
            crash = "classe introuvable : " + mainClass + " (as-tu cree la classe avec ce nom et ce paquet ?)";
        } catch (NoSuchMethodException e) {
            crash = "pas de methode public static void main(String[] args) dans " + mainClass;
        } catch (InvocationTargetException e) {
            crash = "ton programme a lance " + e.getCause();
        } catch (ReflectiveOperationException e) {
            crash = e.toString();
        } finally {
            System.setOut(original);
        }
        List<String> lines = new ArrayList<>(buffer.toString(StandardCharsets.UTF_8).lines().map(String::stripTrailing).toList());
        if (crash != null) {
            System.out.println("[ERREUR] " + crash);
        }
        return lines;
    }

    private static boolean compare(String what, List<String> expected, List<String> actual) {
        int same = 0;
        while (same < expected.size() && same < actual.size() && expected.get(same).equals(actual.get(same))) {
            same++;
        }
        if (same == expected.size() && same == actual.size()) {
            System.out.println("[PASS] " + what + " : les " + expected.size() + " lignes sont identiques");
            return true;
        }
        System.out.println("[FAIL] " + what + " : " + same + "/" + expected.size() + " lignes justes avant la 1re difference (ligne " + (same + 1) + ")");
        System.out.println("       attendu : " + (same < expected.size() ? visible(expected.get(same)) : "(rien de plus)"));
        System.out.println("       obtenu  : " + (same < actual.size() ? visible(actual.get(same)) : "(rien de plus)"));
        return false;
    }

    // Les espaces comptent (text blocks !) : on les rend visibles quand une ligne en contient au debut.
    private static String visible(String line) {
        return line.startsWith(" ") ? line.replace(' ', '·') + "   (les points sont des espaces)" : line;
    }

    private static boolean checkApi(Path sourceDir, List<String> ignored, List<String> requiredApi) throws IOException {
        String code;
        try (Stream<Path> files = Files.walk(sourceDir)) {
            code = files.filter(p -> p.toString().endsWith(".java") && !ignored.contains(p.getFileName().toString()))
                    .filter(p -> !sourceDir.relativize(p).toString().replace('\\', '/').startsWith("solution/"))
                    .map(ProjectChecker::readWithoutComments)
                    .collect(Collectors.joining("\n"));
        }
        return evaluateApi(code, requiredApi);
    }

    /**
     * Pour les projets de MODULES (chapitre 12) : les sources sont HORS de src/main/java (un module-info.java
     * par module casserait la compilation Maven). Lance ton script de commandes, compare sa sortie, puis
     * verifie l'API dans tous les .java et .txt de moduleDir (module-info.java compris) et dans le script.
     *
     * @param moduleDir le dossier de tes modules, depuis la racine du depot (ex. "ch12_modules/p01_library") ;
     *                  avec l'argument "solution", on analyse moduleDir/solution
     */
    public static boolean checkModules(Class<?> checkClass, String scriptName, String moduleDir, String[] args,
                                       List<String> expected, List<String> requiredApi) throws IOException {
        boolean solution = args.length > 0 && args[0].equals("solution");
        boolean scriptOk = checkScript(checkClass, scriptName, args, expected);
        Path tree = solution ? Path.of(moduleDir, "solution") : Path.of(moduleDir);
        Path script = dirOf(packageOf(checkClass, args)).resolve(scriptName);
        System.out.println();
        System.out.println("=== Verification des sources de " + tree.toString().replace('\\', '/') + " et du script ===");
        StringBuilder code = new StringBuilder();
        if (Files.isDirectory(tree)) {
            try (Stream<Path> files = Files.walk(tree)) {
                // Les .java (module-info compris) et les .txt (un manifeste, par exemple).
                code.append(files.filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".txt"))
                        .filter(p -> solution || !tree.relativize(p).toString().replace('\\', '/').startsWith("solution/"))
                        .map(ProjectChecker::readWithoutComments)
                        .collect(Collectors.joining("\n")));
            }
        } else {
            System.out.println("[ERREUR] dossier introuvable : cree " + tree.toString().replace('\\', '/'));
        }
        if (Files.exists(script)) {
            // Dans un script, les commentaires commencent par # (en debut de ligne ou apres un espace).
            code.append('\n').append(Files.readString(script).replaceAll("(?m)(^|\\s)#[^\n]*", "$1"));
        }
        boolean apiOk = evaluateApi(code.toString(), requiredApi);
        System.out.println();
        System.out.println(scriptOk && apiOk ? "*** PROJET REUSSI : sortie identique et toute l'API pratiquee. ***"
                : "*** Pas encore : " + (scriptOk ? "" : "la sortie du script differe. ") + (apiOk ? "" : "il manque des elements de l'API (ou un element interdit est present).") + " ***");
        return scriptOk && apiOk;
    }

    private static boolean evaluateApi(String code, List<String> requiredApi) {
        // Pour les elements INTERDITS, le contenu des chaines ne compte pas ("+====+" n'est pas un +=).
        String codeOnly = code.replaceAll("(?s)\"\"\".*?\"\"\"", "\"\"\"\"\"\"").replaceAll("\"(?:[^\"\\\\\\n]|\\\\.)*\"", "\"\"");
        List<String> missing = new ArrayList<>();
        List<String> forbidden = new ArrayList<>();
        for (String api : requiredApi) {
            boolean forbid = api.startsWith("!");
            String rule = forbid ? api.substring(1) : api;
            String scanned = forbid ? codeOnly : code;
            int wanted = 1;
            if (rule.matches("\\d+x.+")) {
                wanted = Integer.parseInt(rule.substring(0, rule.indexOf('x')));
                rule = rule.substring(rule.indexOf('x') + 1);
            }
            // "re:REGEX##libelle" : expression reguliere, affichee sous son libelle lisible.
            String label = rule.contains("##") ? rule.substring(rule.indexOf("##") + 2) : rule;
            String pattern = rule.contains("##") ? rule.substring(0, rule.indexOf("##")) : rule;
            int found = pattern.startsWith("re:")
                    ? (int) Pattern.compile(pattern.substring(3)).matcher(scanned).results().count()
                    : scanned.split(Pattern.quote(pattern), -1).length - 1;
            if (!rule.contains("##") && rule.startsWith("re:")) {
                label = rule.substring(3);
            }
            if (forbid && found > 0) {
                forbidden.add(label);
            } else if (!forbid && found < wanted) {
                missing.add(label + (wanted > 1 ? " (au moins " + wanted + " fois)" : ""));
            }
        }
        if (!forbidden.isEmpty()) {
            System.out.println("[FAIL] API : interdit ici (Optional.get() ou notion d'un chapitre suivant, voir TODO.md) : " + forbidden);
        }
        if (missing.isEmpty()) {
            System.out.println("[PASS] API : tous les elements vises sont utilises");
        } else {
            System.out.println("[FAIL] API : encore a placer dans ton code : " + missing);
        }
        return missing.isEmpty() && forbidden.isEmpty();
    }

    // Les commentaires ne comptent pas : un appel ecrit seulement dans un // TODO n'est pas une utilisation.
    private static String readWithoutComments(Path file) {
        try {
            return stripJavaComments(Files.readString(file));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retire les commentaires Java en respectant les chaines : "string(/*)" ou "http://..." dans
     * une chaine ne sont PAS des commentaires (une regex naive les prendrait pour tels).
     */
    private static String stripJavaComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            if (s.startsWith("\"\"\"", i)) {
                int end = s.indexOf("\"\"\"", i + 3);
                end = end < 0 ? s.length() : end + 3;
                out.append(s, i, end);
                i = end;
            } else if (s.charAt(i) == '"' || s.charAt(i) == '\'') {
                char q = s.charAt(i);
                int j = i + 1;
                while (j < s.length() && s.charAt(j) != q && s.charAt(j) != '\n') {
                    j += s.charAt(j) == '\\' ? 2 : 1;
                }
                j = Math.min(j + 1, s.length());
                out.append(s, i, j);
                i = j;
            } else if (s.startsWith("//", i)) {
                int end = s.indexOf('\n', i);
                i = end < 0 ? s.length() : end;
            } else if (s.startsWith("/*", i)) {
                int end = s.indexOf("*/", i + 2);
                i = end < 0 ? s.length() : end + 2;
            } else {
                out.append(s.charAt(i++));
            }
        }
        return out.toString();
    }
}
