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

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.BeanSetup.BeanInstallOption;

/**
 * A bean handle represents the private configuration of a bean.
 * <p>
 * Instances of {@code BeanHandle} are never exposed directly to end-users. Instead they are returned wrapped in
 * {@link BeanConfiguration} or a subclass hereof.
 */
public final /* primitive */ class BeanHandle<T> {

    /** The configuration of the bean we are wrapping. */
    final BeanSetup bean;

    /**
     * Creates a new BeanHandle.
     * 
     * @param bean
     *            the configuration of the bean we wrap
     */
    BeanHandle(BeanSetup bean) {
        this.bean = requireNonNull(bean);
    }

    // We need a extension bean
    public OperationHandle addFunctionalOperation(ExtensionBeanConfiguration<?> operator, Class<?> functionalInterface, OperationType type,
            Object functionInstance) {
        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    public OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, MethodHandle methodHandle) {
        return addOperation(operator, Op.ofMethodHandle(methodHandle));
    }

    public OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, Op<?> operation) {
        throw new UnsupportedOperationException();
    }

    /** {@return the bean class.} */
    public Class<?> beanClass() {
        return bean.beanClass;
    }

    /** {@return the bean kind.} */
    public BeanKind beanKind() {
        return bean.beanKind;
    }

    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the key that the bean will be made available under if provided.
     * 
     * @return
     * 
     * @see ProvideableBeanConfiguration#provide()
     * @see ProvideableBeanConfiguration#export()
     * @throws UnsupportedOperationException
     *             if called on a functional bean {@code (beanClass == void.class)}
     */
    public Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("Keys are not support for void bean classes");
        }
        return Key.of(beanClass());
    }

    /**
     * Returns whether or not the bean is still configurable.
     * 
     * @return {@code true} if the bean is still configurable
     */
    public boolean isConfigurable() {
        return !bean.realm.isClosed();
    }

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#computeMethodHandleInvoker()}. Otherwise a build exception will be thrown. Maybe this goes for
     * all operation customizers.
     * 
     * @return
     */
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
     * 
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    public void specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        bean.mirrorSupplier = supplier;
    }

    /** Various install options that can be provided when creating a {@link BeanHandle}. */
    public sealed interface InstallOption permits BeanSetup.BeanInstallOption {

        /**
         * An option that allows for a special bean introspector to be used when introspecting the bean for the extension.
         * Normally, the runtime would call {@link Extension#newBeanIntrospector} to obtain an introspector for the registering
         * extension.
         * 
         * @param introspector
         *            the introspector to use
         * @return the option
         * @see Extension#newBeanIntrospector
         */
        static InstallOption introspectWith(BeanIntrospector introspector) {
            requireNonNull(introspector, "introspector is null");
            return new BeanInstallOption.IntrospectWith(introspector);
        }

        /**
         * An option that allows for multiple beans of the same type in a single container.
         * <p>
         * By default, a container only allows a single bean of particular type if non-void.
         * <p>
         * Beans of kind {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC} does not support this option.
         * {@link IllegalArgumentException} is thrown if this option is specified for such beans.
         * 
         * @return the option
         */
        static InstallOption multiInstall() {
            return new BeanInstallOption.MultiInstall();
        }

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
        static InstallOption namePrefix(String prefix) {
            return new BeanInstallOption.CustomPrefix(prefix);
        }

        static InstallOption spawnNew() {
            // A bean that is created per operation.
            // Obvious manyton, but should we have own kind?
            // I actually think so because, because for now it always requires manyton

            // Some questions, do we support @Schedule? Or anything like it?
            // I don't think we need to set up the support for it by default. Only if used
            // So overhead is not needed

            // But I think those annotations that make sense are always "callback" extensions
            // From other threads
            // Single threaded vs multi-threaded
            // If we are single threaded it is obviously always only the request method
            // If we are multi threaded we create own little "world"
            // I think that is the difference, between the two

            // Maybe bean is always single threaded.
            // And container is always multi threaded

            throw new UnsupportedOperationException();
        }

        static InstallOption synthetic() {
            throw new UnsupportedOperationException();
        }
    }
}

interface SandboxBH<T> {

}
