package vorkurs04_annotations.projects.p09_miniboot.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Appelee juste apres le chargement de la configuration, pour la validation metier. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterLoad {
}
