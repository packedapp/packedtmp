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

import java.util.List;
import java.util.Optional;

import app.packed.artifact.BuildTarget;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;

/**
 * An instance of this interface is available via {@link Extension#context()} or via constructor injection into an
 * extension. Since the extension itself defines most methods in this interface via protected final methods. This
 * interface is typically used to be able to provide these methods to code that is not located on the extension
 * implementation or in the same package as the extension itself.
 * <p>
 * Instances of this class should never be exposed to end-users.
 */
public interface ExtensionContext {

    // What we are generating...
    // This can be tricky, For example, if we create an image.
    // This method will always return image,
    default BuildTarget buildTarget() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extension's onConfigured method has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

    default void checkPreemble() {
        // Ideen er at man kan checke at der ikke er blevet installeret boern...
        // Men saa kan
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the config site of the container the extension is registered with.
     * 
     * @return the config site of the container the extension is registered with
     */
    ConfigSite containerConfigSite();

    /**
     * Returns the path of the container the extension is registered with.
     * 
     * @return the path of the container the extension is registered with
     */
    ComponentPath containerPath();

    <T> SingletonConfiguration<T> install(Factory<T> factory);

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerComposer#installInstance(Object)
     */
    <T> SingletonConfiguration<T> installInstance(T instance);

    /**
     * Returns an extension of the specified type. The specified type must be among the extension's dependencies as
     * specified via.... Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method works similar to {@link ContainerComposer#use(Class)}. However, this method checks that only extensions
     * that have been declared as dependencies via {@link ExtensionComposer#addDependencies(Class...)} are specified. This
     * is done in order to make sure that no extensions ever depend on each other.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type is not specified via {@link UseExtension} on this extension.
     * 
     * @see ContainerComposer#use(Class)
     */
    <E extends Extension> E use(Class<E> extensionType);

    /**
     * If the container in which this extension is register has a parent container within the same artifact, and the parent
     * has this extension installed. Returns the extension otherwise empty.
     * 
     * @return the parent extension
     */
    Optional<Extension> parent();

    /**
     * @return child extensions
     * 
     * @throws IllegalStateException
     *             if called before the runtime can guarantee that all children has been linked
     */
    default List<Extension> children() {
        throw new UnsupportedOperationException();
    }
}
