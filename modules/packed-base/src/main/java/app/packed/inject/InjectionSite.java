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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.container.Component;
import app.packed.util.Key;

/**
 * An injection site extends the dependency interface with runtime information about which injector requested the
 * injection. And if used within a container which component requested the injection.
 * <p>
 * This class is typically used together with the {@link Provides} annotation to provide custom injection depending on
 * attributes of the requestor. <pre> {@code  @Provides
 *  public static Logger provideLogger(InjectionSite site) {
 *    if (site.getComponent().isPresent()) {
 *      return Logger.getLogger(site.getComponent().get().getPath().toString());
 *    } else {
 *      return Logger.getAnonymousLogger();
 *    }
 *  }}
 * </pre>
 * <p>
 * An injection site can also be used to create a factory that is functional equivalent:
 * 
 * <pre> {@code
 *  new Factory1<InjectionSite, Logger>(
 *    site -> site.getComponent().isPresent() ? Logger.getLogger(site.getComponent().get().getPath().toString()) : Logger.getAnonymousLogger()) {};
 * }
 * </pre>
 */
public interface InjectionSite extends Dependency {

    /**
     * If a component is requesting returns the component, otherwise an empty optional.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    Optional<Component> getComponent();

    /**
     * Returns the injector through which injection was requested. If the injection was requested via a container, the
     * returned injector can be cast to Container.
     *
     * @return the injector through which injection was requested
     */
    Injector getInjector();

    static InjectionSite of(Injector injector, Dependency dependency) {
        return new InjectionSiteForDependency(injector, dependency, null);
    }

    static InjectionSite of(Injector injector, Dependency dependency, Component componenent) {
        return new InjectionSiteForDependency(injector, dependency, requireNonNull(componenent, "component is null"));
    }

    /**
     * Returns a new injection site for the specified injector and key.
     * <p>
     * This method is used to create injection site for methods such as {@link Injector#with(Key)}.
     * 
     * @param injector
     *            the injector from where injection is requested
     * @param key
     *            the for which injection is requested
     * @return an injection site for the specified injector and key.
     */
    static InjectionSite of(Injector injector, Key<?> key) {
        return new InjectionSiteForKey(injector, key, null);
    }

    /**
     * Returns a new injection site for the specified injector, key and component.
     * <p>
     * This method is used to create injection site for methods such as {@link Injector#with(Key)} on
     * {@link Component#injector() component injectors}.
     * 
     * @param injector
     *            the injector from where injection is requested
     * @param key
     *            the for which injection is requested
     * @param component
     *            the component to which the injector belongs
     * @return an injection site for the specified injector and key and component.
     * @see #of(Injector, Dependency)
     */
    static InjectionSite of(Injector injector, Key<?> key, Component component) {
        return new InjectionSiteForKey(injector, key, requireNonNull(component, "component is null"));
    }

    // withTags();// A way to provide info to @Provides....ahh bare mere boebl
}