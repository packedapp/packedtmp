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
 *
 */
public interface ExtensionContext {

    void checkConfigurable();

    ConfigSite containerConfigSite();

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

    ArtifactBuildContext buildContext();

    void putIntoInstantiationContext(ArtifactInstantiationContext context, Object sidecar);

    WireletList wirelets();
}
