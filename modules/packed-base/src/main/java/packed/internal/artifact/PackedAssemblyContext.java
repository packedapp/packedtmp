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
import packed.internal.container.PackedContainerConfigurationContext;
import packed.internal.errorhandling.ErrorMessage;

/** The default implementation of {@link AssembleContext} */
public final class PackedAssemblyContext implements AssembleContext {

    /** The build output. */
    private final AssembleOutput output;

    /** The thread that is assembling the system. */
    private final Thread thread = Thread.currentThread();

    /** The configuration of the top container. */
    private final PackedContainerConfigurationContext topContainerConfiguration;

    /**
     * Creates a new build context object.
     * 
     * @param topContainerConfiguration
     *            the configuration of the artifact's top container
     * @param output
     *            the output of the build process
     */
    public PackedAssemblyContext(PackedContainerConfigurationContext topContainerConfiguration, AssembleOutput output) {
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
     * Returns the build output.
     * 
     * @return the build output
     */
    public AssembleOutput output() {
        return output;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> sourceType() {
        return topContainerConfiguration.sourceType();
    }

    public Thread thread() {
        return thread;
    }
}
