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
package app.packed.container.extension;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;

/**
 * A instance of this interface is available to an extension via {@link Extension#context()}. Since the extension itself
 * defines most methods in this interface via protected final methods. This interface is typically used to be able to
 * provide this methods to code that is not located on the extension implementation. This is typically the case if the
 * extension is too complex to contain in a single class.
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
     * Returns the config site of the container to which the extension belongs.
     * 
     * @return the config site of the container to which the extension belongs
     */
    ConfigSite containerConfigSite();

    /**
     * Returns the path of the container to which the extension belongs.
     * 
     * @return the path of the container to which the extension belongs
     */
    ComponentPath containerPath();

    /**
     * Returns an extension of the specified type.
     * <p>
     * Invoking this method is similar to calling {@link ContainerConfiguration#use(Class)}. However, this method also keeps
     * track of which extensions uses other extensions. And forming any kind of circle in the dependency graph will fail
     * with a runtime exception.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and an extension of the specified type has not
     *             already been installed
     */
    <E extends Extension> E use(Class<E> extensionType);
}
