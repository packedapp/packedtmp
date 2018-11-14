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

import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;
import app.packed.util.Taggable;

/**
 * The configuration of a service. An instance of this type is usually obtained by calling the various bind methods
 * located on {@link InjectorConfiguration}.
 * <ul>
 * <li>Binding of services to an injector</li>
 * <li>Exporting services from a bundle</li>
 * <li>Import services from a bundle or Injector</li>
 * <li>Extending ComponentConfiguration</li>
 * </ul>
 */
// This once extended ServiceDescriptor but no more, for some reason I have already forgot???
public interface ServiceConfiguration<T> extends Taggable {

    /**
     * Services registered via {@link InjectorConfiguration} does not support binding to null. However components does.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @throws NullPointerException
     *             if the key is null and the type of service does not support binding to null
     */
    default ServiceConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    /**
     * Services registered via {@link InjectorConfiguration} does not support binding to null. However components does.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @throws NullPointerException
     *             if the key is null and the type of service does not support binding to null
     */
    ServiceConfiguration<T> as(Key<? super T> key);

    /**
     * Services registered via {@link InjectorConfiguration} does not support binding to null. However components does.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @throws NullPointerException
     *             if the key is null and the type of service does not support binding to null
     */
    default ServiceConfiguration<T> as(TypeLiteral<? super T> key) {
        return as(key.toKey());
    }

    /**
     * Returns the binding mode of the service.
     *
     * @return the binding mode of the service
     */
    BindingMode getBindingMode();

    /**
     * Returns the configuration site where this configuration was created.
     * 
     * @return the configuration site where this configuration was created
     */
    ConfigurationSite getConfigurationSite();

    /**
     * Returns the description of this service. Or null if no description has been set.
     *
     * @return the description of this service
     * @see ServiceConfiguration#setDescription(String)
     */
    @Nullable
    String getDescription();

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see ServiceConfiguration#as(Key)
     */
    Key<?> getKey();

    /**
     * Sets the description of this service.
     *
     * @param description
     *            the description of the service
     * @return this configuration
     * @see #getDescription()
     * @see ServiceDescriptor#getDescription()
     */
    ServiceConfiguration<T> setDescription(String description);
}
//
/// **
// * Returns an mutable set of string tags whose contents will be made available via {@link ServiceDescriptor#tags()} at
// * runtime.
// * <p>
// * The returned set throws {@link IllegalStateException} on all mutable operations after the service has been
// * constructed.
// *
// * @return a mutable set of tags
// * @see ServiceDescriptor#tags()
// */
// @Override
// Set<String> tags();