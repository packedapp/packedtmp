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

import java.util.function.Supplier;

/**
 * An {@link Op} type that wraps a {@link Supplier} taking no arguments.
 * <p>
 * Can be used like this:
 *
 * <pre> {@code
 * Op<Long> f = new Op0<>(System::currentTimeMillis) {}};</pre>
 * <p>
 * In this example we create an anonymous class inheriting from Op0 in order to capture information about the suppliers
 * type variable (in this case {@code Long}).
 *
 * @param <R>
 *            the type of objects this op returns
 * @see Op1
 * @see Op2
 */
public abstract class Op0<R> extends CapturingOp<R> {

    /**
     * Creates a new op, that use the specified function to provide values.
     *
     * @param function
     *            the function that will provide values for the op.
     * @throws IllegalArgumentException
     *             if the type variable R could not be determined.
     */
    protected Op0(Supplier<? extends R> function) {
        super(requireNonNull(function, "function is null"));
    }
}
//
//static <R> Op0<R> ofInstancex(R instance) {
//  throw new UnsupportedOperationException();
//}
//
//static <R> Op0<R> ofInstance(Class<R> r, R instance) {
//  throw new UnsupportedOperationException();
//}
//
//static <R> Op0<R> ofInstance(VarToken<R> r, R instance) {
//  throw new UnsupportedOperationException();
//}
//
//static <R> Op0<R> of(Class<R> key, Supplier<R> supplier) {
//  throw new UnsupportedOperationException();
//}
//
//// Given ikke mening at have baade Variable og VarToken... Eller maaske goer det...
//// Maaske er VarToken implements Variable
//static <R> Op0<R> of(VarToken<R> r, Supplier<R> supplier) {
//  new VarToken<@Nullable List<String>>() {};
//  throw new UnsupportedOperationException();
//}
