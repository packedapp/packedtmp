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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.application.LifetimeTemplate;

/**
 * A bean template is an immutable, reusable configuration object that defines the behavior of a bean. A template is
 * always specified when an extension installs a bean (on behalf of the user or another extension).
 * <p>
 * To install a new bean using a template, an extension can use either
 * {@link app.packed.extension.BaseExtensionPoint#newBean(BeanTemplate)} if installing a new bean on behalf of the user.
 * Or {@link app.packed.extension.BaseExtensionPoint#newBean(BeanTemplate, app.packed.extension.ExtensionPoint.UseSite)}
 * if installing a bean on behalf of another extension.
 * <p>
 *
 * A bean created using this template never has has any exposed {@link BeanHandle#lifetimeOperations() lifetime
 * operations}. As the lifetime of the bean is completely controlled by the container in which is installed into.
 * <p>
 * BeanKind.Unmanaged
 *
 * // An unmanaged bean will always return the bean instance.
 *
 * <p>
 * BeanKind.static
 *
 * * Represents a bean with no instances.
 * <p>
 * And hence no lifecycle then bean instance can go through.
 * <p>
 * The lifetime of the bean is identical to its container. A bean can never
 * <p>
 * A static bean never has any lifetime operations. And will fail with XX
 * <p>
 *
 * @see app.packed.extension.BaseExtensionPoint#newBean(BeanTemplate)
 * @see app.packed.extension.BaseExtensionPoint#newBean(BeanTemplate, app.packed.extension.ExtensionPoint.UseSite)
 */
public sealed interface BeanTemplate permits PackedBeanTemplate {

    BeanTemplate FUNCTIONAL = BeanKind.STATIC.template();

    // The bean is created by an operation
    // There are no lifetime operations on the bean
    // But the OperationHandle returned from BeanMethod.newLifetimeOperation.
    // Has a single operation that will create the bean.
    // TODO skal vi baade have managed og unmanged operationer???
    // Fx @Provide paa en prototypeBean (giver vel ikke mening)
    BeanTemplate GATEWAY = new PackedBeanTemplate(BeanKind.UNMANAGED);

    /** {@return a descriptor for this template} */
    BeanTemplate.Descriptor descriptor();

    /**
     * Reconfigures this bean template.
     *
     * @param action
     *            the reconfiguration action
     * @return the reconfigured bean template
     */
    BeanTemplate reconfigure(Consumer<? super Configurator> action);

    /**
     * Creates a new bean template.
     *
     * @param action
     *            the configuration action
     * @return the configured bean template
     */
    static BeanTemplate of(BeanKind kind, Consumer<? super Configurator> action) {
        return PackedBeanTemplate.reconfigureExisting(new PackedBeanTemplate(kind), action);
    }

    /**
     * A configuration object for configuring or reconfiguring a bean template.
     *
     * @see BeanTemplate#of(BeanKind, Consumer)
     * @see BeanTemplate#reconfigure(Consumer)
     */
    sealed interface Configurator permits PackedBeanTemplate.PackedBeanTemplateConfigurator {

        /**
         * Specifies the return type signature of the factory operation(s) that create the bean.
         * <p>
         * The return type of the lifetime operation that creates the bean is {@code Object.class} per default. In order to
         * better support {@link java.lang.invoke.MethodHandle#invokeExact(Object...)}. This method can be used to specify a
         * less generic type if needed.
         * <p>
         * If this template is used to install bean whose bean class is not assignable to the specified class. The framework
         * will throw a {@link app.packed.bean.BeanInstallationException}.
         * <p>
         * The method handle of the factory operation of the new template will always have the specified class as its
         * {@link java.lang.invoke.MethodType#returnType()}.
         *
         * @param clazz
         *            the return type of the method handle that creates the bean lifetime
         * @return a new template
         * @throws IllegalArgumentException
         *             if specifying a primitive type or {@code Void}
         * @throws UnsupportedOperationException
         *             if this template is not based on {@link #MANAGED} or {@link #UNMANAGED}
         * @see java.lang.invoke.MethodHandle#invokeExact(Object...)
         * @see java.lang.invoke.MethodType#changeReturnType(Class)
         */
        Configurator createAs(Class<?> clazz);

        /**
         * The creation MethodHandle will have the actual bean type as its return type.
         * <p>
         * Normally the return type is {@code Object.class} to allow for better interoperability with
         * {@link java.lang.invoke.MethodHandle#invokeExact(Object...)}.
         *
         * @return the new template
         *
         * @throws UnsupportedOperationException
         *             if bean kind is not {@link BeanKind#MANANGED} or {@link BeanKind#UNMANAGED}
         */
        Configurator createAsBeanClass();

        /**
         * Sets a context for the whole bean
         *
         * @param context
         *            the context
         * @return the new template
         */
        // Man skal vel angive hvordan context fungere.
        // Er den stored, eller skal den med til alle operation?
        default Configurator inContext(ContextTemplate context) {
            throw new UnsupportedOperationException();
        }

