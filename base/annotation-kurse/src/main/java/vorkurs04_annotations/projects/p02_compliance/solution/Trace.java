package vorkurs04_annotations.projects.p02_compliance.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Pas de @Retention : la valeur par defaut est CLASS (ni SOURCE, ni RUNTIME). */
@Target(ElementType.METHOD)
public @interface Trace {
}
