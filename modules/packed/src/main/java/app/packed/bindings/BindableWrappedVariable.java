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
package app.packed.bindings;

import java.util.List;
import java.util.Optional;

import app.packed.operation.Op;

/**
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
 */
public interface BindableWrappedVariable extends BindableVariable {

    @Override
    BindableWrappedVariable allowStaticFieldBinding();

    /**
     * Binds to {@link Nullable}, {@link Optional}, Default value.
     *
     * <p>
     * For raw er det automatisk en fejl
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
    default void bindToOptional(Op<Optional<?>> op) {}

    default void bindToOptional(Op<Optional<?>> op, Runnable missing) {}

    void checkNotRequired();

    void checkRequired();

    boolean hasDefaults();

    boolean isLazy();

    boolean isNullable();

    boolean isOptional();

    boolean isProvider();

    boolean isRequired();

    Variable originalVariable();

    default List<Class<?>> wrapperClasses() {
        return List.of();
    }

    default boolean isWrapped() {
        return false;
    }

    default boolean tryBindNone() {
        return false;
    }

    /**
    *
    */
   public enum WrapperKind {
       OPTIONAL, PROVIDER, LAZY;
   }
}