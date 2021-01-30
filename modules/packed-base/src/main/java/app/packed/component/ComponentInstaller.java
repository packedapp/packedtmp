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
 *
 */
// Daarligt navn. Hvis den wire. Men andre metoder som ikke er paa interfacet installer.
// ComponentWireway <-- somewhere you can install wirings
public interface ComponentInstaller {
    
    /**
     * @param bundle
     *            the bundle
     * @param wirelets
     *            wirelets
     * 
     * @apiNote Previously this method returned the specified bundle. However, to encourage people to configure the bundle
     *          before calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have
     *          void return type.
     */
    void link(Assembly<?> bundle, Wirelet... wirelets);
    
    default <C extends ComponentConfiguration, I> C wire(ComponentClassDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

    /**
     * Wires a new child component using the specified driver
     * 
     * @param <C>
     *            the type of configuration returned by the driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the component
     */
    <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets);

    default <C extends ComponentConfiguration, I> C wire(ComponentFactoryDriver<C, I> driver, Factory<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

    default <C extends ComponentConfiguration, I> C wireInstance(ComponentInstanceDriver<C, I> driver, I instance, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bindInstance(instance);
        return wire(cd, wirelets);
    }
}
