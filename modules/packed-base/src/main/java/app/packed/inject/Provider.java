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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.stream.Stream;

import packed.internal.util.ThrowableUtil;

/**
 * A provider of instances.
 * 
 * @param <T>
 *            the type of instances that are provided
 * @apiNote In the future, if the Java language permits, {@link Provider} may become a {@code sealed} interface, which
 *          would prohibit subclassing except by explicitly permitted types.
 */
// Previously this interface also contained information about where
// the instances came from. However, this information is now only
// available from InjectionContext
public interface Provider<T> {

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided value
     * @throws RuntimeException
     *             if an exception is encountered while providing an instance
     */
    T provide();

    /**
     * Returns an infinite stream of instances.
     * 
     * @return an infinite stream of instances
     */
    default Stream<T> stream() {
        return Stream.generate(() -> provide());
    }

    /**
     * @param <T>
     *            the type of the specified instance
     * @param constant
     *            the constant
     * @return a new provider that provides the specified constant everytime
     */
    static <T> Provider<T> of(T constant) {
        return new ConstantProvider<>(constant);
    }
}

//En gang inkludere dette interface ogsaa informationen om hvor T kom fra.
//Det er nu blevet flyttet til InjectionContext, da der er ingen grund
//til at have den information 2 steder...

//isConstant??? Saa er vi ikke laengere et function interface though
class InvokeExactProvider<T> implements Provider<T> {

    private final MethodHandle mh;

    InvokeExactProvider(MethodHandle mh) {
        this.mh = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public T provide() {
        try {
            return (T) mh.invokeExact();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
}

class ConstantProvider<T> implements Provider<T> {

    private final T t;

    public ConstantProvider(T t) {
        this.t = requireNonNull(t);
    }

    /** {@inheritDoc} */
    @Override
    public T provide() {
        return t;
    }
}