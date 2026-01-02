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
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.context.Context;
import internal.app.packed.bean.PackedBeanInstaller;

/**
 * A bean installer is responsible for installing new beans into a container. It is typically only used by
 * {@link app.packed.extension.Extension extensions} and normal users will rarely have any use for it.
 * <p>
 *
 * @see app.packed.extension.BaseExtensionPoint#newApplicationBean(BeanTemplate)
 * @see app.packed.extension.BaseExtensionPoint#newDependantExtensionBean(BeanTemplate,
 *      app.packed.extension.ExtensionPoint.UseSite)
 */
public sealed interface BeanInstaller permits PackedBeanInstaller {

    BeanInstaller componentTag(String... tags);

    BeanInstaller addContext(Class<? extends Context<?>> contextClass);

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
     */
    <H extends BeanHandle<?>> H install(Bean<?> bean, Function<? super BeanInstaller, H> factory);

    // These things can never be multi
    // AbsentInstalledComponent(boolean wasInstalled)

    <H extends BeanHandle<?>> H installIfAbsent(Class<?> beanClass, Class<? super H> handleClass, Function<? super BeanInstaller, H> handleFactory,
            Consumer<? super H> onNew);

    BeanInstaller namePrefix(String prefix);

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
    <T> BeanInstaller setLocal(BeanLocal<T> local, T value);
}



interface Sandbox {
    void ignoreAnnotations(Class<?> annot);

    void noScan();

    // The bean can never
    void proxyForbidden();

    // Ahh alt er raw
    default Object raw() {
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
    default Object withInitializeAs(Class<?> clazz) {
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
    default Object withInitializeAsBeanClass() {
        // return withInitialization(OperationTemplate.defaults().withReturnTypeDynamic());
        throw new UnsupportedOperationException();
    }

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

}