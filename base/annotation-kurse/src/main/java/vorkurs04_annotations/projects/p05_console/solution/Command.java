package vorkurs04_annotations.projects.p05_console.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Une commande de la console. RUNTIME, sinon la console ne la trouverait pas ; les defauts rendent
 * l'annotation courte a ecrire (@Command(name = "ls")) tout en donnant une valeur a chaque element.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String name();

    String description() default "";

    boolean adminOnly() default false;

    int priority() default 0;
}
