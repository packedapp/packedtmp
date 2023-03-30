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

import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanLocal;
import sandbox.extension.bean.BeanHandle;

/**
 * This class provides bean-local variables. Think of them as {@link ThreadLocal thread locals}, but for a single bean.
 * <p>
 * Bean locals are typically used for sharing per-bean data between different components while building the application.
 *
 * There are number where a BeanLocal be used:
 *
 * Before creating a bean a value can be set using
 * {@link app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)}
 *
 *
 * Finally, a bean local can be used from a {@link app.packed.bean.BeanMirror}
 * <p>
 * While bean locals are primarily targeted extension developers, there are no reasons that power users could not use
 * them.
 * <p>
 * Bean locals should generally not be shared outside of the extension that created them.
 * <p>
 * Bean locals are only intended to be used when building an application or from bean mirror subclasses. Specifically,
 * there are no support for querying a bean local at runtime.
 *
 * @see app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)
 * @see app.packed.bean.BeanMirror#isLocalPresent(BeanLocal)
 * @see app.packed.bean.BeanMirror#getLocal(BeanLocal)
 * @see ContainerLocal
 */

// Tilfoej BeanElement, BeanMirror?

// Jeg ved ikke om vi vil have en default vaerdi

// get, use, remove..
@SuppressWarnings("rawtypes")
public sealed abstract class BeanLocal<T> permits ContainerLocal, PackedBeanLocal {

    public final T get(BeanConfiguration configuration) {
        return get(BeanSetup.crack(configuration));
    }

    public final T get(BeanHandle<?> handle) {
        return get(BeanSetup.crack(handle));
    }

    public final T get(BeanIntrospector introspector) {
        return get(introspector.bean());
    }

    protected abstract T get(BeanSetup bean);

    /**
     * In the bean represented by the specified configuration
     *
     * @param configuration
     *            the bean's configuration
     * @return whether or not a value has been set in the local for the configuraiton
     *
     * @throws UnsupportedOperationException
     *             if the bean local has an initial value. As this is always a usage error
     */
    public final boolean isPresent(BeanConfiguration configuration) {
        return isPresent(BeanSetup.crack(configuration));
    }

    public final boolean isPresent(BeanHandle<?> handle) {
        return isPresent(BeanSetup.crack(handle));
    }

    public final boolean isPresent(BeanIntrospector introspector) {
        return isPresent(introspector.bean());
    }

    protected abstract boolean isPresent(BeanSetup bean);

    // or throws the supplied
    public final <X extends Throwable> T orElseThrow(BeanConfiguration configuration, Supplier<? extends X> exceptionSupplier) throws X {
        if (!isPresent(configuration)) {
            throw exceptionSupplier.get();
        } else {
            return null;
        }
    }

    /**
     * Sets the value of this local for the bean represented by the specified bean introspector.
     *
     * @param <B>
     *            the type of bean introspector
     * @param handle
     *            the bean introspector that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean introspector
     */
    public final <B extends BeanElement> B set(B element, T value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the value of this local for the bean represented by the specified bean introspector.
     *
     * @param <B>
     *            the type of bean introspector
     * @param handle
     *            the bean introspector that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean introspector
     */
    public final <B extends BeanIntrospector> B set(B introspector, T value) {
        set(introspector.bean(), value);
        return introspector;
    }

    /**
     * Sets the value of this local for the bean represented by the specified bean configuration.
     *
     * @param <B>
     *            the type of bean configuration
     * @param handle
     *            the bean configuration that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean configuration
     */
    public final <B extends BeanConfiguration> B set(B configuration, T value) {
        set(BeanSetup.crack(configuration), value);
        return configuration;
    }

    /**
     * Sets the value of this local for the bean represented by the specified bean handle.
     *
     * @param <B>
     *            the type of bean handle
     * @param handle
     *            the bean handle that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean handle
     */
    public final <B extends BeanHandle<?>> B set(B handle, T value) {
        set(BeanSetup.crack(handle), value);
        return handle;
    }

    /**
     * Sets the value of this local for the specified bean.
     *
     * @param bean
     *            the bean
     * @param value
     *            the value to set
     */
    protected abstract void set(BeanSetup bean, T value);

    /**
     * Creates a bean local without any initial value.
     *
     * @param <T>
     *            the type of the bean local's value
     * @return a new bean local
     */
    public static <T> BeanLocal<T> of() {
        return PackedBeanLocal.of(null);
    }

    /**
     * Creates a bean local. The initial value is determined by invoking the {@code get} method on the specified
     * {@code Supplier}.
     *
     * @param <T>
     *            the type of the bean local's value
     * @param initialValueSupplier
     *            a supplier used to determine the initial value
     * @return a new bean local
     *
     */
    public static <T> BeanLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        return PackedBeanLocal.of(initialValueSupplier);
    }
}
//Makes no sense to have mutable operations on BeanMirror

//boolean hasInitialValue(), you should never need to query it
