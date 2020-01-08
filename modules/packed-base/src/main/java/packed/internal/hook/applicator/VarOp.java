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
package packed.internal.hook.applicator;

import java.lang.invoke.VarHandle;

/**
 *
 */

// Skal ikke vide noget om fields... eller Methods

public abstract class VarOp<T> {

    public abstract T apply(VarHandle handle);

    public abstract T apply(VarHandle handle, Object instance);

    // public VarOp<Supplier<?>> getter() {
    //
    // }
    //
    // static class SupplierInternalFieldOperation<T> extends VarOp<Supplier<T>> {
    // Class<?> fieldType;
    //
    // /** {@inheritDoc} */
    // @Override
    // public Supplier<T> invoke(MethodHandle mh) {
    // return new StaticSup<T>(mh);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public boolean isSimpleGetter() {
    // return true;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public Supplier<T> applyStaticHook(AnnotatedFieldHook<?> hook) {
    // return new StaticSup<T>(hook.getter());
    // }
    // }
    //
    // static class StaticSupplier<T> implements Supplier<T> {
    //
    // private final VarHandle vh;
    //
    // public StaticSupplier(VarHandle vh) {
    // this.vh = requireNonNull(vh);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public T get() {
    // try {
    // return (T) vh .invoke();
    // } catch (Throwable e) {
    // ThrowableUtil.rethrowErrorOrRuntimeException(e);
    // throw new UndeclaredThrowableException(e);
    // }
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public boolean isSimpleGetter() {
    // return true;
    // }
    // }

}
