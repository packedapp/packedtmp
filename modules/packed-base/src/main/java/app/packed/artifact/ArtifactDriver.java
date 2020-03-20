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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.service.Injector;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactImage;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 * Artifact drivers are responsible for creating new artifacts by wrapping instances of {@link ArtifactContext}.
 * <p>
 * This class can be extended to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link Injector} are not sufficient. In fact, the default implementations of both {@link App} and {@link Injector}
 * are just thin facade that delegates all calls to {@link ArtifactContext}. *
 * <p>
 * Normally, you should never instantiate more then a single instance of a subclass of this class.
 * <p>
 * Subclasses of this class should be thread safe.
 * 
 * @param <T>
 *            The type of artifact this driver creates.
 * @see App#driver()
 */
// Support of injection of the artifact into the Container...
// We do not generally support this, as people are free to any artifact they may like.
public abstract class ArtifactDriver<T> {

    /** A type variable extractor for the type of artifact this driver creates. */
    private static final TypeVariableExtractor ARTIFACT_DRIVER_TV_EXTRACTOR = TypeVariableExtractor.of(ArtifactDriver.class);

    /** The type of artifact this driver produces. */
    private final Class<T> artifactType;

    private final boolean isExecutable;

    /** Creates a new driver. */
    @SuppressWarnings("unchecked")
    protected ArtifactDriver() {
        this.artifactType = (Class<T>) ARTIFACT_DRIVER_TV_EXTRACTOR.extract(getClass());
        this.isExecutable = AutoCloseable.class.isAssignableFrom(artifactType);
        // Set tmp
        configure();
        // convert tmp to perm
        // create() should check that perm is non-null
    }

    /**
     * Returns the type of artifact this driver produce.
     * 
     * @return the type of artifact this driver produce
     */
    // RawType? Should we have a TypeLiteral???
    // For example, I create BigMap<K, V>
    public final Class<T> artifactType() {
        return artifactType;
    }

    protected void configure() {
        // Default Constraints....

        // Kan vi bruge noget af det samme som ComponentExtension...
        // De regler kan vel bruges paa baadde
        // extension nivuea, ArtifactNiveau, Bundle Niveau

        // configuration
        //// forbidden extensions (lifecycle primarily)
        //// Allow injection of ArtifactInstance (for example, App).
        //// In which case it will be injectable into any component...

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

    /**
     * Creates a new artifact using the specified source.
     * <p>
     * This method will invoke {@link #newArtifact(ArtifactContext)} to create the actual artifact.
     * 
     * @param source
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets used to create the artifact
     * @return the new artifact
     * @throws RuntimeException
     *             if the artifact could not be created
     */
    public final T createAndInitialize(Assembly source, Wirelet... wirelets) {
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newArtifact(this, wirelets);
        }
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(BuildOutput.artifact(this), source, wirelets);
        pcc.doBuild();
        ArtifactContext pac = pcc.instantiateArtifact(pcc.wireletContext).newArtifactContext();
        return newArtifact(pac);
    }

    public final T create(Assembly source, Wirelet[] userWirelets, Wirelet... artifactWirelets) {
        // Ideen er lidt at Artifact Implementering, kan kalde med dens egen wirelets...
        // ala

        // start(Assembly, Wirelet... wirelets) {
        // create(Assembly, wirelets, ArtifactWirelets.startSynchronous());
        // create(Assembly, wirelets, ArtifactWirelets.startAsynchronous());
        // }
        // What about execute....
        throw new UnsupportedOperationException();
    }

    public final T createAndStart(Assembly source, Wirelet... wirelets) {
        // Should throw if not Runnable...
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newArtifact(this, wirelets);
        }
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(BuildOutput.artifact(this), source, wirelets);
        pcc.doBuild();
        ArtifactContext pac = pcc.instantiateArtifact(pcc.wireletContext).newArtifactContext();
        pac.start();
        return newArtifact(pac);
    }

    final <E extends T> ArtifactDriver<T> decorate(Class<E> decoratingType, Function<T, E> decorator) {
        // Ideen er egentlig at f.eks. kunne wrappe App, og tilfoeje en metode...
        // Men altsaa, maaske er det bare at kalde metoderne direkte i context...
        // PackedApp kalder jo bare direkte igennem
        throw new UnsupportedOperationException();
    }

    protected final void disableExtensions(Class<?>... extensions) {
        // Alternativ skal vi bruge funktionalitet for at lave arkitektur...
        // Det her med at man som et firma kan specificere ting som
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    /**
     * Create a new artifact. This method is normally implemented by the user, and invoked by the runtime in order to create
     * a new artifact.
     * 
     * @param context
     *            the artifact context to wrap
     * @return the new artifact
     */
    protected abstract T newArtifact(ArtifactContext context);

    public final <C> T newArtifact(Function<ContainerConfiguration, C> factory, Consumer<C> consumer, Wirelet... wirelets) {
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(BuildOutput.artifact(this), consumer, wirelets);
        C c = factory.apply(pcc);
        consumer.accept(c);
        pcc.doBuild();
        ArtifactContext pac = pcc.instantiateArtifact(pcc.wireletContext).newArtifactContext();
        return newArtifact(pac);
    }

    // Taget som en ArtifactDriver(Option... options)
    static class Option {

        static Option disableExtensions(Class<? extends Extension> extensions) {
            throw new UnsupportedOperationException();
        }
    }

    // Lav en constructor baade med og uden Alt
    // ArtifactDriver(boolean isRunning)
    // ArtifactDriver(boolean isRunning, Settings settings)
    static abstract class Settings {
        protected abstract void configure();

        protected final void disableExtensions() {

        }
    }
}
