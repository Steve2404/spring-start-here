package vorkurs04_annotations.projects.p03_typeguard.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** FIELD et TYPE_USE : sur "@Trimmed String code", elle s'applique a la declaration ET au type. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
public @interface Trimmed {
}
