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
package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDescriptor;
import app.packed.component.ComponentDriver;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.inject.Factory;
import app.packed.service.ServiceExtension;
import sandbox.artifact.hostguest.HostConfiguration;
import sandbox.artifact.hostguest.HostDriver;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured by
 * extending {@link ContainerBundle} or {@link DefaultBundle}.
 */
public interface ContainerConfiguration extends ComponentConfiguration {

//    /**
//     * Installs a host and returns the configuration of it.
//     * 
//     * @param <T>
//     *            the type of host configuration to return
//     * @param type
//     *            the type of host configuration to return
//     * @return a host configuration of the specified type
//     */
////    <T extends HostConfiguration> T addHost(Class<T> type);

    <C extends HostConfiguration<?>> C addHost(HostDriver<C> driver);

    /**
     * Returns an unmodifiable view of the extensions that have been configured so far.
     * 
     * @return an unmodifiable view of the extensions that have been configured so far
     * 
     * @see #use(Class)
     * @see ContainerBundle#extensions()
     */
    Set<Class<? extends Extension>> extensions();

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate the an instance of the component. (only if there are
     * dependencies???)
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    // Rename install to add
    <T> SingletonConfiguration<T> install(Class<T> implementation);

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate an component instance from the factory.
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see ContainerBundle#install(Factory)
     */
    <T> SingletonConfiguration<T> install(Factory<T> factory);

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerBundle#installConstant(Object)
     */
    <T> SingletonConfiguration<T> installInstance(T instance);

    /**
     * Installs a stateless component.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    StatelessConfiguration installStateless(Class<?> implementation);

    /**
     * Returns whether or not this container is the root container in an artifact.
     * 
     * @return whether or not this container is the root container in an artifact
     */
    boolean isArtifactRoot();

    /**
     * Creates a new container with this container as its parent by linking the specified bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            any wirelets
     */
    void link(ContainerBundle bundle, Wirelet... wirelets);

    /**
     * Registers a {@link Lookup} object that will be used for accessing members on components that are registered with the
     * container.
     * <p>
     * Lookup objects passed to this method are never made directly available to extensions. Instead the lookup is used to
     * create {@link MethodHandle} and {@link VarHandle} that are passed along to extensions.
     * <p>
     * This method allows passing null, which clears any lookup object that has previously been set. This is useful if allow
     * 
     * 
     * @param lookup
     *            the lookup object
     */
    // If you are creating resulable stuff, you should remember to null the lookup object out.
    // So child modules do not have the power of the lookup object.

    // Class<?> realm(Lookup);

    // Bliver brugt til boern.. og ikke en selv...
    void lookup(@Nullable Lookup lookup);

    /** {@inheritDoc} */
    @Override
    ContainerConfiguration setDescription(String description);

    /** {@inheritDoc} */
    @Override
    ContainerConfiguration setName(String name);

    /**
     * Returns the class that defines the container.
     * 
     * @return the class that defines the container
     */
    Class<?> sourceType();

    /** {@inheritDoc} */
    @Override
    default ComponentDescriptor model() {
        return ComponentDescriptor.CONTAINER;
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type is
     * requested. This method will create a new instance of the extension and return it for all subsequent calls to this
     * method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()
     */
    <T extends Extension> T use(Class<T> extensionType);

    /**
     * The specified wirelet type must have
     * 
     * @param <W>
     *            the type of wirelet
     * @param type
     *            the type of wirelet
     * @return an optional containing the wirelet if defined otherwise empty.
     * @throws IllegalArgumentException
     *             if the specified wirelet type does not have {@link WireletSidecar#failOnImage()} set to true
     */
    <W extends Wirelet> Optional<W> assemblyWirelet(Class<W> type); // Should assembly be the default????

    static ComponentDriver<ContainerConfiguration> driver() {
        return new PackedContainerDriver();
    }

}

class PackedContainerDriver implements ComponentDriver<ContainerConfiguration> {}

// I think ContainerConfiguration is better
// Ideen er lidt at man kan lave sine egne
// ContainerConfiguration.of(Lookup); <--- of(lookup, lookup.lookupClass)
// ContainerConfiguration.of(Lookup, Class sourceType);
///**
//* Creates a new layer.
//* 
//* @param name
//*            the name of layer
//* @param dependencies
//*            dependencies on other layers
//* @return the new layer
//*/
//ContainerLayer newLayer(String name, ContainerLayer... dependencies);

/**
 * Returns the build context. A single build context object is shared among all containers for the same artifact.
 * 
 * @return the build context
 */
// ArtifactBuildContext buildContext();