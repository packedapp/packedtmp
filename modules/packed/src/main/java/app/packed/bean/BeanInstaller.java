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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanInstaller;

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
public sealed interface BeanInstaller permits PackedBeanInstaller {

    BeanInstaller componentTag(String... tags);

    default BeanHandle<BeanConfiguration> install(Class<?> beanClass) {
        return install(beanClass, BeanHandle::new);
    }

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
    <H extends BeanHandle<?>> H install(Class<?> beanClass, Function<? super BeanInstaller, H> factory);

    <H extends BeanHandle<?>> H install(Op<?> beanClass, Function<? super BeanInstaller, H> factory);

    // These things can never be multi
    // AbsentInstalledComponent(boolean wasInstalled)
    <H extends BeanHandle<T>, T extends BeanConfiguration> H installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanInstaller, H> configurationCreator, Consumer<? super BeanHandle<?>> onNew);

    // instance = introspected bean
    // constant = non-introspected bean
    <H extends BeanHandle<?>> H installInstance(Object instance, Function<? super BeanInstaller, H> factory);

    /**
     * Creates a new bean without a source.
     *
     * @return a bean handle representing the new bean
     *
     * @throws IllegalStateException
     *             if this builder was created with a base template other than {@link BeanTemplate#STATIC}
     * @see app.packed.bean.BeanSourceKind#SOURCELESS
     */
    <H extends BeanHandle<?>> H installSourceless(Function<? super BeanInstaller, H> factory);

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
    <T> BeanInstaller setLocal(BeanBuildLocal<T> local, T value);
}