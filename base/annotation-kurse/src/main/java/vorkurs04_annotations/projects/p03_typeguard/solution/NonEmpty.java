package vorkurs04_annotations.projects.p03_typeguard.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation de TYPE : elle qualifie un usage de type (List<@NonEmpty String>), pas une declaration. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface NonEmpty {
}
