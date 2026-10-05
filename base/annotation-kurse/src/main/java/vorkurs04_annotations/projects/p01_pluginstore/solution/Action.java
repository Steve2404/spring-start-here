package vorkurs04_annotations.projects.p01_pluginstore.solution;

/** @FunctionalInterface demande au compilateur de verifier qu'il y a UNE seule methode abstraite. */
@FunctionalInterface
public interface Action {
    String run(String input);

    // Une methode default ne compte pas : l'interface reste fonctionnelle.
    default Action twice() {
        return input -> run(run(input));
    }
}
