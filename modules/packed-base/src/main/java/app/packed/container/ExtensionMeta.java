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
 * An extension sidecar annotation that can be used on subclasses of {@link Extension}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionMeta {

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
    Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>[] pipelines() default {};
}
// Pipelines will be made available if any wirelets using them are specified
// Otherwise an empty Optional can be used...
