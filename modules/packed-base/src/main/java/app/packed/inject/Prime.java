package app.packed.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented

//Dom?
//Dynamic Inject
//@ProvideDynamically...
@interface Prime {}

@interface PrimeProvider {}

// Produce instead of provide...

// ProvideSingle
// ProvidePrototype
// ProvideViaPrime() <--- is protoype
// -- Explicitly defined via a sidecar...
// -- Overriden in the some internals

/// ----