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
package packed.internal.host;

import static java.util.Objects.requireNonNull;

import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactImage;
import app.packed.artifact.HostConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.moduleaccess.ModuleAccess;

/**
 *
 */
public class PackedHostConfiguration extends AbstractComponentConfiguration implements HostConfiguration {

    /**
     * @param configSite
     * @param parent
     */
    public PackedHostConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent) {
        super(configSite, parent);
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(ContainerSource source, ArtifactDriver<?> driver, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        requireNonNull(driver, "driver is null");
        ArtifactImage img = (ArtifactImage) source;
        PackedContainerConfiguration pcc = ModuleAccess.artifact().getConfiguration(img);
        addChild(new ContainerFutureConfiguration(this, pcc, img));
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        return "Host";
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
        return new PackedHost(parent, this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfiguration setName(String name) {
        super.setName(name);
        return this;
    }
}
