package vorkurs04_annotations.projects.p06_schema.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation de TYPE (TYPE_USE) : elle qualifie un element, ex. List<@Positive ...>, et se lit via AnnotatedType. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface Positive {
}
