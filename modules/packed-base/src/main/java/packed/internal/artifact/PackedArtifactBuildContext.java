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
package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import app.packed.errorhandling.ErrorMessage;
import packed.internal.container.PackedContainerConfiguration;

/** The default implementation of {@link ArtifactBuildContext} */
public final class PackedArtifactBuildContext implements ArtifactBuildContext {

    /** The artifact's driver. */
    private final BuildOutput output;

    /** The configuration of the artifacts top container. */
    private final PackedContainerConfiguration topContainerConfiguration;

    /**
     * Creates a new build context object.
     * 
     * @param topContainerConfiguration
     *            the configuration of the artifact's top container
     * @param output
     *            the output of the build process
     */
    public PackedArtifactBuildContext(PackedContainerConfiguration topContainerConfiguration, BuildOutput output) {
        this.topContainerConfiguration = requireNonNull(topContainerConfiguration);
        this.output = requireNonNull(output);
    }

    /** {@inheritDoc} */
    @Override
    public void addError(ErrorMessage message) {}

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return topContainerConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInstantiating() {
        return false;
    }

    public BuildOutput output() {
        return output;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ContainerSource> sourceType() {
        return topContainerConfiguration.source.getClass();
    }
}
