package vorkurs04_annotations.projects.p07_access;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 7 (ne pas modifier). Il execute TON AccessAuditor.main, compare sa sortie
 * a EXPECTED, puis verifie la pratique du controle d'acces de la reflection (0.4.12).
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "READ Vault.counter : accessible ; direct -> 7",
            "READ Vault.secret : inaccessible ; sans forcer IllegalAccessException ; force -> abc",
            "CALL Vault.reveal : inaccessible ; sans forcer IllegalAccessException ; force -> abc",
            "WRITE Vault.secret xyz : inaccessible ; sans forcer IllegalAccessException ; force -> ecrit",
            "CALL Vault.reveal : inaccessible ; sans forcer IllegalAccessException ; force -> xyz",
            "WRITE Vault.owner tom : inaccessible ; sans forcer IllegalAccessException ; force -> ecrit",
            "CALL Vault.owner : accessible ; direct -> tom",
            "WRITE Vault.code 99 : inaccessible ; sans forcer IllegalAccessException ; force -> ecrit",
            "READ Vault.code : inaccessible ; sans forcer IllegalAccessException ; force -> 99",
            "CALL Vault.code : accessible ; direct -> 1234",
            "WRITE Vault.VERSION 2.0 : inaccessible ; sans forcer IllegalAccessException ; force -> IllegalAccessException",
            "CALL Vault.hint ok : inaccessible ; sans forcer IllegalAccessException ; force -> ok!",
            "READ Vault.missing : introuvable (NoSuchFieldException)",
            "CREATE Vault : inaccessible ; sans forcer IllegalAccessException ; force -> objet Vault",
            "WRITE Point.x 5 : inaccessible ; sans forcer IllegalAccessException ; force -> IllegalAccessException",
            "CREATE Shape : accessible ; direct -> InstantiationException",
            "CREATE Thrower : accessible ; direct -> la cible a leve IllegalStateException (boom)",
            "READ String.value : inaccessible ; sans forcer IllegalAccessException ; trySetAccessible false (module ferme)",
            "FORCE String.value : InaccessibleObjectException",
            "MODULE Vault : nomme false ; MODULE String : java.base ; java.lang exporte true ; ouvert a Vault false",
            "MODIFICATEURS VERSION : private static final ; Point.x : private final");
            // EXPECTED-END

    static final List<String> API = List.of(
            "Javac.compile(", "Javac.load(", "Data.TARGETS", "Data.PROBES",
            ".getDeclaredField(", ".getDeclaredMethods()", ".getDeclaredConstructor(", ".getConstructor(",
            ".canAccess(", ".trySetAccessible()", ".setAccessible(true)", "AccessibleObject",
            ".get(", ".set(", ".invoke(", ".newInstance(",
            "IllegalAccessException", "InaccessibleObjectException", "ReflectiveOperationException",
            "InvocationTargetException", ".getCause()", "NoSuchFieldException", "NoSuchMethodException",
            "Modifier.isStatic(", "Modifier.toString(", ".getModule()", ".isNamed()", ".isExported(", ".isOpen(",
            // Crescendo : processors (0.4.13) plus tard.
            "!AbstractProcessor");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "AccessAuditor", args, EXPECTED, API);
    }
}
