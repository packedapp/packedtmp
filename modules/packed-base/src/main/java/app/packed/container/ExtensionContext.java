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

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;

/**
 * An instance of this interface is available via {@link Extension#context()} or via constructor injection into an
 * extension. Since the extension itself defines most methods in this interface via protected final methods. This
 * interface is typically used to be able to provide these methods to code that is not located on the extension
 * implementation or in the same package as the extension itself.
 */
public interface ExtensionContext {

    /**
     * Checks that the underlying extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extension's onConfigured method has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

    /**
     * Returns the config site of the container to which the underlying extension belongs.
     * 
     * @return the config site of the container to which the underlying extension belongs
     */
    ConfigSite containerConfigSite();

    /**
     * Returns the path of the container to which the underlying extension belongs.
     * 
     * @return the path of the container to which the underlying extension belongs
     */
    ComponentPath containerPath();

    /**
     * Returns an extension of the specified type. The specified type must be among the dependencies of underlying
     * extension. Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method is similar to {@link ContainerConfiguration#use(Class)}. However, this method also makes sure that the
     * extension do request any other extensions that are in the set of its dependencies. Thereby potentially forming cycles
     * in the dependency graph.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws InternalExtensionException
     *             if the specified extension type is not among the dependencies of this extension. Or if calling this
     *             method from the constructor of the extension. Or if the underlying container is no longer configurable
     *             and an extension of the specified type has not already been installed
     * 
     * @see ContainerConfiguration#use(Class)
     */
    <E extends Extension> E use(Class<E> extensionType);

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see BaseBundle#installInstance(Object)
     */
    <T> ComponentConfiguration<T> installInstance(T instance);
}
