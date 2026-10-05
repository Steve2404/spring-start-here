package vorkurs04_annotations.projects.p03_typeguard.solution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Repetable : @Tag("a") @Tag("b") est range par javac dans un conteneur @Tags invisible a l'ecriture. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(Tags.class)
public @interface Tag {
    String value();
}
