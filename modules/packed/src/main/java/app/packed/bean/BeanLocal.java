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
package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.extension.BeanElement;
import app.packed.extension.BeanIntrospector;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanElement;
import internal.app.packed.container.PackedLocal;
import sandbox.extension.bean.BeanHandle;

/**
 * This class provides build-time bean-local variables primarily for use by {@link app.packed.extension.Extension
 * extensions}.
 * <p>
 * Bean locals are typically used for sharing per-bean data between various parts of the application while the
 * application is being built. For example, a value can be set for a bean when installing it. And then later be
 * retrieved from a bean mirror instance. The {@link LocalAccessor} interfaces details all the entities that supports
 * bean local storage in its permit clause.
 * <p>
 * While bean locals are primarily developers of extensions, power users are free to use them in any way they want.
 * <p>
 * Bean locals should generally not be shared outside outside of trusted code.
 * <p>
 * Bean locals should in general only be used while building an application. Or for querying from bean mirror
 * subclasses. Specifically, there are no support for querying bean locals at runtime.
 *
 * @see app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)
 * @see ContainerLocal
 */
public final class BeanLocal<T> extends PackedLocal<T> {

    private BeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /**
     * Returns the bean local value from the specified accessor.
     * <p>
     * If no value has been set for this local previously, this method will:
     * <ul>
     * <li>Initialize lazily, if an initial value supplier was used when creating this local.</li>
     * <li>Fail with {@link java.util.NoSuchElementException}, if no initial value supplier was used when creating this
     * local.</li>
     * </ul>
     *
     * @param accessor
     *            the bean local accessor
     * @return the value of the bean local
     *
     * @throws java.util.NoSuchElementException
     *             if a value has not been set previously for this bean local and an initial value supplier was not
     *             specified when creating the bean local
     */
    public T get(LocalAccessor accessor) {
        BeanSetup bean = crack(accessor);
        return bean.locals().get(this, bean);
    }

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if a value is present
     * @throws NullPointerException
     *             if value is present and the given action is {@code null}
     */
    public void ifPresent(LocalAccessor accessor, Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not a value has been set or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    public boolean isSet(LocalAccessor accessor) {
        BeanSetup bean = crack(accessor);
        return bean.locals().isSet(this, bean);
    }

    /**
     * Returns whether or not a value has been set or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @param other
     *            the value to return if a value has not been set previously
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    public T orElse(LocalAccessor accessor, T other) {
        BeanSetup bean = crack(accessor);
        return bean.locals().orElse(this, bean, other);
    }

    public <X extends Throwable> T orElseThrow(LocalAccessor accessor, Supplier<? extends X> exceptionSupplier) throws X {
        BeanSetup bean = crack(accessor);
        return bean.locals().orElseThrow(this, bean, exceptionSupplier);
    }

    /**
     * Sets the value of this local for the specified bean.
     *
     * @param bean
     *            the bean
     * @param value
     *            the value to set
     */
    public void set(LocalAccessor accessor, T value) {
        BeanSetup bean = crack(accessor);
        bean.locals().set(this, bean, value);
    }

    /**
     * Extracts the actual bean setup from the specified accessor.
     *
     * @param accessor
     *            the accessor to extract from
     * @return the extracted bean
     */
    private static BeanSetup crack(LocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        if (accessor instanceof BeanConfiguration bc) {
            return BeanSetup.crack(bc);
        } else if (accessor instanceof PackedBeanElement bc) {
            return bc.bean();
        } else if (accessor instanceof BeanHandle<?> bc) {
            return BeanSetup.crack(bc);
        } else if (accessor instanceof BeanIntrospector bc) {
            return BeanSetup.crack(bc);
        } else if (accessor instanceof BeanMirror bc) {
            return bc.bean;
        } else {
            throw new Error();
        }
    }

    /**
     * Creates a new bean local without any initial value supplier.
     *
     * @param <T>
     *            the type of the bean local's value
     * @return a new bean local
     */
    public static <T> BeanLocal<T> of() {
        return new BeanLocal<>(null);
    }

    /**
     * Creates a new bean local. The initial value is determined by invoking the {@code get} method on the specified
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
        requireNonNull(initialValueSupplier);
        return new BeanLocal<>(initialValueSupplier);
    }

    /** An entity where bean local values can be stored and retrieved. */
    @SuppressWarnings("rawtypes")
    public sealed interface LocalAccessor permits BeanConfiguration, BeanElement, BeanHandle, BeanIntrospector, BeanMirror {}
}

//https://docs.oracle.com/en/java/javase/20/docs/api/jdk.incubator.concurrent/jdk/incubator/concurrent/ScopedValue.html#get()
