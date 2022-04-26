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
package packed.internal.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.Extension;

/**
 * An annotation indicating that the runtime must install one or more extensions of annotated type requires
 * 
 * A meta-annotation that can be placed on annotations...
 * 
 * The annotation can currently be used in the following places:
 * 
 * On an {@link Extension}
 * 
 * On Annotation, Instances
 * 
 * ComponentType??? Ja det er jo saadan hvad InstanceOf
 * 
 * <p>
 * This annotation cannot be used on subclasses of {@link CommonContainerAssembly} as we want to avoid situations where some
 * extensions are added via class annotations and others via {@link CommonContainerAssembly#build()}. Giving the false impression to
 * users that only annotations added via this annotation is used
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//RequireExtension, UseExtension, ActivateExtension
//Tror den ryger ud...
//Paa componenter kan man altid bruge {packlet=componentType.class, extension = ExtensionToUse.class}
public @interface Packlet {

    /**
     * Returns the extensions that the annotated type uses.
     * 
     * @return the extensions that the annotated type uses
     */
    Class<? extends Extension<?>>[] extension();

    /**
     * Optional extension types that will only be used if they can be resolved at runtime using
     * {@link Class#forName(String, boolean, ClassLoader)} or a similar mechanism.
     * <p>
     * Checking whether or not an optional dependency is available is done exactly once per usage site. Caching the result
     * for future usage.
     * 
     * @return any optional extensions that should be used
     * @see Class#forName(String)
     */
    // I'm not sure this is really useful for Packlets, as they would normally be available in the
    // same module as the extension
    String[] optionalExtensions() default {};

    Class<?>[] sidecars() default {};
}
