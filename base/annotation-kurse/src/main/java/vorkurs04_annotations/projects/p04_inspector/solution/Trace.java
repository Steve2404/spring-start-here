package vorkurs04_annotations.projects.p04_inspector.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Retention CLASS (par defaut) : bien presente dans le .class, mais la reflection ne la voit pas. */
@Target(ElementType.METHOD)
public @interface Trace {
}
