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
package app.packed.bean.hooks.sandboxinvoke;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.Provider;

/**
 *
 */
public class VariableParser<T> {
    
}

class Zandbox {

    public interface InjectableVariableDescription {

        /**
         * If the variable is {@link #isOptional() optional} this method returns the optional class.
         * <p>
         * The optional class is currently one of {@link Optional}, {@link OptionalInt}, {@link OptionalDouble} or OptionalLong.
         * 
         * @return
         */
        Optional<Class<?>> getOptionalClass();

        // cannot both be nullable and have a default value
        // cannot both be optional and have a default value
        // if default value it is not required...
        boolean hasDefaultValue(); // maybe we can have a boostrap#setDefaultValue() (extract the string)

        boolean isNullable();

        boolean isOptional();

        /**
         * Wrapped in {@link Provider}.
         * 
         * @return
         */
        boolean isProvider();

        /**
         * @return
         * 
         * @see #isNullable()
         * @see #isOptional()
         * @see #hasDefaultValue()
         */
        boolean isRequired();
    }

    public @interface InjectableVariableHook {

        interface Stuff {

            Object getDefaultValue();

            boolean hasDefaultValue();

            /**
             * @return whether or not there is fallback mechanism for providing a value, for example, a default value
             */
            boolean hasFallback();

            /** {@return whether or not a Nullable annotation is present on the variable} */
            boolean isNullable();

            /**
             * @return whether or the variable is wrapped in an optional type
             * @see Optional
             * @see OptionalDouble
             * @see OptionalLong
             * @see OptionalInt
             */
            boolean isOptional();

            /** {@return whether or not the variable is wrapped in a Provider type} */
            boolean isProvider();

            default boolean isRequired() {
                return !isNullable() && !isOptional() && !hasFallback();
            }
        }

        enum TransformerSupport {
            COMPOSITE, // Any annotation that is called Nullable...
            CONVERSION, DEFAULTS, LAZY, NULLABLE,

            OPTIONAL, PROVIDER, VALIDATATION
        }
    }
}