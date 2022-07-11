package app.packed.application.sandbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A marker interface for methods that builds stuff. And cannot, for example, be used at runtime with a native image.
 */
@Target(ElementType.METHOD)
public @interface BuildsApplication {}
