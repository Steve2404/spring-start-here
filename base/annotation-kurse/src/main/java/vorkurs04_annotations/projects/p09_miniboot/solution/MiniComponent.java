package vorkurs04_annotations.projects.p09_miniboot.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Une classe que le conteneur cree une seule fois et peut injecter ailleurs. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MiniComponent {
}
