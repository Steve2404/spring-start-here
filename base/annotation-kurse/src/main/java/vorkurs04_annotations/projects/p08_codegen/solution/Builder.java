package vorkurs04_annotations.projects.p08_codegen.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Demande la generation d'un builder. SOURCE suffit : un processor travaille PENDANT la compilation,
 * il lit le code source ; personne n'a besoin de l'annotation dans le .class ni a l'execution.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Builder {
}
