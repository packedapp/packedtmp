package app.packed.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotation can be used to dynamically provide a variable.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicVariable {}

// Dynamic variables are dependencies that cannot statically be expressed as a key..
// F.eks. if you want to inject a system property @SystemProperty("doobar") has infinite many possibilities
// DynamicVariable to the rescue

// Can either be used with a sidecar 
// Or without a sidecar in which the context in which it is used must provide it.

//@SomePAnnotation cannot be used in this context.
// Throw new ProvisionException
//Look at the annotation to see which contexts it can be used in.

/// Optional...
//@DynamicVariable(supportOptional = true)

//// Old Names
//Dom, DynVar, Prime, @ProvideDynamically...
//Produce instead of provide...
//ProvideSingle
//ProvidePrototype
//ProvideViaPrime() <--- is protoype
//-- Explicitly defined via a sidecar...
//-- Overriden in the some internals
//WildcardVariable <- Nah det har jo ikke noget med Generics at goere....

// >=2 Dynamic variable anntoations fail
// = 1 Dynamic variable ok
// = 0 ordinary service