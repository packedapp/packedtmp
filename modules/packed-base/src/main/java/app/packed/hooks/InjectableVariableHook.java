package app.packed.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 *
 */
// Kan man styre tidspunkt med felter???
// Det taenker jeg ikke. Det er altid ved instantiering...
public @interface InjectableVariableHook {

    Class<? extends Annotation>[] annotation() default {};

    Class<?>[] exactClass() default {};

    // Altsaa taenker lidt det giver sig selv om typen er parameterized
    // Hvis den er parameterizered er man jo interesset i at extract den praecise type
    Class<?>[] rawType() default {};


    abstract class Bootstrap {

        // Unwrapped...
        public final Class<?> getType() {
            throw new UnsupportedOperationException();
        }

        /**
         * @return the actual type of the underlying variable
         * @see Field#getType()
         * @see Parameter#getType()
         * @see ParameterizedType#getRawType()
         */
        public final Class<?> getActualType() {
            throw new UnsupportedOperationException();
        }

        public final Type getParameterizedType() {
            throw new UnsupportedOperationException();
        }

        public final Type getActualParameterizedType() {
            throw new UnsupportedOperationException();
        }
    }

    interface Stuff {

        /**
         * @return whether or not there is fallback mechanism for providing a value, for example, a default value
         */
        boolean hasFallback();

        /** {@return whether or not a Nullable annotation is present on the variable} */
        boolean isNullable();

        boolean hasDefaultValue();

        Object getDefaultValue();

        /**
         * @return whether or the variable is wrapped in an optional type
         * @see Optional
         * @see OptionalDouble
         * @see OptionalLong
         * @see OptionalInt
         */
        boolean isOptional();

        default boolean isRequired() {
            return !isNullable() && !isOptional() && !hasFallback();
        }

        /** {@return whether or not the variable is wrapped in a Provider type} */
        boolean isProvider();
    }

    enum TransformerSupport {
        NULLABLE, // Any annotation that is called Nullable...
        OPTIONAL, PROVIDER, DEFAULTS, VALIDATATION,

        CONVERSION, LAZY, COMPOSITE
    }
}
