package vorkurs04_annotations.projects.p09_miniboot.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Une classe de configuration : ses champs @ConfigValue sont lus sous prefix. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MiniConfig {
    String prefix();
}
