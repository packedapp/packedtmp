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
package app.packed.conversion;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Nullable;

/**
 * A container object which may or may not contain the result of an object-to-object conversion. If the conversion was
 * successful, {@code isConverted()} returns {@code true}. If the object could not be converted, {@code isFailed()}
 * returns {@code false}.
 */
// source -> 
public final class Conversion<T> {

    /** A failed conversion. */
    @Nullable
    private final PackedConversionFailure failure;

    /** A successfully converted value. */
    @Nullable
    private final T value;

    /**
     * Creates a new conversion. Either {@code value} or {@code failure} must always be null.
     * 
     * @param value
     *            the converted value
     * @param failure
     *            a failed conversion
     */
    private Conversion(@Nullable T value, @Nullable PackedConversionFailure failure) {
        this.value = value;
        this.failure = failure;
    }

    /**
     * Asserts that the conversion was successful or throws an {@link AssertionError}.
     * 
     * @throws AssertionError
     *             if the conversion was not successful
     */
    public void assertConverted() {
        if (failure != null) {
            throw new AssertionError("oops");
        }
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception produced by the exception supplying function.
     *
     * @apiNote A method reference to the exception constructor with an empty argument list can be used as the supplier. For
     *          example, {@code IllegalStateException::new}
     *
     * @param <X>
     *            Type of the exception to be thrown
     * @param exceptionSupplier
     *            the supplying function that produces an exception to be thrown
     * @return the value, if present
     * @throws X
     *             if no value is present
     * @throws NullPointerException
     *             if no value is present and the exception supplying function is {@code null}
     */
    public <X extends Throwable> T orElseThrow(Function<String, ? extends X> exceptionSupplier) throws X {
        if (failure == null) {
            return value;
        } else {
            throw exceptionSupplier.apply(failure.toString());
        }
    }

    /**
     * Returns the result of the conversion if successful. Otherwise throws {@link ConversionException}.
     * 
     * @return the result of the conversion
     * @throws ConversionException
     *             if the conversion failed
     */
    public T get() {
        if (failure == null) {
            return value;
        }
        throw new ConversionException("oops");
    }

    /**
     * If the conversion was successful, performs the given action with the converted value, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if the conversion was successful
     */
    public void ifConverted(Consumer<? super T> action) {
        if (failure == null) {
            action.accept(value);
        }
    }

    /**
     * Returns whether or not the conversion was successful.
     *
     * @return {@code true} if the conversion was successful, otherwise {@code false}
     */
    public boolean isConverted() {
        return failure == null;
    }

    /**
     * Returns whether or not the conversion failed.
     *
     * @return {@code true} if the conversion failed, otherwise {@code false}
     */
    public boolean isFailed() {
        return failure != null;
    }

    /**
     * @param <T>
     *            the type value a successful conversion would have created
     * @param context
     *            a conversion context
     * @return a failed conversion
     */
    public static final <T> Conversion<T> failed(ConversionContext context) {
        return new Conversion<>(null, new PackedConversionFailure("Oops"));
    }

    public static final <T> Conversion<T> failed(String message) {
        return new Conversion<>(null, new PackedConversionFailure(message));
    }

    /**
     * Creates a new successful conversion.
     * 
     * @param <T>
     *            the type value converted to
     * @param value
     *            the value that was successfully converted
     * @return a successful conversion
     */
    public static final <T> Conversion<T> of(T value) {
        requireNonNull(value, "value is null");
        return new Conversion<>(value, null);
    }

    public static void main(String[] args) {
        Conversion.of("foo").orElseThrow(s -> new IllegalArgumentException(s));

        Conversion.failed("foo").orElseThrow(s -> new IllegalArgumentException(s));
    }
}
