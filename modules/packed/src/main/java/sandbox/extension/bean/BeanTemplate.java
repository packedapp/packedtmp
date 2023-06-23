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
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.lifetime.PackedBeanTemplate;
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
// Contexts (Hele beanen)
// Lifetime Args (Er det bare private
// Pouched-non-pouched
public sealed interface BeanTemplate permits PackedBeanTemplate {

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
    BeanTemplate EXTERNAL = new PackedBeanTemplate(BeanKind.UNMANAGED);

    // The bean is created by an operation
    // There are no lifetime operations on the bean
    // But the OperationHandle returned from BeanMethod.newLifetimeOperation.
    // Has a single operation that will create the bean.
    // TODO skal vi baade have managed og unmanged operationer???
    // Fx @Provide paa en prototypeBean (giver vel ikke mening)
    BeanTemplate GATEWAY = new PackedBeanTemplate(BeanKind.UNMANAGED);

    /**
     * Specifies the return type signature of the factory operation(s) that creates the bean.
     * <p>
     * The return type of the lifetime operation that creates the bean is normally {@link BeanHandle#beanClass()}. However,
     * in order to better support {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} this method can be used to
     * specify a more generic type, typically {@code Object.class}.
     * <p>
     * If this template is used when installing a bean whose bean class is not assignable to the specified class. The
     * framework will throw a {@link app.packed.bean.BeanInstallationException}.
     * <p>
     * The method handle of factory operation of the new template will always have the specified class as its
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

    // When do you ever want this????
    BeanTemplate createAsBeanClass();

    /**
     * Sets a context for the whole bean
     *
     * @param context
     *            the context
     * @return the new template
     */
    // Man skal vel angive hvordan context fungere.
    // Er den stored, eller skal den med til alle operation?
    default BeanTemplate inContext(ContextTemplate context) {
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
    BeanTemplate inLifetimeOperationContext(int index, ContextTemplate template);

    /** {@return a descriptor for this template} */
    BeanTemplate.Descriptor descriptor();

    /** A descriptor for a BeanTemplate. */
    interface Descriptor {
        Class<?> createAs();

        /** {@return a list of the various lifetime operations for this bean template.} */
        // These operations cannot be directly modified. Instead must methods on this class
        List<OperationTemplate.Descriptor> lifetimeOperations();

        BeanKind beanKind();
        // Contexts
    }

}

interface Sandbox {

    void ignoreAnnotations(Class<?> annot);

    default boolean isBasedOn(BeanTemplate template) {
        // return template.isUnmodified() && this.base = template
        return false;
    }

    void noScan();

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
    BeanBuilder synthetic(); // Maybe on template?

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
