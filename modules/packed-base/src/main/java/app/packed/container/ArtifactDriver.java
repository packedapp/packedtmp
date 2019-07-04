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
package app.packed.container;

import app.packed.app.App;
import packed.internal.container.ContainerSource;
import packed.internal.container.PackedApp;
import packed.internal.container.PackedContainer;
import packed.internal.container.PackedContainerConfiguration;

/**
 * Ideen er at man implementere en driver. Som tager sig af at laver en artifact instance.... Dvs. kalder image hvis det
 * er et image
 */
// AbstractArtifact skal vi naesten ogsaa have saa.
// Skal artifact implementere Artifact.... Hvad hvis jeg ikke
// gider expose det.... F.eks. ud mod mine brugere....
// Don't poke in stream()..
public abstract class ArtifactDriver<T extends Artifact> {

    public final Class<T> type() {
        // We cache this when we create the driver...
        throw new UnsupportedOperationException();
    }

    // Needs Lifecycle
    public final T create(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).newArtifact(this, wirelets);
        }
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(ArtifactType.APP, ContainerSource.forApp(source), wirelets);
        return create(pcc.doBuild().doInstantiate());
    }

    @SuppressWarnings("exports")
    public abstract T create(PackedContainer container);
}

class AppArtifactDriver extends ArtifactDriver<App> {

    /** {@inheritDoc} */
    @Override
    public App create(PackedContainer container) {
        return new PackedApp(container);
    }
}
