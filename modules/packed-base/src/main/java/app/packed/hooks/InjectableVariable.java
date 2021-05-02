package app.packed.hooks;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import app.packed.inject.Provider;

public interface InjectableVariable {

    /**
     * If the variable is {@link #isOptional() optional} this method returns the optional class.
     * <p>
     * The optional class is currently one of {@link Optional}, {@link OptionalInt}, {@link OptionalDouble} or OptionalLong.
     * 
     * @return
     */
    Optional<Class<?>> getOptionalClass();

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

    // cannot both be nullable and have a default value
    // cannot both be optional and have a default value
    // if default value it is not required...
    boolean hasDefaultValue(); // maybe we can have a boostrap#setDefaultValue() (extract the string)
}

class DefaultValue {
    // ??
    // String/String[]
}