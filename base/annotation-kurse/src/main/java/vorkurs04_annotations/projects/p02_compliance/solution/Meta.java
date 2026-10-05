package vorkurs04_annotations.projects.p02_compliance.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Une annotation pour annoter d'autres annotations (une meta-annotation maison). */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Meta {
}
