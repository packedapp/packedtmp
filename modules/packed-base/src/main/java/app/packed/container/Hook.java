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
package app.packed.container;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Hooks are used for callbacks.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface Hook {

    // Class<? extends Annotation>[] methodsAnnotatedWith() default {};

    /**
     * Will attempt to compile the resulting very aggressively. For example, via lambda factory.
     * 
     * @return
     */
    // Or Class<? extends SomeOptimizer>
    boolean attemptCompilation() default false;

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
