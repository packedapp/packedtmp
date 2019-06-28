/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.hook;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Hooks are used for callbacks. Methods annotated with this method must have exactly one parameter which is an instance
 * of either {@link AnnotatedFieldHook}, {@link OldAnnotatedMethodHook}, {@link AnnotatedTypeHook} or
 * {@link InstanceOfHook}.
 */
// Should also be able to take a Stream/List/Collection/Iterable
// Hvad hvis hvis vi bare tager en definition....
// Should we Allow Hook? matching every hook? or AnnontatedFieldHook<?> matching all field annotations

// Could allow mailbox'es for actors. Where we automatically transforms method invocations into
// We would need to have some way to indicate that some method invocation can be done without requring the result
// Maybe return Void to indicate sync and void as async?
// @Extension.ActivatorAnnotation(HooksExtension.class)
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface OnHook {

    /**
     * Whether or not the annotated method will capture hooks from outside of the defining bundle. The default value is
     * false.
     * 
     * @return whether or not the annotated method will capture hooks from outside of the defining bundle
     */
    // InternalOnly, ExternalOnly, Both
    boolean exported() default false;// export or exported?? align with @Provides
}

@interface OHWithMethod {

    // Multiplicity, 0-1, 1, 0-N, 1-N, int AppMinimum, int AppMaximum.. Maaske Noget med en Context...
    // Is it per app, per bundle, ect???? ZERO_OR_ONE, ONE, ONE_OR_MORE, ANY

    // Only component with these tags
    String[] includeComponentTags() default {};

    // never components with these tags
    String[] excludeComponentTags() default {};

    String pathMatcher() default "";// "Doo.*/*"

    /**
     * @return stuff
     */
    boolean ignoreOwnContainer() default false;

    // Use cases.....

    // maybe more allowRuntimeRegistration, I think in most situations you are not going to support it????
    // failOnRuntimeRegistration, will fail if encountering an annotation for a component installed at runtime

    // Class<? extends Annotation>[] methodsAnnotatedWith() default {};

    /**
     * Will attempt to compile the resulting very aggressively. For example, via lambda factory.
     * 
     * @return stuff
     */
    // Or Class<? extends SomeOptimizer>
    boolean attemptCompilation() default false;

    //
    // Man kan registere en eller flere MethodHandleTransformation for et Hook. Maaske via annoterering
    //

    // If true AnnotatedMethodHook.methodHandle returns non-null;
    // if false AnnotatedMethodHook.methodHandle throws UnsupportedOperationException
    // boolean needsHandle default false();

    /**
     * Her er taenkt paa at vi paa en eller anden maade beskriver method transformationen. Bytekode haandteringen... Et
     * eller andet der goer at vi kan lave en generics transformation vi kan kompilere. Dette er mere eller mindre umuligt
     * 
     * 
     * @return
     */
    // Class<? extends AOPRewriter> rewriter() default AOPRewriter.class;
}

// boolean disableForOwnContainer
// transient?? Containers of Containers...Or Apps of Apps... meaning it will hook down the food chain...

// Analysis

// On Initialize
// Lifecycle....
// @Inject <- Inject phase ..... Saa burde det vel ogsaa virke i injector:!>>!!! only, on non-provided methods...
// betyder det vi ogsaa har hooks....??? Naaah, maaske vi goer det paa en anden maade
// @OnInitialize
// @OnStart
// @OnStop
// @OnNative.....

class AOPRewriter {
    // Taenker vi hellere vil have en alternativ klasse til AnnotatedComponentMethod...
    // Saa det er paa parameteren vi kender forskel og ikke paa @Hook annoteringen
}
// Kunne jo saadan set godt tillade, metoder der returnerede CompletableFuture....
// allowFieldWrite..
