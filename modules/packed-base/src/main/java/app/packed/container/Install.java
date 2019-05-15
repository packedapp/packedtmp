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

import app.packed.inject.InjectionException;

/**
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Cake to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are being injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an {@link InjectionException}.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Install {

    /**
     * The description of the component. The default value is the empty string, which means that no description will be set.
     *
     * @return the description of the component
     * @see ComponentConfiguration#setDescription(String)
     * @see Component#description()
     */
    String description() default "";

    /**
     * The name of the component. The default value is the empty string, which means that the container will automatically
     * generate a unique name for the component.
     *
     * @return the name of the component
     * @see ComponentConfiguration#setName(String)
     * @see Component#name()
     */
    String name() default "";

    boolean instantiable() default true;

}
/// **
// * Returns any children that should be installed for the component.
// *
// * @return any children that should be installed for the component
// */
// Class<?>[] children() default {};

// Would also solve our problems with mixin cycles.
// Cannot come up with any situations where you would reference
// from a component with a mixin to another component with a mixin
// Class<?>[] mixins() default {};