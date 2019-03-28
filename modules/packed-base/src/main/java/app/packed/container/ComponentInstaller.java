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

import app.packed.inject.Factory;
import app.packed.util.TypeLiteral;

/**
 * A component installer that can install children
 */
// Split into two-> RuntimeComponentInstaller extended by this class.
// With RuntimeComponent ju
// Maybe ServiceableComponentInstaller
// ComponentServiceInstaller
public interface ComponentInstaller {

    /**
     * Install the specified component implementation as a new component.
     *
     * @param implementation
     *            the component implementation to install
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    default ComponentConfiguration install(Class<?> implementation) {
        return installService(implementation);
    }

    /**
     * Installs a new child to this configuration, which uses the specified factory to instantiate the component instance.
     *
     * @param <S>
     *            the type of child component to install
     * @param factory
     *            the factory used to instantiate the component instance
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    default ComponentConfiguration install(Factory<?> factory) {
        return installService(factory);
    }

    /**
     * Install the specified component instance as a child of this component.
     * 
     * @param <S>
     *            the type of child component to install
     * @param instance
     *            the component instance to install
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    default ComponentConfiguration install(Object instance) {
        return installService(instance);
    }

    /**
     * Install the specified component implementation as a child of this component.
     *
     * @param <S>
     *            the type of child component to install
     * @param implementation
     *            the component implementation to install
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    <S> ComponentServiceConfiguration<S> installService(Class<S> implementation);

    /**
     * Installs a new child to this configuration, which uses the specified factory to instantiate the component instance.
     *
     * @param <S>
     *            the type of child component to install
     * @param factory
     *            the factory used to instantiate the component instance
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    <S> ComponentServiceConfiguration<S> installService(Factory<S> factory);

    /**
     * Install the specified component instance as a child of this component.
     * 
     * @param <S>
     *            the type of child component to install
     * @param instance
     *            the component instance to install
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    <S> ComponentServiceConfiguration<S> installService(S instance);

    <S> ComponentServiceConfiguration<S> installService(TypeLiteral<S> implementation);
}

/// **
// * Install the specified component implementation and makes it available as a service with the specified class as the
// * key. Invoking this method is equivalent to invoking {@code Factory.findInjectable(implementation)}.
// *
// * @param <T>
// * the type of component to install
// * @param implementation
// * the component implementation to install
// * @return a component configuration that can be use to configure the component in greater detail
// * @throws InvalidDeclarationException
// * if some property of the implementation prevents it from being installed as a component
// */
// @Override
// <T> ComponentServiceConfiguration<T> installService(Class<T> implementation);
//
// @Override
// <T> ComponentServiceConfiguration<T> installService(Factory<T> factory);
//
/// **
// * Install the specified component instance and makes it available as a service with the key
// * {@code instance.getClass()}.
// * <p>
// * If this install operation is the first install operation. The component will be installed as the root component of
// * the container. All subsequent install operations on this configuration will have the component as its parent. If
/// you
// * wish to use a specific component as a parent, use the various install methods on the
// * {@link ComponentServiceConfiguration} instead of the install methods on this interface.
// *
// * @param <T>
// * the type of component to install
// * @param instance
// * the component instance to install
// * @return the component configuration
// * @throws InvalidDeclarationException
// * if some property of the instance prevents it from being installed as a component
// */
// @Override
// <T> ComponentServiceConfiguration<T> installService(T instance);
//
/// **
// * Install the specified component implementation and makes it available as a service with the specified type literal
/// as
// * the key. Invoking this method is equivalent to invoking {@code Factory.findInjectable(implementation)}.
// *
// * @param <T>
// * the type of component to install
// * @param implementation
// * the component implementation to install
// * @return component configuration that can be use to configure the component in greater detail
// * @throws InvalidDeclarationException
// * if some property of the implementation prevents it from being installed as a component
// */
// @Override
// <T> ComponentServiceConfiguration<T> installService(TypeLiteral<T> implementation);
