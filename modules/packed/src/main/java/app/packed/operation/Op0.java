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
 * A {@link Op} type that wraps a {@link Supplier} in order to dynamically provide new instances.
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Op<Long> f = new Op0<>(System::currentTimeMillis) {}};</pre>
 * <p>
 * In this example we create a new class inheriting from Op0 is order to capture information about the suppliers type
 * variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining type
 * information at runtime.
 * 
 * @param <R>
 *            the type of objects this factory constructs
 * @see Op1
 * @see Op2
 */
public abstract class Op0<R> extends CapturingOp<R> {

    /**
     * Creates a new factory, that use the specified supplier to provide values.
     *
     * @param supplier
     *            the supplier that will provide the actual values.
     * @throws IllegalArgumentException
     *             if the type variable R could not be determined.
     */
    protected Op0(Supplier<? extends R> supplier) {
        super(requireNonNull(supplier, "supplier is null"));
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
