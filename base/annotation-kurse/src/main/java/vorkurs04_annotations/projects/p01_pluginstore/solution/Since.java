package vorkurs04_annotations.projects.p01_pluginstore.solution;

/** Annotation a element unique : grace au nom "value", on peut ecrire @Since("1.0") sans "value =". */
public @interface Since {
    String value();
}
