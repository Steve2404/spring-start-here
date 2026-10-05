package vorkurs04_annotations.projects.p03_typeguard.solution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Le conteneur : un element value() qui est un TABLEAU de Tag, une retention au moins aussi longue,
 * des cibles incluses dans celles de Tag, et @Documented puisque Tag l'est.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Tags {
    Tag[] value();
}
