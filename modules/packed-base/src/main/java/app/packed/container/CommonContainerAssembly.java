/*
  * Copyright (c) 2008 Kasper Nielsen.
? *
?
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
package app.packed.container;

import java.util.Set;

import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.BaseBeanConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;

/**
 * A container assembly. Typically you
 * 
 * 
 * Assemblies are the main source of system configuration. Basically a assembly is just a thin wrapper around
 * {@link BaseContainerConfiguration}. Delegating every invocation in the class to an instance of
 * {@link BaseContainerConfiguration} available via {@link #configuration()}.
 * <p>
 * A assembly instance can be used ({@link #build()}) exactly once. Attempting to use it multiple times will fail with
 * an {@link IllegalStateException}.
 * 
 * A generic assembly. Normally you would extend {@link BaseAssembly}
 * 
 * @see BaseAssembly
 */
// Altsaa har vi brug for at lave container assemblies uden en masse metoder
public abstract class CommonContainerAssembly extends ContainerAssembly<BaseContainerConfiguration> {

    /** Creates a new assembly using {@link BaseContainerConfiguration#driver()}. */
    protected CommonContainerAssembly() {
        super(BaseContainerConfiguration.driver());
    }

    /**
     * Creates a new assembly using the specified driver.
     * 
     * @param driver
     *            the container driver to use
     */
    protected CommonContainerAssembly(ComponentDriver<? extends BaseContainerConfiguration> driver) {
        super(driver);
    }

    /**
     * Returns an unmodifiable view of every extension that is currently used.
     * 
     * @return an unmodifiable view of every extension that is currently used
     * @see BaseContainerConfiguration#extensions()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension>> extensions() {
        return configuration().extensions();
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    // add? i virkeligheden wire vi jo class komponenten...
    // Og taenker, vi har noget a.la. configuration().wire(ClassComponent.Default.bind(implementation))
    protected final BaseBeanConfiguration install(Class<?> implementation) {
        return configuration().install(implementation);
    }

    /**
     * Installs a singleton bean that will use the specified {@link Factory} to instantiate the bean instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    protected final BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        return configuration().install(implementation, wirelets);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseAssembly#install(Factory)
     */
    protected final BaseBeanConfiguration install(Factory<?> factory) {
        return configuration().install(factory);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see CommonContainerAssembly#install(Factory)
     */
    protected final BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        return configuration().install(factory, wirelets);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ServiceBeanConfiguration} can be used to specify a specific parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final BaseBeanConfiguration installInstance(Object instance) {
        return configuration().installInstance(instance);
    }

    protected final BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        return configuration().installInstance(instance, wirelets);
    }

    /**
     * Links the specified container assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a mirror of the component that was linked
     * @see BaseContainerConfiguration#link(Assembly, Wirelet...)
     */
    // Er lidt ked af at returnere ComponentMirror... Det er ikke verdens undergang...
    // Men maaske skulle Component
    protected final ContainerMirror link(ContainerAssembly<?> assembly, Wirelet... wirelets) {
        return configuration().link(assembly, wirelets);
    }

    /**
     * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a container.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @see BaseContainerConfiguration#named(String)
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @throws IllegalStateException
     *             if called from outside {@link #build()}
     */
    protected final void named(String name) {
        configuration().named(name);
    }

    /**
     * {@return the path of the container}
     * 
     * @see BaseContainerConfiguration#path()
     */
    protected final NamespacePath path() {
        return configuration().path();
    }

    /**
     * Returns an instance of the specified extension class.
     * <p>
     * If this is first time this method has been called with the specified extension type. This method will instantiate an
     * extension of the specified type and retain it for future invocation.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the extension class to return an instance of
     * @return an instance of the specified extension class
     * @throws IllegalStateException
     *             if called from outside {@link #build()}
     * @see BaseContainerConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }
}
