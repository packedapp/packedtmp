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
package app.packed.operation.dependency;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.container.Extension;
import app.packed.inject.Variable;

/**
 *
 */

// Kan vel laves fra ...
// Parameter
// Field
// TypeVariable
// MethodHandle parameter
// Synthetic?
// Alt hvad der kommer fra en factory

// En operation styre det foer vi slaa noget op.
// Denne operation kan goere hvad den vil med alle variable.
// Variable den ikke goer noget med
public interface BeanVariable1 {

    Variable declaredVar();

    Variable var();

    // Whether or not a value has been provided
    boolean isProvided();

    RawProvider raw();

    WrappingProvider unwrap(Object modes);

    WrappingProvider unwrap();

    interface RawProvider {

    }

    interface WrappingProvider {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface AnnotatedWithHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();

        // HttpRequestContext... requireAllContexts, requireAnyContexts
        Class<?>[] requiresContext() default {};
    }

}

interface Sazndbox {
    Object source();
}