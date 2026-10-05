package vorkurs04_annotations.projects.p03_typeguard.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Inherited : une SOUS-CLASSE d'une classe @Audit sera vue comme @Audit par la reflection
 * (projet 4). Seulement pour une annotation de CLASSE, et jamais via une interface.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audit {
}
