package vorkurs04_annotations.projects.p09_miniboot;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du capstone (ne pas modifier). Il execute TON MiniBoot.main, compare sa sortie a
 * EXPECTED, puis verifie que tout le chapitre 0.4 a servi a construire ton mini-framework.
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "APP boutique",
            "  ignore Mailer",
            "  composants : [Audit, Clock, OrderService, Repository, ShopSettings, SmtpMailer]",
            "  ordre : Clock -> Repository -> ShopSettings -> SmtpMailer -> OrderService -> Audit",
            "  config ShopSettings : debug=false (defaut) owner=admin (defaut) port=8080 timeout=30",
            "  afterLoad ShopSettings.check : ok",
            "  injections : Audit.orders<-OrderService OrderService.mailer<-SmtpMailer OrderService.repository<-Repository OrderService.settings<-ShopSettings Repository.clock<-Clock",
            "  warmUp : order-1@08:00 mail->admin port 8080",
            "  onStart OrderService.warmUp : ok",
            "  open : audit pret",
            "  onStart Audit.open : ok",
            "  DEMARRE : 6 composant(s)",
            "APP port-invalide",
            "  composants : [Clock, Repository, ShopSettings]",
            "  ordre : Clock -> Repository -> ShopSettings",
            "  config ShopSettings : debug=false (defaut) owner=lea port=70000 timeout=5",
            "  ECHEC afterLoad ShopSettings.check : IllegalStateException port hors limites : 70000",
            "  ARRET",
            "APP cycle",
            "  composants : [Clock, Ping, Pong]",
            "  ECHEC cycle : Ping, Pong",
            "  ARRET",
            "APP sans-mailer",
            "  composants : [Clock, OrderService, Repository, ShopSettings]",
            "  ECHEC injection OrderService.mailer : aucun composant de type Mailer",
            "  ARRET",
            "APP mauvaise-config",
            "  composants : [StrictSettings]",
            "  ordre : StrictSettings",
            "  ECHEC config StrictSettings.enabled : 'yes' n'est pas un boolean",
            "  ECHEC config StrictSettings.level : champ static interdit",
            "  ECHEC config StrictSettings.name : champ final interdit",
            "  ECHEC config StrictSettings.retries : 'trois' n'est pas un int",
            "  ARRET",
            "APP structure",
            "  composants : [AbstractJob, BadStart, Clock]",
            "  ECHEC structure AbstractJob : classe abstraite",
            "  ECHEC structure BadStart.start : @OnStart doit etre void, sans parametre, non static",
            "  ARRET",
            "APP fragile",
            "  composants : [Clock, Fragile]",
            "  ordre : Clock -> Fragile",
            "  ECHEC creation Fragile : IllegalStateException disque plein",
            "  ARRET");
            // EXPECTED-END

    static final List<String> API = List.of(
            "@Retention(", "@Target(", "default", "Javac.compile(", "Javac.load(", "Data.SOURCES", "Data.APPS",
            ".isAnnotationPresent(", ".getAnnotation(", ".getDeclaredFields()", ".getDeclaredMethods()",
            ".getDeclaredConstructor()", ".newInstance()", ".canAccess(", ".trySetAccessible()",
            ".set(", ".invoke(", "InvocationTargetException", ".getCause()",
            "Modifier.isAbstract(", "Modifier.isStatic(", "Modifier.isFinal(",
            ".getParameterCount()", ".getReturnType()", "void.class", "isAssignableFrom", ".getDeclaringClass()",
            // Un framework demande l'acces poliment ; setAccessible(true) peut exploser (projet 7).
            "!.setAccessible(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "MiniBoot", args, EXPECTED, API);
    }
}
