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

/**
 *
 */

/** A service refers to a live version of ServiceDescriptor */
// Hvorfor ikkke? Endnu en klasse
// Hvorfor, kunne vaere rart at kunne slaa den op, og saa
// kan man vel mere eller mindre noejes med at implementre injector().services()
// Hvis ServiceConfiguration skal have en Contextualizable saa behover vi den jo...
// Ogsaa interessant med at tags er readonly....og context ikke kan vaere statisk,... hmmmx

public interface Service<T> extends ServiceDescriptor {
    // T get(InjectionSite request);

    /**
     * Returns the injector this service is registered with.
     *
     * @return
     */
    // Vil vi have denne? Ja det er sgu ligesom getContainer paa component
    Injector getInjector();

    // /**
    // * Returns the container in which the service is defined. Or null if registered outside a container. For example, via
    // a
    // * standalone {@link Injector}.
    // *
    // * @return the container in which the service is defined
    // */
    // Container getContainer();

    /// **
    // * Returns the component that defines the service.
    // *
    // * @return the component that defines the service
    // */
    // Component getComponent();// getDeclaringComponent(); Optional<Component>

    // Provider<T> getProvider(InjectionSite request);
}
