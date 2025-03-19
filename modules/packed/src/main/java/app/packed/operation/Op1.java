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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * An {@link Op} type that wraps a {@link Function} taking  a single argument.
 * <p>
 * Can be used like this:
 *
 * <pre> {@code
 * Op<Long> op = new Op0<>(System::currentTimeMillis) {}};</pre>
 * <p>
 * In this example we create an anonymous class inheriting from Op0 in order to capture information about the suppliers
 * type variable (in this case {@code Long}).
 *
 * An {@link Op} type that wraps a {@link Function} taking a single argument.
 * <p>
 * Is typically used like this:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(new Factory1<>(System::currentTimeMillis).setDescription("Startup Time"){};}
 * </pre>
 * <p>
 * You can also use annotations on the dependency's type parameter. For example, say you have a {@link Qualifier} that
 * can read system properties, you can use something like this:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(new FunctionFactory<@SystemProperty("threads") Integer, ExecutorService>(t -> Executors.newFixedThreadPool(t)){});}
 * </pre>
 *
 * A special {@link Op} type that wraps a {@link Supplier} in order to dynamically provide new instances.
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Op<Long> f = new Op1<>(System::currentTimeMillis) {}};</pre>
 * <p>
 * In this example we create a new class inheriting from Factory0 is order to capture information about the suppliers
 * type variable (in this case {@code Long}).
 *
 * @param <T>
 *            The type of the argument the op takes
 * @param <R>
 *            the type of objects the op returns
 * @see Op0
 * @see Op2
 */
public abstract class Op1<T, R> extends CapturingOp<R> {

    /**
     * Creates a new op, that use the specified function to provide values.
     *
     * @param function
     *            the function that will provide values for the op.
     * @throws IllegalArgumentException
     *             if the type variable T or R could not be determined.
     */
    protected Op1(Function<? super T, ? extends R> function) {
        super(requireNonNull(function, "function is null"));
    }
}
