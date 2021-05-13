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

import app.packed.container.BaseAssembly;
import app.packed.inject.Factory;
import packed.internal.component.OldPackedClassComponentDriver;

/**
 * Component drivers are responsible for configuring and creating new components. They are rarely created by end-users.
 * And it is possible to use Packed without every being directly exposed to component drivers. Instead users would
 * normally used some of the predifined used drivers such as ....
 * <p>
 * 
 * Every time you, for example, call {@link BaseAssembly#install(Class)} it actually de
 * 
 * install a
 * 
 * @param <C>
 *            the type of component configuration this driver create
 */
public /* sealed */ interface ComponentDriver<C extends ComponentConfiguration> {

    /**
     * Returns the set of modifiers that will be applied to the component.
     * <p>
     * Additional modifiers may be added once the component is wired.
     * 
     * @return the set of modifiers that will be applied to the component
     */
    ComponentModifierSet modifiers();

    ComponentDriver<C> with(Wirelet wirelet);

    ComponentDriver<C> with(Wirelet... wirelet);

    default ComponentDriver<C> bind(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a driver that can be used to create stateless components.
     * 
     * @param <T>
     *            the type
     * @return a driver
     */
    @SuppressWarnings("unchecked")
    private static ComponentDriver<BaseComponentConfiguration> driver() {
        return OldPackedClassComponentDriver.STATELESS_DRIVER;
    }

    // Not sure we want this public or ma
    @SuppressWarnings("unchecked")
    static ComponentDriver<BaseComponentConfiguration> driverInstall(Class<?> implementation) {
        return OldPackedClassComponentDriver.INSTALL_DRIVER.bind(implementation);
    }

    @SuppressWarnings("unchecked")
    static ComponentDriver<BaseComponentConfiguration> driverInstall(Factory<?> factory) {
        return OldPackedClassComponentDriver.INSTALL_DRIVER.bind(factory);
    }

    @SuppressWarnings("unchecked")
    static ComponentDriver<BaseComponentConfiguration> driverInstallInstance(Object instance) {
        return OldPackedClassComponentDriver.INSTALL_DRIVER.bind(instance);
    }

    static ComponentDriver<BaseComponentConfiguration> driverStateless(Class<?> implementation) {
        return driver().bind(implementation);
    }

    static <T> ComponentDriver<BaseComponentConfiguration> functional(Class<?> implementation) {
        return driver().bind(implementation);
    }

    // Or FunctionalInterface? Nahhh Function
    // forFunction(FunctionalInterface)
    // forFunction(TypeToken)
    interface ForFunction {
        // Completely stateless
        // Syntes maaske static component skal fungere paa samme maade
    }
    interface ForContainer {}
    interface ForClass {}
    
    interface ForHook {}
    interface ForEmbedded {} //?
    // We pack a user supplied object
    interface ForClassEnvelope{}

    // IDK
    interface SourcedBuilder {

    }
}
