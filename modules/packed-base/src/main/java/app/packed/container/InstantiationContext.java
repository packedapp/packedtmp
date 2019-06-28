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

import app.packed.util.Nullable;

/**
 * An instantiation context is created for every delimited tree hierachy.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
// ArtifactInstantiationContext
public interface InstantiationContext {

    /**
     * Returns the type of artifact that is being instantiated. Is either {@link ArtifactType#APP} or
     * {@link ArtifactType#INJECTOR}.
     * 
     * @return the type of artifact that is being instantiated
     */
    ArtifactType artifactType();

    @Nullable
    <T> T get(ContainerConfiguration configuration, Class<T> type);

    void put(ContainerConfiguration configuration, Object obj);

    <T> T use(ContainerConfiguration configuration, Class<T> type);

    /**
     * Returns a list of wirelets that used to instantiate.
     * 
     * @return a list of wirelets that used to instantiate
     */
    WireletList wirelets();
}
