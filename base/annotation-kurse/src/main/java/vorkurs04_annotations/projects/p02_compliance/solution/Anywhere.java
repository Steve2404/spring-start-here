package vorkurs04_annotations.projects.p02_compliance.solution;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Pas de @Target : applicable a toutes les DECLARATIONS, mais pas aux usages de type ni aux parametres de type. */
@Retention(RetentionPolicy.RUNTIME)
public @interface Anywhere {
}
