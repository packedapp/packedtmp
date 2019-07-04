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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.config.ConfigSite;
import app.packed.container.ArtifactBuildContext;
import app.packed.container.ArtifactSource;
import app.packed.container.ArtifactType;
import app.packed.container.WireletList;

/** The default implementation of {@link ArtifactBuildContext} */
final class PackedArtifactBuildContext implements ArtifactBuildContext {

    /** The artifact type. */
    private final ArtifactType artifactType;

    /** The configuration of the artifacts root container. */
    private final PackedContainerConfiguration rootContainerConfiguration;

    /**
     * Creates a new build context object.
     * 
     * @param rootContainerConfiguration
     *            the configuration of the artifacts root container
     * @param artifactType
     *            the type of artifact we are building
     */
    PackedArtifactBuildContext(PackedContainerConfiguration rootContainerConfiguration, ArtifactType artifactType) {
        this.rootContainerConfiguration = requireNonNull(rootContainerConfiguration);
        this.artifactType = requireNonNull(artifactType);
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactType artifactType() {
        return artifactType;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return rootContainerConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactSource source() {
        return rootContainerConfiguration.configurator.source;
    }

    /** {@inheritDoc} */
    @Override
    public WireletList wirelets() {
        return rootContainerConfiguration.wirelets();
    }
}
