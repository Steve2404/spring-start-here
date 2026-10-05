package vorkurs04_annotations.projects.p04_inspector.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Repetable, mais PAS @Inherited : une sous-classe n'herite pas des tags de sa super-classe. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(Tags.class)
public @interface Tag {
    String value();
}