        /**
         * <p>
         * Use {@code ContextValue(BeanLifetimeOperationContext.class)} of the exact type
         * <p>
         * The context is only available for the extension that installed the bean
         * <p>
         * When returned the targeted lifetime operation will have been updated.
         * <p>
         * If this template contains multiple lifetime operations different contexts can be set.
         *
         * @param index
         *            the index of the lifetime operation. Must match an operation in {@link #lifetimeOperations()}.
         * @param argumentType
         *            the type of argument that will be taken and made available
         * @return the new template
         * @throws IndexOutOfBoundsException
         *             if the specified index does not match a lifetime operation
         * @throws IllegalArgumentException
         *             if the specified argument type is void
         * @see BeanLifetimeOperationContext
         * @see app.packed.extension.context.ContextValue
         */
        // Lifetime operationer kan koeres i en context
        Configurator inContextForLifetimeOperation(int index, ContextTemplate template);

        @SuppressWarnings("exports")
        Configurator lifetime(LifetimeTemplate lifetime);

        /**
         * Sets the default value of the specified bean local. This value will be applied every time the template is used to
         * install a bean.
         *
         * The value set by this method can be overridden when installing a specific bean by using
         * {@link Installer#setLocal(BeanLocal, Object)}.
         *
         * @param <T>
         *            the type of value
         * @param local
         *            the bean local to set
         * @param value
         *            the value to set
         * @return this configurator
         */
        <T> Configurator localSet(BeanLocal<T> local, T value);
    }

    /** A descriptor for a BeanTemplate. This class is mainly used for informational purposes. */
    // Alternativet er jo at vi ikke exposer det paa BeanMirrror...
    // Lige nu er descriptoren kun fordi vi vil holde det "hemmeligt" fx links
    sealed interface Descriptor permits PackedBeanTemplate.PackedBeanTemplateDescriptor {

        /** {@return the kind of bean the descriptor's template creates} */
        BeanKind beanKind();

        /** {@return the bean contexts} */
        Map<Class<?>, ContextTemplate.Descriptor> contexts();

        /**
         * <p>
         * Empty means create as bean class
         *
         * @return
         *
         * @see BeanTemplate#createAs(Class)
         * @see BeanTemplate#createAsBeanClass()
         */
        Optional<Class<?>> createAs();

        /** {@return a list of the various lifetime operations for the descriptor's template.} */
        List<OperationTemplate.Descriptor> lifetimeOperations();

        default Module module() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An installer for installing beans into a container.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     *
     * @see app.packed.extension.BaseExtensionPoint#newApplicationBean(BeanTemplate)
     * @see app.packed.extension.BaseExtensionPoint#newDependantExtensionBean(BeanTemplate,
     *      app.packed.extension.ExtensionPoint.UseSite)
     *
     * @apiNote The reason we have a Builder and not just 1 class. Is because of the bean scanning. Which makes it confusing
     *          which methods can be invoked before or only after the scanning
     */
    sealed interface Installer permits PackedBeanInstaller {

        /**
         * Installs the bean using the specified class as the bean source.
         * <p>
         * {@link BeanHandle#configuration()} returns the configuration that is created using the specified function
         *
         * @param <T>
         *            the bean class
         * @param beanClass
         *            the bean class
         * @param configurationCreator
         *            responsible for creating the configuration of the bean that is exposed to the end user.
         * @return a bean handle representing the installed bean
         *
         * @see app.packed.bean.BeanSourceKind#CLASS
         */
        <H extends BeanHandle<?>> H install(Class<?> beanClass, Function<? super BeanTemplate.Installer, H> factory);

        <H extends BeanHandle<?>> H install(Op<?> beanClass, Function<? super BeanTemplate.Installer, H> factory);

        // These things can never be multi
        // AbsentInstalledComponent(boolean wasInstalled)
        <H extends BeanHandle<T>, T extends BeanConfiguration> H installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
                Function<? super BeanTemplate.Installer, H> configurationCreator, Consumer<? super BeanHandle<?>> onNew);

        // instance = introspected bean
        // constant = non-introspected bean
        <H extends BeanHandle<?>> H installInstance(Object instance, Function<? super BeanTemplate.Installer, H> factory);

        /**
         * Creates a new bean without a source.
         *
         * @return a bean handle representing the new bean
         *
         * @throws IllegalStateException
         *             if this builder was created with a base template other than {@link BeanTemplate#STATIC}
         * @see app.packed.bean.BeanSourceKind#SOURCELESS
         */
        <H extends BeanHandle<?>> H installSourceless(Function<? super BeanTemplate.Installer, H> factory);

        Installer namePrefix(String prefix);

        /**
         * Sets the value of the specified bean local for the new bean.
         *
         * @param <T>
         *            the type of value the bean local holds
         * @param local
         *            the bean local to set
         * @param value
         *            the value of the local
         * @return this builder
         */
        <T> Installer setLocal(BeanLocal<T> local, T value);
    }

}

interface Sandbox {

    void ignoreAnnotations(Class<?> annot);

    void noScan();

    // The bean can never
    void proxyForbidden();

    // Ahh alt er raw
    default BeanTemplate raw() {
        return null;
    }

    /** {@return the allowed bean source kinds for.} */
    default EnumSet<BeanSourceKind> sourceKinds() {
        return EnumSet.allOf(BeanSourceKind.class);
    }

    /**
     * Marks the bean as synthetic.
     *
     * @return this installer
     */
    BeanTemplate.Installer synthetic(); // Maybe on template?

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

}
