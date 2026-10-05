package vorkurs04_annotations.projects.p01_pluginstore.solution;

/** Un tableau de Class : @Requires(String.class) marche aussi (un seul element entre accolades implicites). */
public @interface Requires {
    Class<?>[] value();
}
