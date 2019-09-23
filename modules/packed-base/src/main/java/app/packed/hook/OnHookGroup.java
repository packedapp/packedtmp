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
 * of either {@link AnnotatedFieldHook}, {@link AnnotatedMethodHook}, {@link AnnotatedTypeHook} or
 * {@link InstanceOfHook}.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface OnHookGroup {

    /**
     * The type of builder that will create the actual group.
     * 
     * The returned builder must be instantiable to "app.packed.base"
     * 
     * @return an aggregate builder
     */
    Class<? extends HookGroupBuilder<?>> value();
}

/// **
// * Whether or not the annotated method will capture hooks from outside of the defining bundle. The default value is
// * false.
// *
// * @return whether or not the annotated method will capture hooks from outside of the defining bundle
// */
//// InternalOnly, ExternalOnly, Both
//// Makes no sense for extensions which is what we use hooks for now, because the exports und so weiter.
//// Is controlled on a per extension basis
// boolean exported() default false;// export or exported?? align with @Provides

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

// class AOPRewriter {
// Taenker vi hellere vil have en alternativ klasse til AnnotatedComponentMethod...
// Saa det er paa parameteren vi kender forskel og ikke paa @Hook annoteringen
// }
// Kunne jo saadan set godt tillade, metoder der returnerede CompletableFuture....
// allowFieldWrite..
// Should also be able to take a Stream/List/Collection/Iterable...
//// Nah virker ikke paa runtime
// Hvad hvis hvis vi bare tager en definition....
// Should we Allow Hook? matching every hook? or AnnontatedFieldHook<?> matching all field annotations

// Could allow mailbox'es for actors. Where we automatically transforms method invocations into
// We would need to have some way to indicate that some method invocation can be done without requring the result
// Maybe return Void to indicate sync and void as async?
// @Extension.ActivatorAnnotation(HooksExtension.class)