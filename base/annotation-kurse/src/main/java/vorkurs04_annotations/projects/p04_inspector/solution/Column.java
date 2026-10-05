package vorkurs04_annotations.projects.p04_inspector.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** nullable a une valeur par defaut : la reflection rend true meme si personne ne l'a ecrit. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();

    boolean nullable() default true;
}
