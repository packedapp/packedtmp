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
import java.util.Optional;

import app.packed.context.Context;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.PackedBeanTemplate;
import sandbox.application.LifetimeTemplate;

/**
 * A bean template is an immutable, reusable configuration object that defines the behaviour of a bean. A template is
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
// Don't do BeanTemplate<H> it does not work because Bean is parameterized unlike Container/Application
// It al goes well until you get to BeanInstaller. Where we need the <T> of the BeanHandle
public sealed interface BeanTemplate permits PackedBeanTemplate {

    // InstanceMembersForbiddenException
    BeanTemplate FUNCTIONAL = BeanLifetime.FUNCTIONEL.template();

    // The bean is created by an operation
    // There are no lifetime operations on the bean
    // But the OperationHandle returned from BeanMethod.newLifetimeOperation.
    // Has a single operation that will create the bean.
    // TODO skal vi baade have managed og unmanged operationer???
    // Fx @Provide paa en prototypeBean (giver vel ikke mening)
    BeanTemplate GATEWAY = BeanTemplate.builder(BeanLifetime.UNMANAGED).build();

    /** {@return the kind of bean the descriptor's template creates} */
    BeanLifetime beanKind();

//    /** {@return a list of the various lifetime operations for the descriptor's template.} */
//    List<OperationTemplate> lifecycleOperations();

  //  Set<Class<? extends Context<?>>> contexts();

    /**
     * Normally a bean is constructed as the ben
     * <p>
     * Empty means create as bean class
     *
     * @return
     *
     * @see BeanTemplate#createAs(Class)
     * @see BeanTemplate#createAsBeanClass()
     */
    Optional<Class<?>> createAsSpecificClass();

    Optional<OperationTemplate> initializationX();

    static BeanTemplate.Builder builder(BeanLifetime kind) {
        return PackedBeanTemplate.builder(kind);
    }

    /** A builder for {@link BeanTemplate}. */
    // Add component tags
    sealed interface Builder permits PackedBeanTemplate.PackedBuilder {

        default Builder requiredManagedContainer() {
            // If attempting to install into an unmanaged container
            // it will fail
            // IDK about this. Should fail no matter anyway if we use @OnStop
            throw new UnsupportedOperationException();
        }
        // requireManaged() <-- attempting to install

        // Specific class right???
        Builder beanClass(Class<?> beanClass);

        /** {@return a new bean template} */
        BeanTemplate build();

        Builder initialization(java.util.function.Function<OperationTemplate, OperationTemplate> configure);

        Builder initialization(OperationTemplate initialization);

        @SuppressWarnings("exports")
        Builder lifetime(LifetimeTemplate lifetime);


        Builder addContext(Class<? extends Context<?>> contextClass);

        <T> Builder setLocal(BeanLocal<T> local, T value);
    }
}

///**
//* <p>
//* Use {@code ContextValue(BeanLifetimeOperationContext.class)} of the exact type
//* <p>
//* The context is only available for the extension that installed the bean
//* <p>
//* When returned the targeted lifetime operation will have been updated.
//* <p>
//* If this template contains multiple lifetime operations different contexts can be set.
//*
//* @param index
//*            the index of the lifetime operation. Must match an operation in {@link #lifetimeOperations()}.
//* @param argumentType
//*            the type of argument that will be taken and made available
//* @return the new template
//* @throws IndexOutOfBoundsException
//*             if the specified index does not match a lifetime operation
//* @throws IllegalArgumentException
//*             if the specified argument type is void
//* @see BeanLifetimeOperationContext
//* @see app.packed.extension.context.ContextValue
//*/
//// Lifetime operationer kan koeres i en context
//Configurator inContextForLifetimeOperation(int index, ContextTemplate template);

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
    // Allowed source kinds
    default EnumSet<BeanSourceKind> sourceKinds() {
        return EnumSet.allOf(BeanSourceKind.class);
    }

    /**
     * Marks the bean as synthetic.
     *
     * @return this installer
     */
    BeanInstaller synthetic(); // Maybe on template?

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
    default BeanTemplate withInitializeAs(Class<?> clazz) {
//      if (template.createAs.isPrimitive() || BeanSetup.ILLEGAL_BEAN_CLASSES.contains(template.createAs)) {
//      throw new IllegalArgumentException(template.createAs + " is not valid argument");
//  }
        // return withInitialization(OperationTemplate.defaults().withReturnType(clazz));
        throw new UnsupportedOperationException();
    }

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
    default BeanTemplate withInitializeAsBeanClass() {
        // return withInitialization(OperationTemplate.defaults().withReturnTypeDynamic());
        throw new UnsupportedOperationException();
    }

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

}
