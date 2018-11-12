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
package app.packed.inject;

import java.util.Optional;

import app.packed.container.Component;
import app.packed.util.VariableDescriptor;
import packed.internal.inject.InternalInjectionSites;

/**
 * An injection site extends the Dependency interface with runtime information about which injector requested the
 * injection. And if used within a container which component requested the injection.
 * <p>
 * This class is typically used together with the {@link Provides} annotation to provide custom injection depending on
 * attributes of the requestor.
 *
 * <p>
 * This interface can only be used together with {@link Factory1}
 * 
 * Access mode: lookup + injection or
 *
 * Anonymous :
 *
 * Basically there are 3 supported ways to inject.
 *
 * Parameters on constructors or methods.
 *
 * Fields
 *
 * getInstance() such as {@link Injector#with(Class)}.
 */
// Hvad hvis man injecter i en service???Hvorfor kan man ikke faa det at vide
public interface InjectionSite extends Dependency {

    /**
     * If a component is requesting returns the component, otherwise an empty optional.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    Optional<Component> getComponent();

    /**
     * Returns the injector through which injection was requested. If the injection was requested via a container, the
     * returned injector can be cast to the container interface.
     *
     * @return the injector through which injection was requested
     */
    Injector getInjector();

    /**
     * Returns a new injection site for the specified injector and key.
     * <p>
     * This method is can be used to create a valid injection site for methods such as {@link Injector#with(Key)}.
     * 
     * @param injector
     *            the injector from where injection is requested
     * @param key
     *            the for which injection is requested
     * @return an injection site for the specified injector and key.
     */
    static InjectionSite of(Injector injector, Key<?> key) {
        // Add components???
        // add Dependency????
        // or Dependency.toInjectionSite(Injector i);
        // or Dependency.toInjectionSite(Injector i, Component c);
        return InternalInjectionSites.of(injector, key, null);
    }

    static InjectionSite of(Injector injector, VariableDescriptor variable) {
        throw new UnsupportedOperationException();
    }

    // public static void main(String[] args) {
    // new Factory1<InjectionSite, Logger>(s -> {
    // String loggerName = s.getMember().map(e -> e.getDeclaringClass().getName()).orElse("Unknown");
    // return Logger.get(loggerName);
    // }) {};
    // }

}
