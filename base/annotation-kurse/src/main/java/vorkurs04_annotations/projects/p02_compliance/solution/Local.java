package vorkurs04_annotations.projects.p02_compliance.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Meme RUNTIME, une annotation de variable locale n'est JAMAIS ecrite dans le .class. */
@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Local {
}
