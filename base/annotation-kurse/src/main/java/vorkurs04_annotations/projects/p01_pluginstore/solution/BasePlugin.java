package vorkurs04_annotations.projects.p01_pluginstore.solution;

import java.util.List;

/**
 * La classe de base de tous les plugins. Elle porte les annotations "connues" de 0.4.2 :
 * elles ne CHANGENT rien a l'execution, elles donnent des consignes au compilateur.
 */
public abstract class BasePlugin {

    public abstract String name();

    /** La nouvelle API, qui remplace init() et legacyInit(). */
    public void start() {
    }

    // since documente QUAND, forRemoval=false (par defaut) : simple avertissement "deprecation".
    @Deprecated(since = "2.0")
    public void init() {
    }

    // forRemoval=true : avertissement plus fort ("removal"), meme si on a supprime "deprecation".
    @Deprecated(since = "2.0", forRemoval = true)
    public void legacyInit() {
    }

    // @SafeVarargs n'est permis que sur une methode qu'on ne peut pas redefinir (static, final,
    // private) ou un constructeur ; il promet que le tableau generique n'est pas pollue.
    @SafeVarargs
    public static <T> List<T> listOf(T... items) {
        return List.of(items);
    }

    // Le compilateur verifie que toString existe bien dans Object : une faute de frappe serait refusee.
    @Override
    public String toString() {
        return "Plugin " + name();
    }
}
