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

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;
import app.packed.container.WireletList;

/**
 * A instance of this interface is available to an extension via {@link Extension#context()}. Since the extension itself
 * defines all methods in this interface via protected methods. This interface is typically used to be able to delegate.
 * For example, if the extension is to complex to be contained in a single class.
 */
public interface ExtensionContext {

    ArtifactBuildContext buildContext();

    /**
     * Returns the config site of the container in which the extension is registered in.
     * 
     * @return the config site of the container in which the extension is registered in
     */
    ConfigSite containerConfigSite();

    void putIntoInstantiationContext(ArtifactInstantiationContext context, Object sidecar);

    /**
     * Checks that the underlying extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extension's {@link Extension#onConfigured()} method has been invoked
     * by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

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

    WireletList wirelets();
}
