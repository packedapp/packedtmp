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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import app.packed.base.TypeLiteral;
import packed.internal.methodhandle.LookupUtil;

/** A function that maps the result of another function. */
public final class MappingFactoryHandle<T, R> extends FactoryHandle<R> {

    /** A method handle for {@link Function#apply(Object)}. */
    private static final MethodHandle APPLY = LookupUtil.lookupVirtualPublic(Function.class, "apply", Object.class, Object.class);

    /** The function we map the result from. */
    final FactoryHandle<T> mapFrom;

    /** The function used to map the result. */
    final Function<? super T, ? extends R> mapper;

    public MappingFactoryHandle(TypeLiteral<R> typeLiteral, FactoryHandle<T> mapFrom, Function<? super T, ? extends R> mapper) {
        super(typeLiteral);
        this.mapFrom = requireNonNull(mapFrom, "mapFrom is null");
        this.mapper = requireNonNull(mapper, "mapper is null");
    }

    public static void main(String[] args) throws Throwable {
        MethodHandle c = MethodHandles.constant(Object.class, 5);

        Function<Integer, Integer> ff = i -> i + 1;

        MethodHandle apply = LookupUtil.lookupVirtualPublic(Function.class, "apply", Object.class, Object.class);
        MethodHandle fu = apply.bindTo(ff);

        c = c.asType(c.type().changeReturnType(Object.class));
        System.out.println(MethodHandles.foldArguments(fu, c).type());

        // Skal vel vaere en filter operation istedet for folding

    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle() {
        MethodHandle mf = mapFrom.toMethodHandle();

        // Change return type to Object so the method handle for the function will accept it
        mf = mf.asType(mf.type().changeReturnType(Object.class));
        return MethodHandles.foldArguments(APPLY.bindTo(mapper), mf);
    }

    /** {@inheritDoc} */
    @Override
    public MethodType methodType() {
        throw new UnsupportedOperationException();
    }
}
