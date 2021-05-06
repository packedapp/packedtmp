package app.packed.state;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// Ideen er lidt at man kan smide det paa en annotering som saa kan bruges

// @OnActorKilled(). er saa configured

// Taenker ogsaa man kan bruge @Provide f.eks. StopReason... Eller ogsaa er den bare til raadigehed under stopping?

/**
 * Kan bruges paa method
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
// @MetaHook(method = @AnnotatedMethodHook(ddd.bootstrap), bootstrapClass = StateTransitionHook.Bootstrap.class)
// Det er et hook der kraever en accessible method... Men provider et specielt bootstrap...
public @interface StateTransitionHook {}

// ifRestarting...
///// Paa en eller anden maade bliver Restarting noedt til at vaere en feature...


// Altsaa det der er specielt i forhold til AccessibleMethodHook
// Er at naar man laver annoteringen faar man ikke selv adgang til metoden
// Saa det er faktisk et slags meta hook