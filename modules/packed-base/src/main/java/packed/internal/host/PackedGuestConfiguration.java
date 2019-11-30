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
import app.packed.component.ComponentType;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
// Vi pakker altid containere ind i future configuration.
// ISaeer pga images, hvor vi f.eks. har et permanent name i imaged
// Men vi vil jo gerne aendre det.
// Bl.a. fordi jo skal store wirelets et sted. Det kan ikke vaere i hosten.
// Og vi kan heller ikke goere det i guesten, hvis den f.eks. er et image.
// Saa vi store den midt imellem.
public class PackedGuestConfiguration extends AbstractComponentConfiguration {

    public final PackedContainerConfiguration delegate;

    PackedGuestConfiguration(PackedHostConfiguration host, PackedContainerConfiguration pcc, ArtifactImage image) {
        super(pcc.configSite(), host, pcc, BuildOutput.image());
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
    protected AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
        return delegate.instantiate(parent, ic);
        // Maaske, kan vi tilgaengeaeld nogen gange instantiered en PackedContainer direkte herfra...

        // return delegate.instantiate();
        // return new ContainerFuture(parent, this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentType type() {
        return delegate.type();
    }
}
