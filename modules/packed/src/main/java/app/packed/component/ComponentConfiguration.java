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

import app.packed.base.NamespacePath;
import app.packed.bean.BeanConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;

/**
 * The base class for all component configuration classes.
 * <p>
 * This class can only be extended through one of its subclasses {@link BeanConfiguration} or
 * {@link ContainerConfiguration}.
 */
public abstract sealed class ComponentConfiguration permits BeanConfiguration,ContainerConfiguration {

    protected abstract void checkIsWiring();

    /** {@return a mirror for the component/} */
    // Er det et problem.. naar den ikke er fuldt wired endnu??? Men det er den vel, paa naer navnet
    protected abstract ComponentMirror mirror();

    /**
     * Sets the name of the component. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * If no name is explicitly set on a component. A name will be assigned to the component (at build time) in such a way
     * that it will have a unique name among other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see ComponentMirror#name()
     * @see Wirelet#named(String)
     */
    public abstract ComponentConfiguration named(String name);

    /**
     * Ivoked A callback method invoked by Packed immediatly before it is marked as no longer configurable
     * <p>
     * <strong>Note:</strong> This method should never be overridden with a public modifier.
     */
    // Jeg tror vi replacer den med en lambda paa BeanHandle
    protected void onConfigured() {}

    // I think I would prefer a lambda on the bean driver
    protected void onWired() {}

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public abstract NamespacePath path();
}
//
///**
//* Returns an extension configuration object. This configuration object is typically used in situations where the
//* extension needs to delegate responsibility to classes that cannot invoke the protected methods on this class do to
//* visibility rules.
//* <p>
//* An instance of {@code ExtensionConfiguration} can also be dependency injected into the constructor of an extension
//* subclass. This is useful, for example, if you want to setup some external classes in the constructor that needs
//* access to the configuration object.
//* <p>
//* This method will fail with {@link IllegalStateException} if invoked from the constructor of the extension.
//* 
//* @throws IllegalStateException
//*             if invoked from the constructor of the configuration.
//* @return a configuration object for this extension
//*/
//final ComponentSetup component() {
//  ComponentSetup c = component;
//  if (c == null) {
//      throw new IllegalStateException("This operation cannot be invoked from the constructor of the configuration. If you need to perform "
//              + "initialization before the configuration is returned to the user, override " + ComponentConfiguration.class.getSimpleName() + "#onNew()");
//  }
//  return c;
//}
// Altsaa maaske skal vi reintroducere component context...
// Det er isaer den der BeanConfiguration.provide jeg ikke har lyst til at hardcode i BeanConfiguration...
/// Men hvis vi saa har ServiceExtension.Sub.provide(BeanConfigurationContext bc)

///**
//* A method that can be overridden
//* 
//* <p>
//* <strong>Note:</strong> This method should never be overridden with a public modifier.
//*/
//protected final void onNew() {}
// I don't expect this class to have any $ methods
// They should most likely be located in the driver instead
// A component configuration is just a thin wrapper

// Nice man kan faktisk lave en Assembly tager en component configuration med package private metoder
// Man saa kan expose... Men uden at expose nogle metoder paa selve configurations objektet...