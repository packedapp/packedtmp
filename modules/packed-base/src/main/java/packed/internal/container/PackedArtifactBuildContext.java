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

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactSource;
import app.packed.config.ConfigSite;
import app.packed.container.WireletList;

/** The default implementation of {@link ArtifactBuildContext} */
final class PackedArtifactBuildContext implements ArtifactBuildContext {

    /** The artifact's driver. */
    private final ArtifactDriver<?> driver;

    /** The configuration of the artifacts top container. */
    private final PackedContainerConfiguration topContainerConfiguration;

    /**
     * Creates a new build context object.
     * 
     * @param topContainerConfiguration
     *            the configuration of the artifact's top container
     * @param driver
     *            the driver of the artifact
     */
    PackedArtifactBuildContext(PackedContainerConfiguration topContainerConfiguration, ArtifactDriver<?> driver) {
        this.topContainerConfiguration = requireNonNull(topContainerConfiguration);
        this.driver = requireNonNull(driver);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> artifactType() {
        return driver.artifactType();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return topContainerConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInstantiating() {
        return driver.isInstantiating();
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactSource source() {
        return topContainerConfiguration.configurator.source;
    }

    /** {@inheritDoc} */
    @Override
    public WireletList wirelets() {
        return topContainerConfiguration.wirelets();
    }
}
