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
package app.packed.sidecar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;

/**
 * An annotation that can be used on subclasses of {@link Extension}. {@link Extension Extensions} are implicit sidecars
 * even without this annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionSidecar {

    /**
     * The extension has been successfully instantiated. But the extension instance has not yet been returned to the user.
     * Used for invoking methods on {@link ExtensionContext}. The next event will be {@link #ON_PREEMBLE}.
     */
    String INSTANTIATION = "Instantiation";

    /** All components and extensions have been added and configured. The next event will be {@link #CHILDREN_CONFIGURED} */
    String ON_PREEMBLE = "on_premble";

    /**
     * Any child containers located in the same artifact will be has been defined. Typically using
     * {@link Bundle#link(Bundle, app.packed.container.Wirelet...)}. The next event will be {@link #GUESTS_CONFIGURED}.
     */
    String CHILDREN_CONFIGURED = "ChildrenConfigured";

    /** This is the final event. */
    String GUESTS_CONFIGURED = "GuestsConfigured";

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
     * Checking whether or not an optional dependency is available is done exactly once when the extension is first used.
     * Caching the result for future usage.
     * 
     * @return extensions that the extension may use if they are present on the classpath or modulepath
     */
    String[] optionalDependencies() default {};
}
//boolean requireExecution default false()???

//Well they should both nonstatic or static, and take ExtensionContext, InjectionContext

//Container finished

//Child Linked
//Parent Linked
//NoParent Linked... //NoDirectLink
//Er det her vi linker paa tvaers af artifacts?????
//ExtensionBarrier....
