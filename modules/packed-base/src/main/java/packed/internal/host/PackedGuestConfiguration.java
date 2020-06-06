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

import app.packed.artifact.ArtifactImage;
import app.packed.component.ComponentDescriptor;
import packed.internal.artifact.AssembleOutput;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
// Vi pakker altid containere ind i future configuration.
// F.eks. kan vi jo tilfoeje et image 2 steder
// Hvilke resultere i to forskellige paths.

// Der er ikke nogen vej udenom
public class PackedGuestConfiguration extends AbstractComponentConfiguration {

    public final PackedContainerConfiguration delegate;

    PackedGuestConfiguration(PackedHostConfiguration host, PackedContainerConfiguration pcc, ArtifactImage image) {
        super(pcc.configSite(), host, pcc, AssembleOutput.image());
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

    /** {@inheritDoc} */
    @Override
    protected AbstractComponent instantiate(AbstractComponent parent, PackedInstantiationContext ic) {
        return delegate.instantiate(parent, ic);
        // Maaske, kan vi tilgaengeaeld nogen gange instantiered en PackedContainer direkte herfra...

        // return delegate.instantiate();
        // return new ContainerFuture(parent, this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDescriptor type() {
        return delegate.type();
    }
}
