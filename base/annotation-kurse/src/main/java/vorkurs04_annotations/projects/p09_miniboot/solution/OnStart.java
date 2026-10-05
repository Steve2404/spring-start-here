package vorkurs04_annotations.projects.p09_miniboot.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Appelee une fois, apres toutes les injections, dans l'ordre des dependances. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnStart {
}
