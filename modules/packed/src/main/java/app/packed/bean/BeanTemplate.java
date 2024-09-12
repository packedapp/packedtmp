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
import java.util.function.Supplier;

import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.application.LifetimeTemplate;
import sandbox.extension.operation.OperationTemplate;

/**
 * A bean template is a immutable reusable configuration object that defines how a specific bean should behave. A
 * template must always be specified when an extension installs a bean on behalf of the user or another extension.
 * <p>
 * using when installating new beans using {@link app.packed.extension.BaseExtensionPoint#beanBuilder(BeanTemplate)} or
 * {@link app.packed.extension.BaseExtensionPoint#beanInstallerForExtension(BeanTemplate, app.packed.extension.ExtensionPoint.UseSite)}.
 *
 * <p>
 * In most cases Ideen er man started med en af de foruddefinered templates, og saa laver man modification
 *
 *
 * <p>
 * BeanKind.Container
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
     * Reconfigures this template.
     *
     * @param configure
     *            the action
     * @return the reconfigured template
     */
    BeanTemplate reconfigure(Consumer<? super Configurator> configure);

    static BeanTemplate of(BeanKind kind, Consumer<? super Configurator> configure) {
        return PackedBeanTemplate.configure(new PackedBeanTemplate(kind), configure);
    }

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
        <T extends BeanConfiguration> BeanHandle<T> install(Class<?> beanClass, Function<? super BeanTemplate.Installer, T> configurationCreator);

        <T extends BeanConfiguration> BeanHandle<T> install(Op<?> beanClass, Function<? super BeanTemplate.Installer, T> configurationCreator);

        // These things can never be multi
        // AbsentInstalledComponent(boolean wasInstalled)
        <T extends BeanConfiguration> BeanHandle<T> installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
                Function<? super BeanTemplate.Installer, T> configurationCreator, Consumer<? super BeanHandle<?>> onNew);

        // instance = introspected bean
        // constant = non-introspected bean
        <T extends BeanConfiguration> BeanHandle<T> installInstance(Object instance, Function<? super BeanTemplate.Installer, T> configurationCreator);

        /**
         * Creates a new bean without a source.
         *
         * @return a bean handle representing the new bean
         *
         * @throws IllegalStateException
         *             if this builder was created with a base template other than {@link BeanTemplate#STATIC}
         * @see app.packed.bean.BeanSourceKind#SOURCELESS
         */
        <T extends BeanConfiguration> BeanHandle<T> installSourceless(Function<? super BeanTemplate.Installer, T> configurationCreator);

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

        /**
         * Sets a supplier that creates a special bean mirror instead of a generic {@code BeanMirror} if a mirror for the bean
         * is requested.
         *
         * @param supplier
         *            the supplier used to create the bean mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         */
        Installer specializeMirror(Supplier<? extends BeanMirror> supplier);
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
