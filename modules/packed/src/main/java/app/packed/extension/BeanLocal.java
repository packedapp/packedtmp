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
import internal.app.packed.bean.PackedBeanElement;
import internal.app.packed.bean.PackedBeanLocal;
import sandbox.extension.bean.BeanHandle;

/**
 * This class provides bean-local variables at build-time.
 * <p>
 * Bean locals are typically used for sharing per-bean data between various parts of the application while building it.
 * For example, a value can be set for a bean when installing it using XXX.
 *
 * This value can then retrieved from a bean handle
 *
 * There are number where a BeanLocal be used:
 *
 * Before creating a bean a value can be set using
 * {@link app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)}
 *
 *
 * Finally, a bean local can be used from a {@link app.packed.bean.BeanMirror}
 * <p>
 * While bean locals are primarily developers of extensions, there are no reasons that ordinary users could not use
 * them.
 * <p>
 * Bean locals should generally not be shared outside outside of trusted code.
 * <p>
 * Bean locals should in general only be used while building an application. Or for querying from bean mirror
 * subclasses. Specifically, there are no support for querying bean locals at runtime.
 *
 * @see app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)
 * @see app.packed.bean.BeanMirror#isLocalPresent(BeanLocal)
 * @see app.packed.bean.BeanMirror#getLocal(BeanLocal)
 * @see ContainerLocal
 */
@SuppressWarnings("rawtypes")
public sealed abstract class BeanLocal<T> permits PackedBeanLocal {

    public final T get(BeanConfiguration configuration) {
        return get(BeanSetup.crack(configuration));
    }

    // Is useful, if we delegate operation creation to other extensions
    // As they will not have access to BeanIntrospector
    public final T get(BeanElement element) {
        throw new UnsupportedOperationException();
    }

    public final T get(BeanHandle<?> handle) {
        return get(BeanSetup.crack(handle));
    }

    public final T get(BeanIntrospector introspector) {
        return get(introspector.bean());
    }

    /**
     * <p>
     * If this local has been created with an initial value supplier. The supplier will be used to initialize a value
     *
     * @param bean
     *            the bean to return a value from
     * @return the value of the bean local if set in the specified bean
     *
     * @throws java.util.NoSuchElementException
     *             if a value has not been set previously for the bean, and no initial value supplier was specified when
     *             creating the local
     */
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
    public final boolean isSet(BeanConfiguration configuration) {
        return isSet(BeanSetup.crack(configuration));
    }

    public final boolean isSet(BeanHandle<?> handle) {
        return isSet(BeanSetup.crack(handle));
    }

    public final boolean isSet(BeanIntrospector introspector) {
        return isSet(introspector.bean());
    }

    /**
     * <p>
     * Calling this method will not initialize the value of the local if no value has been explicitly set and a initial
     * value supplier was specified when creating the local.
     *
     * @param bean
     * @return whether or not a value has previously been expl
     */
    protected abstract boolean isSet(BeanSetup bean);

    protected abstract T orElse(BeanSetup bean, T other);

    // or throws the supplied
    public final <X extends Throwable> T orElseThrow(BeanConfiguration configuration, Supplier<? extends X> exceptionSupplier) throws X {
        if (!isSet(configuration)) {
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
     * Sets the value of this local for the bean represented by the specified bean element.
     *
     * @param <B>
     *            the type of bean element
     * @param handle
     *            the bean element that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean element
     */
    public final void set(BeanElement element, T value) {
        PackedBeanElement e = (PackedBeanElement) element;
        set(e.bean(), value);
        // return element;
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
     * Creates a bean local without any initial value supplier.
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
     * {@code Supplier}. If the specified supplier returns null, no initial value will be set.
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
