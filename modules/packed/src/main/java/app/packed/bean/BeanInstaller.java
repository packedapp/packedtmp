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

    // Er det udelukken BeanClass vi reagere paa her???

    //Shouldn't it be Consumer<? super H> onNew
    <H extends BeanHandle<T>, T extends BeanConfiguration> H installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanInstaller, H> configurationCreator, Consumer<? super BeanHandle<?>> onNew);

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