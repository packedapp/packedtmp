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
 * An annotation that can be used on subclasses of {@link Extension}. Classes that extend {@link Extension} are implicit
 * sidecars even without the use of this annotation. However, if the extension uses any other extensions this annotation
 * must be used to indicate which extensions it may use.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionSidecar {

    /**
     * Used together with the {@link PostSidecar} annotation to indicate that an {@link Extension}method should be executed
     * as soon as the extension has been successfully instantiated and before it is returned to the user.
     * <p>
     * 
     * An extension sidecar event that the sidecar has been successfully instantiated by the runtime. But the instance has
     * not yet been returned to the user. The next event will be {@link #ON_PREEMBLE}.
     */
    String INSTANTIATION = "Instantiation";

    /** All components and extensions have been added and configured. The next event will be {@link #CHILDREN_CONFIGURED} */
    String ON_PREEMBLE = "on_premble";

    /**
     * Any child containers located in the same artifact will be has been defined. Typically using
     * {@link Bundle#link(Bundle, app.packed.container.Wirelet...)}. The next event will be {@link #GUESTS_CONFIGURED}.
     */
    String CHILDREN_CONFIGURED = "ChildrenConfigured";

    /** This is the final event. This event will be invoked even if no guests are defined. */
    String GUESTS_CONFIGURED = "GuestsConfigured";

    /**
     * Other extensions that an extension may use (but do not have to). This need not include transitive dependencies
     * (dependencies of dependencies). Only extensions that are directly used, for example, via
     * {@link ExtensionContext#use(Class)}.
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
