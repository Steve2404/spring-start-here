package vorkurs04_annotations.projects.p04_inspector.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Le conteneur : c'est LUI qui est stocke quand un @Tag est ecrit deux fois. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Tags {
    Tag[] value();
}
