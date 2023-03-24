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

import java.util.EnumSet;
import java.util.List;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.lifetime.PackedBeanTemplate;
import sandbox.extension.context.ContextTemplate;
import sandbox.extension.operation.OperationTemplate;

/**
 * A bean template defines basic properties for a bean and one must be provided when creating a new bean.
 * <p>
 * using when installating new beans using {@link app.packed.extension.BaseExtensionPoint#beanBuilder(BeanTemplate)} or
 * {@link app.packed.extension.BaseExtensionPoint#beanInstallerForExtension(BeanTemplate, app.packed.extension.ExtensionPoint.UseSite)}.
 *
 * <p>
 * In most cases Ideen er man started med en af de foruddefinered templates, og saa laver man modification
 *
 */
// Contexts (Hele beanen)
// Lifetime Args (Er det bare private
// Pouched-non-pouched
public sealed interface BeanTemplate permits PackedBeanTemplate {

    /**
     * Represents a bean whose lifetime is that of its container. This means it will always be created and destroyed
     * together with its container.
     * <p>
     * A single instance of the bean will be created (if the instance was not already provided when installing the bean)
     * when the container is instantiated. Where after its lifecycle will follow that of the container.
     * <p>
     * Beans that are part of the container's lifecycle
     * <p>
     * A bean created using this template never has has any {@link BeanHandle#lifetimeOperations() lifetime operations}. As
     * the lifetime of the bean is completely controlled by the container in which is installed into.
     */
    BeanTemplate CONTAINER = new PackedBeanTemplate(BeanKind.CONTAINER);

    /**
     * The lifetime of the bean is not managed by any extension. At least not in a standard way
     * <p>
     * {@link #operations()} always returns a empty list
     * <p>
     * All operations on the bean must take a bean instance.
     * <p>
     * Giver det mening overhoved at supportere operation
     * <p>
     * It is a failure to use lifecycle annotations on the bean
     **/
    BeanTemplate EXTERNAL = new PackedBeanTemplate(BeanKind.MANYTON);

    /**
     * A single instance of the bean is created lazily when needed.
     * <p>
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, Consumer)
     * @see BeanInstaller#install(Op)
     * @see Map#isEmpty()
     */
    BeanTemplate LAZY = new PackedBeanTemplate(BeanKind.LAZY);

    BeanTemplate MANAGED = new PackedBeanTemplate(BeanKind.MANYTON);

    /**
     * Represents a bean that no instances.
     * <p>
     * And hence no lifecycle then bean instance can go through.
     * <p>
     * The lifetime of the bean is identical to its container. A bean can never
     * <p>
     * Never has any lifetime operations.
     * <p>
     * Beans that use this template must always be created using {@link BeanSourceKind#CLASS a class} or
     * {@link BeanSourceKind#SOURCELESS without a source.}. Attempting to use an Op or an instance when installing the bean
     * will always fail with {@link app.packed.bean.BeanInstallationException}.
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, java.util.function.Consumer)
     * @see BeanInstaller#installWithoutSource()
     */
    BeanTemplate STATIC = new PackedBeanTemplate(BeanKind.STATIC);

    // An unmanaged bean will always return the bean instance.
    BeanTemplate PROTOTYPE = new PackedBeanTemplate(BeanKind.MANYTON);

    // The bean is created by an operation
    // There are no lifetime operations on the bean
    // But the OperationHandle returned from BeanMethod.newLifetimeOperation.
    // Has a single operation that will create the bean.
    // TODO skal vi baade have managed og unmanged operationer???
    // Fx @Provide paa en prototypeBean (giver vel ikke mening)
    BeanTemplate GATEWAY = new PackedBeanTemplate(BeanKind.MANYTON);

    /**
     * Specifies the return type signature of the lifetime operation that creates the bean.
     * <p>
     * The return type of the lifetime operation that creates the bean is normally {@link BeanHandle#beanClass()}. However,
     * in order to better support {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} this method can be used to
     * specify a more generic type, typically {@code Object.class}.
     * <p>
     * If this template is used when installing a bean whose bean class is not assignable to the specified class. The
     * framework will throw a {@link app.packed.bean.BeanInstallationException}.
     * <p>
     * The method handle of the first lifetime operation of the new template will always have the specified class as its
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
    BeanTemplate createAs(Class<?> clazz);

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
    BeanTemplate lifetimeOperationContext(int index, ContextTemplate template);
}

interface Sandbox {

    /** {@return a list of the various lifetime operations for this bean template.} */
    // Maybe just MethodType???
    default List<OperationTemplate> lifetimeOperations() {
        return List.of();
    }

    /**
     * Sets a context for the whole bean
     *
     * @param context
     *            the context
     * @return the new template
     */
    // Man skal vel angive hvordan context fungere.
    // Er den stored, eller skal den med til alle operation?
    default BeanTemplate beanContext(ContextTemplate context) {
        throw new UnsupportedOperationException();
    }

    /**
     * Marks the bean as synthetic.
     *
     * @return this installer
     */
    BeanBuilder synthetic(); // Maybe on template?

    default boolean isBasedOn(BeanTemplate template) {
        // return template.isUnmodified() && this.base = template
        return false;
    }

    // Ahh alt er raw
    default BeanTemplate raw() {
        return null;
    }

    void noScan();

    void ignoreAnnotations(Class<?> annot);

    /** {@return the allowwed bean source kinds for.} */
    default EnumSet<BeanSourceKind> sourceKinds() {
        return EnumSet.allOf(BeanSourceKind.class);
    }

    // Maa man goere paa installeren..
//    default <T> void initializeLocal(BeanLocal<T> local, T value) {
//
//    }

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

//
//    // Ideen er lidt at vi som default returnere Object vil jeg mene
//    // Men man kunne sige fx AbstractEntityBean at bean.init returnere
//    Builder instanceAs(Class<?> clazz);
//
//    default Builder inContext(ContextTemplate template) {
//        // This means Context args are added to all operations.
//
//        // builderManyton().inContext(WebContext.template).builder();
//        return this;
//    }
}
