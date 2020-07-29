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
import app.packed.artifact.ArtifactSource;
import app.packed.component.ComponentDescriptor;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.container.Wirelet;
import packed.internal.artifact.PackedArtifactImage;
import packed.internal.component.PackedComponentConfigurationContext;

/**
 * The defa
 */
// We don't actually store the HostConfiguration in this class.

public final class PackedHostConfigurationContext extends PackedComponentConfigurationContext {

    /**
     * @param configSite
     * @param parent
     */
    public PackedHostConfigurationContext(ConfigSite configSite, PackedComponentConfigurationContext parent) {
        super(ComponentDescriptor.COMPONENT_INSTANCE, configSite, parent);
    }

    public void deploy(ArtifactSource source, ArtifactDriver<?> driver, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        requireNonNull(driver, "driver is null");

        PackedArtifactImage img;
        if (source instanceof PackedArtifactImage) {
            img = ((PackedArtifactImage) source).with(wirelets);
        } else {
            img = PackedArtifactImage.of((ContainerBundle) source, wirelets);
        }

        PackedGuestConfigurationContext pgc = new PackedGuestConfigurationContext(this, img.configuration(), img);
        pgc.initializeName(State.LINK_INVOKED, null);

        addChild(pgc);
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        // Vi burde kunne extract AppHost fra <T>
        return "Host"; // Host for now, But if we have host driver...
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfigurationContext setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfigurationContext setName(String name) {
        super.setName(name);
        return this;
    }
}
