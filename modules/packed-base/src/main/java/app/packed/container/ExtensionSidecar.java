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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A sidecar annotation that can be used on subclasses of {@link Extension}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionSidecar {

    /**
     * The extension has been successfully instantiated. But the extension instance has not yet been returned to the user.
     * Used for invoking methods on {@link ExtensionContext}.
     */
    String ON_INSTANTIATION = "On_Instantiation";

    /** All components and extensions have been added and configured. */
    String ON_PREEMBLE = "on_premble";

    /**
     * Any child containers located in the same artifact will be has been defined. Typically using
     * {@link Bundle#link(Bundle, app.packed.container.Wirelet...)}.
     */
    String ON_CHILDREN_DONE = "on_children_done";

    /**
     * Other extensions that an extension may use (but do not have to).
     * 
     * @return extensions that the extension may use
     */
    Class<? extends Extension>[] dependencies() default {};

    /**
     * Other extensions that an extension may use if they are present on the classpath or modulepath.
     * <p>
     * The extension types will only be used if they can be resolved at runtime using
     * {@link Class#forName(String, boolean, ClassLoader)} or a similar mechanism.
     * <p>
     * Checking whether or not an optional dependency is available is done exactly once per usage site. Caching the result
     * for future usage.
     * 
     * @return extensions that the extension may use if they are present on the classpath or modulepath
     */
    String[] optionalDependencies() default {};

    /**
     * Pipelines.
     * 
     * Will be made available to features, descriptors, and at instantiation.
     * 
     * @return pipelines
     */
    Class<? extends ExtensionWireletPipeline<?, ?, ?>>[] pipelines() default {};
}
// Pipelines will be made available if any wirelets using them are specified
// Otherwise an empty Optional can be used...

//Must be nonstatic and parameter less????
//Well they should both nonstatic or static, and take ExtensionContext, InjectionContext

//An extension cannot have more than one method for a given assembling event.
//@AfterExtension(Instantiated)

//Container finished

//Child Linked
//Parent Linked
//NoParent Linked... //NoDirectLink

//Er det her vi linker paa tvaers af artifacts?????

//ExtensionBarrier....

//Kan sagtens tage statiske metoder..

//F.eks. en artifact finished....
//enum ExtensionState {
//
///**
//* The extension has been successfully instantiated. But the extension instance has not yet been returned to the user.
//* Used for invoking methods on {@link ExtensionContext}.
//*/
//INSTANTIATED,
//
///** All components and extensions have been added and configured. */
//PREEMBLED_DONE,
//
///**
//* Any child containers located in the same artifact will be has been defined. Typically using
//* {@link Bundle#link(Bundle, app.packed.container.Wirelet...)}.
//*/
//CHILDREN_ADDING_FINISHED,
//
///** */
//GUESTS_FINISHED;
//}
