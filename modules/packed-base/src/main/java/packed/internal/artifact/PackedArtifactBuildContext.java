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

import app.packed.artifact.ArtifactSource;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerComposer;
import packed.internal.container.PackedExtensionContext;
import packed.internal.errorhandling.ErrorMessage;

/** The default implementation of {@link ArtifactBuildContext} */
public final class PackedArtifactBuildContext implements ArtifactBuildContext {

    /** The build output. */
    private final BuildOutput output;

    /** The configuration of the top container. */
    private final ContainerComposer topContainerConfiguration;

    private final ExtensionList extensions = new ExtensionList();

    /**
     * Creates a new build context object.
     * 
     * @param topContainerConfiguration
     *            the configuration of the artifact's top container
     * @param output
     *            the output of the build process
     */
    public PackedArtifactBuildContext(ContainerComposer topContainerConfiguration, BuildOutput output) {
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

    /**
     * Registers the specified extension type for first use. This method is invoked the first time an extension is used for
     * in every container.
     * 
     * @param pec
     *            the context of the extension that was used.
     */
    public void usesExtension(PackedExtensionContext pec) {
        extensions.extensions.add(pec.model());
    }

    public void forEachExtension() {

    }

    /**
     * Returns the build output.
     * 
     * @return the build output
     */
    public BuildOutput output() {
        return output;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ArtifactSource> sourceType() {
        return topContainerConfiguration.sourceType();
    }
}
