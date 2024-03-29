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
import java.util.Map;
import java.util.Optional;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.lifetime.PackedBeanTemplate;
import sandbox.extension.bean.BeanHandle.Builder;
import sandbox.extension.operation.OperationTemplate;

/**
 * A bean template is a reusable building block that is specified when a new bean is created.
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
//CreateAsClass
// Contexts (Bean + Lifetime operation)
// LocalSet
//Pouched-non-pouched // Not
public sealed interface BeanTemplate permits PackedBeanTemplate {

    // The bean is created by an operation
    // There are no lifetime operations on the bean
    // But the OperationHandle returned from BeanMethod.newLifetimeOperation.
    // Has a single operation that will create the bean.
    // TODO skal vi baade have managed og unmanged operationer???
    // Fx @Provide paa en prototypeBean (giver vel ikke mening)
    BeanTemplate GATEWAY = new PackedBeanTemplate(BeanKind.UNMANAGED);

    BeanTemplate FUNCTIONAL = BeanKind.STATIC.template();

    /**
     * Specifies the return type signature of the factory operation(s) that creates the bean.
     * <p>
     * The return type of the lifetime operation that creates the bean is {@code Object.class} as default. In order to
     * better support {@link java.lang.invoke.MethodHandle#invokeExact(Object...)}. However, this method can be used to
     * specify a less generic type if needed.
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
    BeanTemplate createAsBeanClass();

    /** {@return a descriptor for this template} */
    BeanTemplate.Descriptor descriptor();

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
    BeanTemplate inContextForLifetimeOperation(int index, ContextTemplate template);

    default <T> BeanTemplate localSet(BeanLocal<T> beanLocal, T value) {
        throw new UnsupportedOperationException();
    }

    /** A descriptor for a BeanTemplate. This class is mainly used for informational purposes. */
    interface Descriptor {

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
    }

}

interface Sandbox {

    void ignoreAnnotations(Class<?> annot);

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
    Builder synthetic(); // Maybe on template?

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

}
