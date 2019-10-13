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

import java.util.function.Function;

import app.packed.app.App;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.service.Injector;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 * This class can be extended to create custom artifact types if the built-in artifact types such as {@link App} and
 * {@link Injector} are not sufficient. In fact both {@link App} and {@link Injector} are just a thin facade that
 * delegates all calls to {@link ArtifactContext}.
 * 
 * An artifact driver is used to create artifact instances such as {@link App} and {@link Injector}. Taking care of
 * initializing internal classes and handling artifact images.
 * 
 * <p>
 * Normally, you should never instantiate more then a single instance of a particular implementation of this class.
 * Subclasses of this class should be thread safe.
 * 
 * @param <T>
 *            The type of artifact this driver creates.
 */
// Support of injection of the artifact into the Container...
// We do not generally support this, as people are free to any artifact they may like.
public abstract class ArtifactDriver<T> {

    /** The type of artifact this driver produces. */
    private final Class<T> artifactType;

    private static final TypeVariableExtractor ARTIFACT_DRIVER_TV_EXTRACTOR = TypeVariableExtractor.of(ArtifactDriver.class);

    /** Creates a new driver. */
    @SuppressWarnings("unchecked")
    protected ArtifactDriver() {
        this.artifactType = (Class<T>) ARTIFACT_DRIVER_TV_EXTRACTOR.extract(getClass());

        // Set tmp
        configure();
        // convert tmp to perm
        // create() should check that perm is non-null
    }

    /**
     * Returns the type of artifacts this driver produce.
     * 
     * @return the type of artifacts this driver produce
     */
    public final Class<T> artifactType() {
        return artifactType;
    }

    protected void configure() {
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

    protected final void disableExtensions(Class<?>... extensions) {
        // Alternativ skal vi bruge funktionalitet for at lave arkitektur...
        // Det her med at man som et firma kan specificere ting som
    }

    /**
     * Instantiates a new artifact. This method is normally implemented by the user, and invoked by the runtime to create a
     * new artifact.
     * 
     * @param context
     *            the runtime context to wrap
     * @return the new artifact
     */
    protected abstract T instantiate(ArtifactContext context);

    /**
     * Creates a new artifact using the specified source.
     * <p>
     * This method will invoke {@link #instantiate(ArtifactContext)} to create the actual artifact.
     * 
     * @param source
     *            the source of the top-level container
     * @param wirelets
     *            any wirelets used to create the artifact
     * @return the new artifact
     * @throws RuntimeException
     *             if the artifact could not be created
     */
    public final T newArtifact(ContainerSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).instantiateArtifact(this, wirelets);
        }
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(this, source, wirelets);
        return instantiate(pcc.doBuild().doInstantiate(pcc.wc));
    }

    public final <C> T newArtifact(Function<ContainerConfiguration, C> factory, ArtifactConfigurator<C> configurator, Wirelet... wirelets) {
        PackedContainerConfiguration pcc = new PackedContainerConfiguration(this, configurator, wirelets);
        C c = factory.apply(pcc);
        configurator.configure(c);
        return instantiate(pcc.doBuild().doInstantiate(pcc.wc));
    }
}
