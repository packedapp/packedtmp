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
package app.packed.artifact;

import java.lang.reflect.Type;

import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import packed.internal.container.ContainerSource;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 * Ideen er at man implementere en driver. Som tager sig af at laver en artifact instance.... Dvs. kalder image hvis det
 * er et image
 */

// Ditch interface + ArtifactType

// Men kun hvis vi ikke skal bruge den til

// Hmm maaske maaske ikke
// ArtifactBuilder???

// configuration
//// forbidden extensions (lifecycle primarily)
//// Allow injection of ArtifactInstance (for example, App).
//// In which case it will be injectable into any component...
public abstract class ArtifactDriver<T> {

    // private final Class<T> type;

    // private final
    /** Creates a new driver. */
    protected ArtifactDriver() {
        // Set tmp
        configure();
        // convert tmp to perm
        // create() should check that perm is non-null
    }

    protected void configure() {
        // Alternativ
        // @ArtifactDriver.Limitations(forbiddenExtensions(LifecycleExtension.class)

        // Hvordan sikre vi os at configure er koert?????
        // Bruger instantitere den jo selv...

        // Taenker vi godt kan kalde den fra constructeren....

        // Either a configure() class
        // For example, supports lifecycle... if not-> Lifecycle cycle methods on
        // PackedContainer (Artifact?) throws Unsupported

        // Needs Lifecycle
    }

    protected final void disableExtensions(Class<?>... extensions) {

    }

    @SuppressWarnings("unchecked")
    public final Class<T> type() {
        Type type = TypeVariableExtractorUtil.findTypeParameterUnsafe(getClass(), ArtifactDriver.class, 0);
        return (Class<T>) type;
    }

    /**
     * Creates a new artifact.
     * 
     * @param source
     *            the source of the artifact
     * @param wirelets
     *            any wirelets used to create the artifact
     * @return the new artifact
     */
    public final T create(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).newArtifact(this, wirelets);
        }
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(ArtifactType.APP, ContainerSource.forApp(source), wirelets);
        return newArtifact(pcc.doBuild().doInstantiate(WireletList.of()));
    }

    // protected abstract T newDescriptor(PackedConfiguration container);

    /**
     * Creates a new artifact using the specified context.
     * 
     * @param context
     *            the context to wrap
     * @return the new artifact
     */
    protected abstract T newArtifact(ArtifactContext context);
}
