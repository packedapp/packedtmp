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

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.inject.Factory;
import app.packed.operation.OperationConfiguration;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.bean.PackedBeanHandler;

/**
 * A bean driver must be created via {@link BeanExtensionPoint}.
 */
// INFO (type, kind)

// BeanCustomizer?? Syntes maaske handle er lidt mere runtime
// BeanMaker??

// Operations (add synthetic/functional)

// Sidecars (add)

// Lifecycle (Custom Factory, InvocationConfigurations)

// Services (bind, bindContext)

// callacbks, onBound, onBuild, ...

// Vigtigt at note... Vi scanner klassen naar vi kalder build()
// Saa hvis vi provider services/contexts saa skal det vaere paa
// builderen ellers er de ikke klar til BeanField og friends
// som jo bliver lavet naar man invoker build()

// BeanExtensor?? OperationExtensor, ContainerExtensor, InterceptorExtensor
@SuppressWarnings("rawtypes")
public sealed interface BeanHandler<T> permits PackedBeanHandler {

    default <F> OperationConfiguration addFunctionalOperation(Class<F> tt, F function) {
        throw new UnsupportedOperationException();
    }

    OperationConfiguration functionalOperationBuilder(Object functionInstance);

    default OperationConfiguration functionalOperationBuilder(TypeToken<?> tt, Object functionInstance) {
        throw new UnsupportedOperationException();
    }

    // Hvis vi aabner op for specialized bean mirrors
    // maybe just name it mirror?
    default void addMirror(Supplier<? extends BeanMirror> mirrorFactory) {}

    default OperationConfiguration addSyntheticOperation(MethodHandle methodHandle, boolean firstParamIsBeanInstance) {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers a wiring action to run when the bean becomes fully wired.
     * 
     * @param action
     *            a {@code Runnable} to invoke when the bean is wired
     */
    BeanHandler<T> addWiringAction(Runnable action);

    /**
     * @return
     * 
     * @see BeanConfiguration#beanClass()
     * @see BeanMirror#beanClass()
     */
    Class<?> beanClass(); // beanSource instead??

    /**
     * @param decorator
     */
    void decorateInstance(Function<? super T, ? extends T> decorator);

    /**
     * @return
     * 
     * @throws UnsupportedOperationException
     *             if called on a bean with void.class beanKind
     */
    Key<?> defaultKey();

    boolean isConfigurable();

    default boolean isCurrent() {
        return false;
    }

    /**
     * @param consumer
     */
    // giver den plus decorate mening?
    void peekInstance(Consumer<? super T> consumer);

    /**
     * A builder for {@link BeanHandler}. Can only be created via the various {@code newBuilder} methods on
     * {@link BeanExtensionPoint}.
     * 
     * @see BeanExtensionPoint#beanBuilder(BeanKind)
     * @see BeanExtensionPoint#beanBuilderFromClass(BeanKind, Class)
     * @see BeanExtensionPoint#beanBuilderFromFactory(BeanKind, Factory)
     * @see BeanExtensionPoint#beanBuilderFromInstance(BeanKind, Object)
     */
    // Could also have, scan(), scan(BeanScanner), noScan() instead of build()
    sealed interface Builder<T> permits PackedBeanHandleBuilder {

        /**
         * @param scanner
         * @return
         * 
         * @throws UnsupportedOperationException
         *             if called on a bean of kind functional
         */
        Builder<T> beanScanner(BeanProcessor scanner);

        // Scan (disable, do scan) ???

        /**
         * {@return the bean class.}
         * 
         * @see BeanConfiguration#beanClass()
         * @see BeanMirror#beanClass()
         */
        Class<?> beanClass();

        /**
         * Adds a new bean to the container and returns a handle for it.
         * 
         * @return the new handle
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        BeanHandler<T> build();

        /**
         * Sets a prefix that is used for naming the bean (This can always be overridden by the user).
         * <p>
         * If there are no other beans with the same name (for same parent container) when creating the bean. Packed will use
         * the specified prefix as the name of the bean. Otherwise, it will append a postfix to specified prefix in such a way
         * that the name of the bean is unique.
         * 
         * @param prefix
         *            the prefix used for naming the bean
         * @return this builder
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        default Builder<T> namePrefix(String prefix) {
            return this;
        }

        /**
         * Marks the bean as owned by the extension representing by specified extension point context
         * 
         * @param context
         *            an extension point context representing the extension that owns the bean
         * @return this builder
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        Builder<T> ownedBy(UseSite context);
    }
}

//default OperationHandle addOperation(OperationDriver driver) {
//  throw new UnsupportedOperationException();
//}
//
//default void addOperation(OperationDriver2 driver) {
//  throw new UnsupportedOperationException();
//}
