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
package app.packed.component;

import app.packed.inject.Factory;

/**
 * A component installer that can install children
 */
// Split into two-> RuntimeComponentInstaller extended by this class.
/// Maybe throw UnsupportedOperationException....
// With RuntimeComponent ju
// Maybe ServiceableComponentInstaller
// ComponentServiceInstaller

// Den er actuel paa runtime, og maaske hvis man lavede en actor bundle...

// Kunn ogsaa vaere smart, at kunne registere ngoet, som ikke er services.
// Men noget der wrapper ComponentContext, og f.eks. ActorContext();
// Og saa hide ComponentContext...

// Uhh, kan man lave nogle cool Distributerede systemer paa den her maade???

// Vi installere et job,
public interface ComponentInstaller {

    /**
     * Install the specified component implementation as a new component.
     *
     * @param implementation
     *            the component implementation to install
     * @return a configuration for the new component
     */
    // throw some kind of missing method handle lookup instance
    default ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /**
     * Installs a new child to this configuration, which uses the specified factory to instantiate the component instance.
     *
     * @param factory
     *            the factory used to instantiate the component instance
     * @return the configuration of the child component
     */
    ComponentConfiguration install(Factory<?> factory);

    /**
     * Installs a component a singleton component. lazy, prototype und so weiter will throw an
     * {@link UnsupportedOperationException}.
     * 
     * the specified component instance as a child of this component.
     * 
     * @param instance
     *            the component instance to install
     * @return the configuration of the child component
     */
    ComponentConfiguration install(Object instance);

    //
    // /**
    // * Install the specified component implementation as a child of this component.
    // *
    // * @param <S>
    // * the type of child component to install
    // * @param implementation
    // * the component implementation to install
    // * @return the configuration of the child component
    // * @throws UnsupportedOperationException
    // * if the installer does not support installing component services. For example, this is not allowed at
    // * runtime.
    // */
    // default <S> ComponentServiceConfiguration<S> installService(Class<S> implementation) {
    // return installService(Factory.findInjectable(implementation));
    // }
    //
    // /**
    // * Installs a new child to this configuration, which uses the specified factory to instantiate the component instance.
    // *
    // * @param <S>
    // * the type of child component to install
    // * @param factory
    // * the factory used to instantiate the component instance
    // * @return the configuration of the child component
    // * @throws UnsupportedOperationException
    // * if the installer does not support installing component services. For example, this is not allowed at
    // * runtime.
    // */
    // <S> ComponentServiceConfiguration<S> installService(Factory<S> factory);

    /**
     * Install the specified component instance as a child of this component.
     * 
     * @param <S>
     *            the type of child component to install
     * @param instance
     *            the component instance to install
     * @return the configuration of the child component
     * @throws UnsupportedOperationException
     *             if the installer does not support installing component services. For example, this is not allowed at
     *             runtime.
     */
    // <S> ComponentServiceConfiguration<S> installService(S instance);
    //
    // default <S> ComponentServiceConfiguration<S> installService(TypeLiteral<S> implementation) {
    // return installService(Factory.findInjectable(implementation));
    // }
}
