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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.artifact.ArtifactImage;
import packed.internal.artifact.AssembleOutput;
import packed.internal.container.PackedContainerConfigurationContext;

/**
 *
 */
// Vi pakker altid containere ind i future configuration.
// F.eks. kan vi jo tilfoeje et image 2 steder
// Hvilke resultere i to forskellige paths.

// Der er ikke nogen vej udenom
public class PackedGuestConfigurationContext extends PackedComponentConfigurationContext {

    public final PackedContainerConfigurationContext delegate;

    PackedGuestConfigurationContext(PackedHostConfigurationContext host, PackedContainerConfigurationContext pcc, ArtifactImage image) {
        super(PackedComponentDriver.defaultComp(), pcc.configSite(), host, pcc, AssembleOutput.image());
        this.delegate = requireNonNull(pcc);
        this.description = pcc.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        if (delegate.name != null) {
            return delegate.name;
        }
        return delegate.initializeNameDefaultName();
    }
}
