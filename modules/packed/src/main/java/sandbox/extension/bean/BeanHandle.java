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
package sandbox.extension.bean;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocalAccessor;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.component.Authority;
import app.packed.component.ComponentHandle;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.util.Key;
import internal.app.packed.bean.PackedBeanHandle;
import internal.app.packed.context.publish.ContextualizedElement;
import sandbox.extension.operation.OperationHandle;

/**
 * A bean handle is a build-time reference to an installed bean. Typically they are returned by the framework when an
 * extension installs a bean on behalf of another extension or the user.
 * <p>
 * Instances of {@code BeanHandle} should not be exposed outside of the extension that created the bean. Instead the
 * extension should expose instances of {@link #configuration() bean configuration} to the other extension or user.
 */
public sealed interface BeanHandle<C extends BeanConfiguration> extends ComponentHandle , ContextualizedElement , BeanLocalAccessor permits PackedBeanHandle {

    // Or a Bean Service???

    /**
     * Adds a "service"
     *
     * @param <K>
     * @param key
     * @param supplier
     */
    // Is lazy, or eager?
    // Eager_never_fail, Eager_fail_if_not_used, Lazy_whenFirstUsed, LazyFailIfNotUsed, Some default for the container?
    <K> void addComputedConstant(Key<K> key, Supplier<? extends K> supplier);

    /** {@return the bean class.} */
    Class<?> beanClass();

    /** {@return the bean kind.} */
    BeanKind beanKind();

    /** {@return the bean source kind.} */
    BeanSourceKind beanSourceKind();

    // Primarily specified by the user and for used for building or mirrors
    void componentTags(String... tags);

    /** {@return the bean's configuration} */
    C configuration();

    /**
     * Returns the key that the bean will be made available under as default if provided as service.
     *
     * @return the default key
     * @throws UnsupportedOperationException
     *             if called on a bean that has a void bean class
     *
     * @see app.packed.service.ServiceableBeanConfiguration#defaultKey()
     * @see #exportAs(Key)
     * @see #provideAs(Key)
     */
    default Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("This method is not supported for beans with a void bean class");
        }
        return Key.fromClass(beanClass());
    }

    default void exportAs(Class<?> key) {
        exportAs(Key.of(key));
    }

    /**
     * Exports the bean as a service with the specified key.
     *
     * @param key
     *            the key to export the bean a
     * @throws ClassCastException
     *             if the specified key is not assignable to the type of the underlying bean
     * @throws KeyAlreadyInUseException
     *             if another service is already exported for the specified key
     * @see #defaultKey()
     * @see #serviceProvideAs(Key)
     */
    void exportAs(Key<?> key);

    /**
     * Returns a list of operation handles that corresponds to the {@link BeanTemplate#lifetimeOperations() lifetime
     * operations} on the template that created the bean.
     *
     * @return a list of lifetime operation handles
     *
     * @see BeanTemplate#lifetimeOperations()
     * @see BeanInstaller#lifetimes(app.packed.operation.OperationTemplate...)
     */
    List<OperationHandle> lifetimeOperations();

    /**
     * Sets the name of the bean, overriding any existing name.
     *
     * @param name
     *            the name of the bean
     * @throws IllegalArgumentException
     *             if another bean with the specified name already exists
     */
    void named(String name);

    /**
     * Prepares t
     *
     * @param function
     *            the user defined function that should be invoked
     * @return a new operation handle handle
     * @throws IllegalArgumentException
     *             if the specified function does not implement a functional interface
     */
    // Skal vel ogsaa have en template her...
    default OperationHandle.Builder newFunctionalOperation(Object function) {
        // De giver faktisk ret god mening at tage funktionen nu
        // Det er jo ligesom at BeanMethod giver metoden videre til builderen
        throw new UnsupportedOperationException();
    }

    /** {@return the owner of the bean} */
    Authority owner();

    /**
     * Provides of the bean as a service.
     * <p>
     * This method is typically not called directly, but instead via the various provide methods on
     * {@link ProvideableBeanConfiguration}.
     *
     * @param key
     *            the key to provide the service as
     * @throws ClassCastException
     *             if the specified key is not assignable to the type of the underlying bean
     * @throws KeyAlreadyInUseException
     *             if another service is already provided for the specified key
     * @throws UnsupportedOperationException
     *             if bean cannot be provided as a service. Need a new exception
     * @see #defaultKey()
     * @see ProvideableBeanConfiguration#provide()
     * @see ProvideableBeanConfiguration#provideAs(Class)
     * @see ProvideableBeanConfiguration#provideAs(Key)
     */
    void provideAs(Key<?> key);
}

// Operations
// Call an operation at some point in the lifecycle
//// Take other (extension) bean instance
//// Take
// Replace instance after creation

interface Zandbox<T> {
    OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation);

    // onClientInitialize
    default <B> void afterInitialize(InstanceBeanConfiguration<B> extensionBean, BiConsumer<? super B, ? super T> consumer) {
        //// Ideen er at man fx kan have en BeanHandle<Driver>.onInitialize(MyEBC, (b,p)->b.drivers[i]=p);
        //// Skal godt nok passe paa med capturing her... Ellers faar man hele application setup med i lambdaen

        // Alternativt kan man have noget foo(@SPIInject MyInterface[] mi)..
        // Og saa have registreret interfacet inde
        // Maaske den her bare er bedre...

        // state = Initialized? eller Initializing
        // Take runState???
    }

    void decorateInstance(Function<? super T, ? extends T> decorator);

    <B> void onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super T> consumer);

    default Class<? extends Extension<?>> operator() {
        return BaseExtension.class;
    }

    <K> OperationHandle overrideService(Key<K> key, K instance);

    /**
     * @param consumer
     * @throws UnsupportedOperationException
     *             if the bean does not have or creates any instances
     */
    // Vi har ikke laengere typen, saa maaske bare MH
    // Eller ogsaa skal vi tage en class
    void peekInstance(Consumer<? super T> consumer);

    // (BeanClass) void
    void peekInstance(MethodHandle methodHandle);

    // Altssa, er det ikke en bare en
    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    void setErrorHandler(ErrorHandler errorHandler);
}
