package vorkurs04_annotations.projects.p01_pluginstore.solution;

/**
 * La carte d'identite d'un plugin. Les elements ressemblent a des methodes, mais ce sont des
 * champs de metadonnees : id est OBLIGATOIRE (pas de default), les autres ont une valeur par defaut.
 */
public @interface Plugin {
    String id();

    int version() default 1;

    String[] tags() default {};

    Category category() default Category.TOOL;
}
