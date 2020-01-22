package app.packed.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented

// Allow er nok et daarligt ord...
//Allow services
//Allow Nullables
//Allow Provider...
//Allow Default
//Allow Optional
//Allow Converters

//Support verification...

// Allow Composite <- Spoergmaalet er om vi skal checke det???
// Det taenker jeg. Cannot use @HttpParam together with @Composite
// Or on X type annotated with @Composite...
public @interface Prime {}

@interface PrimeProvider {}

// Produce instead of provide...
