package vorkurs04_annotations.projects.p04_inspector.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @Inherited : getAnnotation(Audit.class) la trouve aussi sur une SOUS-CLASSE (pas sur une classe qui implemente une interface). */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audit {
}
