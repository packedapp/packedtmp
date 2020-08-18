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
package app.packed.service;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.container.BaseBundle;

/**
 * A configuration object for a service. An instance of this interface is usually obtained by calling the various
 * provide or export methods located on {@link ServiceExtension}, {@link InjectorAssembler} or {@link BaseBundle}.
 */
public interface ServiceConfiguration<T> /* extends Taggable */ {

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #getKey()
     */
    default ServiceConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    /**
     * Registers this service under the specified key.
     *
     * @param key
     *            the key for which to register the service under
     * @return this configuration
     * @see #getKey()
     */
    ServiceConfiguration<T> as(Key<? super T> key);

    /**
     * Returns the configuration site where this configuration was created.
     * 
     * @return the configuration site where this configuration was created
     */
    ConfigSite configSite();

    /**
     * Returns the description of this service. Or null if no description has been set.
     *
     * @return the description of this service
     * @see #setDescription(String)
     */
    @Nullable
    String getDescription();

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see #as(Key)
     * @see #as(Class)
     */
    Key<?> getKey();

    /**
     * Sets the description of this service.
     *
     * @param description
     *            the description of the service
     * @return this configuration
     * @see #getDescription()
     */
    ServiceConfiguration<T> setDescription(@Nullable String description);

    // :< Can't really have both named and setName
    // ServiceConfiguration<T> named(String name);//put a Named qualifier

}

/// **
// * Indicates that the service will not be registered under any key. There are a number of use cases for this method:
// * <p>
// * The primary use for this method is to register object with has fields and/or methods annotated with {@link
/// Provides}.
// * But where we do not want to expose the declaring class as a service.
// * <p>
// * Install component with a serv
// * <p>
// * For import and export stages, to indicate that a service should not be send further in the pipeline.
// *
// * @return this configuration
// */
//// another usecase is for registering a service that should only be available outward facing
//// (exportService(provide().asNone).as(Foo.class)
// ServiceConfiguration<?> asNone();
//
/// / Should include dependencies via @Inject
//// List<DependencyDescriptor> dependencies();