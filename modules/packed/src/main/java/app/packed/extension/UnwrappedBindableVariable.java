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
package app.packed.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Supplier;

import app.packed.operation.Op;
import app.packed.util.Nullable;
import app.packed.util.Variable;
import internal.app.packed.bean.PackedBindableWrappedVariable;

/**
 * A bindable variable whose type has been unwrapped from various "container objects" such as {@link Optional},
 * {@link app.packed.operation.Provider}
 *
 * <p>
 * The following container objects and annotations will be processed by default:
 *
 * Opts into
 *
 * Optional
 *
 * Provider
 *
 * Lazy
 *
 * Nullable
 *
 * Validate (Er jo i virkeligheden peek??
 *
 * Default
 *
 * <p>
 * By default there are a number of combinations that will fail:
 *
 * Optional, Nullable, default annotation combinations makes no sense
 */
// BeanInjectableVariablw
public sealed interface UnwrappedBindableVariable extends BindableVariable permits PackedBindableWrappedVariable {

    @Override
    UnwrappedBindableVariable allowStaticFieldBinding();

    /**
     * Binds the variable to the {@code empty value} if not {@link #isRequired()}. Otherwise fails with
     * {@link RuntimeException} (ValueRequiredException) if a binding is required
     * <p>
     * What exactly none means depends on the underlying variable:
     *
     * Optional Class: Binds to {@link Optional#empty()}, {@link java.util.OptionalLong#empty()},
     * {@link java.util.OptionalDouble#empty()}, {@link java.util.OptionalInt#empty()} respectively none {@link Nullable},
     * {@link Optional}, Default value.
     *
     * Default Value: The default value as specified by the annotation
     *
     * Nullable: {@code null}
     *
     * <p>
     *
     * @throws UnsupportedOperationException
     *             if the underlying field does not support
     */
    void bindNone();

    // Problemet er vi gerne vil smide en god fejlmeddelse
    // Det kan man vel ogsaa...
    //// isOptional()->bindOptionallTo()
    //// else provide()
    // Will automatically handle, @Nullable, and Default

//    default void bindOpWithNoneAsNull(Op<?> op) {
//        bindOpWithNoneToken(op, null);
//    }

    // bindNoneableOp
    // bindNoneableOpWithToken
    // bindNoneableOpWithOptional

    // Wrapper er klassen som op returnere. Skal matche med Op
    // isEmpty (boolean, Op.returnValue) -> ifTrue -> none, otherwise uses valueExtractor
    // ValueExtractor(T, Op.returnValue)
    default void bindNoneableOp(Op<?> op, MethodHandle isEmptyTest, MethodHandle valueExtractor) {}

    // Op must have Optional, OptionalLong, OptionalInt or OptionalDouble as result
    default void bindNoneableOpWithOptional(Op<?> op) {}

    // Replace with Supplier<Throwable>? skal have denne version for all empty binders
    default void bindNoneableOpWithOptional(Op<?> op, Supplier<Throwable> throwing) {}

    /**
     * Uses identity
     * <p>
     * This method is typically called with {@code null} as the token.
     *
     * @param op
     * @param noneToken
     */
    default void bindNoneableOpWithToken(Op<?> op, @Nullable Object noneToken) {}

    void checkNotNoneable();

    /**
     * Checks that the unwrapped variable it not nullable, is not wrapped in an optional container, and is not annotated
     * with a default annotation.
     *
     * @throws app.packed.bean.BeanInstallationException
     *             if the unwrapped variable is not required
     */
    void checkNoneable();

    boolean hasDefaults();

    boolean isLazy();

    boolean isNullable();

    // isOptionalContainer
    boolean isOptional();

    boolean isProvider();

    /**
     * {@return whether or not the variable is noneable}
     * <p>
     * A variable is noneable if it is either:
     * <tt>
     * <li>Is nullable</li>
     * <li>Is wrapped in an optional container (Optional, OptionalLong, OptionalDouble, or OptionalInt)
     * <li>Is annotated with default annotation such as Default</li>
     * </tt>
     */
    boolean isNoneable();

    default boolean isWrapped() {
        return false;
    }

    // BindableVariable source()???
    Variable originalVariable();

    // Usecase???
    default boolean tryBindNone() {
        if (!isNoneable()) {
            bindNone();
            return true;
        }
        return false;
    }

    default List<Annotation> wrapperAnnotations() {
        return List.of();
    }

    /** {@return a list of all wrapper classes in applied order} */
    default List<Class<?>> wrapperClasses() {
        return List.of();
    }

    /**
    *
    */
    public enum WrapperKind {
        LAZY, OPTIONAL, PROVIDER;
    }
}