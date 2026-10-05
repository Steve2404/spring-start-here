package vorkurs04_annotations.projects.p02_compliance.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** SOURCE : le compilateur la lit puis l'oublie, rien dans le .class. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Generated {
}
