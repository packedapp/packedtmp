/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;

/**
 * A {@link Op} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input to the
 * bi-function being the two dependencies.
 *
 *
 *
 * @param <T>
 *            The type of the first argument this op takes
 * @param <U>
 *            The type of the second argument this op takes
 * @param <R>
 *            the type of objects this op returns
 * @see Op0
 * @see Op2
 */
public abstract class Op2<T, U, R> extends CapturingOp<R> {

    /**
     * Creates a new op, that use the specified function to provide values.
     *
     * @param function
     *            the function that will provide values for the op.
     * @throws IllegalArgumentException
     *             if the type variable T, U or R could not be determined.
     */
    protected Op2(BiFunction<? super T, ? super U, ? extends R> function) {
        super(requireNonNull(function, "function is null"));
    }
}
