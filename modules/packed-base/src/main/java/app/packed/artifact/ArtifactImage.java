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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import packed.internal.container.ComponentConfigurationToComponentAdaptor;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;
import packed.internal.module.AppPackedArtifactAccess;
import packed.internal.module.ModuleAccess;
import packed.internal.container.NonInstantiatingArtifactDriver;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.WireletContext;
import packed.internal.container.WireletList;

/**
 * Artifact images are immutable ahead-of-time configured artifacts. By configuring an artifact ahead of time, the
 * actual time to instantiation an artifact can be severely decreased often down to a couple of microseconds. In
 * addition to this, artifact images are reusable, so you can create multiple artifacts from a single image.
 * 
 * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
 * by using artifact images you can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases: Extremely fast startup.. graal
 * 
 * Instantiate the same container many times
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link app.packed.app.App}, {@link app.packed.service.Injector},
 * {@link BundleDescriptor} or other artifact images. It can not be used with {@link Bundle#link(Bundle, Wirelet...)}.
 */
public final class ArtifactImage implements ContainerSource {

    static {
        ModuleAccess.initialize(AppPackedArtifactAccess.class, new AppPackedArtifactAccess() {

            /** {@inheritDoc} */
            @Override
            public PackedContainerConfiguration getConfiguration(ArtifactImage image) {
                return image.containerConfiguration;
            }
        });
    }

    /** The configuration of the root container of the artifact. */
    private final PackedContainerConfiguration containerConfiguration;

    private final Class<? extends ContainerSource> sourceType;

    /** Additional wirelets. */
    // Vi evaluere them naar
    private final WireletList wirelets;

    WireletContext wl;

    /**
     * Creates a new image from the specified configuration.
     * 
     * @param containerConfiguration
     *            the configuration this image will wrap
     */
    private ArtifactImage(PackedContainerConfiguration containerConfiguration, Class<? extends ContainerSource> containerSource) {
        this(containerConfiguration, WireletList.of(), containerSource);
    }

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param containerConfiguration
     *            the configuration this image will wrap
     * @param wirelets
     *            any wirelets for the image configuration or artifact instantiation
     */
    private ArtifactImage(PackedContainerConfiguration containerConfiguration, WireletList wirelets, Class<? extends ContainerSource> containerSource) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
        this.wirelets = requireNonNull(wirelets);
        this.sourceType = requireNonNull(containerSource);
    }

    /**
     * Returns the configuration site of this image.
     * 
     * @return the configuration site of this image
     */
    public ConfigSite configSite() {
        return containerConfiguration.configSite();
    }

    /**
     * Returns any description that has been set for the image.
     * <p>
     * The returned description is always identical to the description of the artifact's root container.
     * 
     * @return any description that has been set for the image
     * @see ContainerConfiguration#setDescription(String)
     */
    public Optional<String> description() {
        return Optional.ofNullable(containerConfiguration.getDescription());
    }

    /**
     * Returns the name of this artifact.
     * <p>
     * The returned name is always identical to the name of the artifact's root container.
     * <p>
     * If no name is explicitly set when creating the artifact, the runtime will generate a name that guaranteed to be
     * unique among any of the artifact'ssiblings.**@return the name of this artifact
     */
    public String name() {
        return wirelets.findLast(ComponentNameWirelet.class).map(e -> e.name).orElse(containerConfiguration.getName());
    }

    public String name2() {
        ComponentNameWirelet nw = wirelets.findLastOrNull(ComponentNameWirelet.class);
        return nw == null ? containerConfiguration.getName() : nw.name;
    }

    <T> T newArtifact(ArtifactDriver<T> driver, Wirelet... wirelets) {
        return driver.instantiate(containerConfiguration.doInstantiate(this.wirelets.plus(wirelets)));
    }

    /**
     * Returns the type of bundle that was used to create this image.
     * <p>
     * If this image was created from an existing image, the new image image will retain the original image source bundle
     * type.
     * 
     * @return the original source type of this image
     */
    // sourceType?? bundleType.. Igen kommer lidt an paa den DynamicContainerSource....
    @SuppressWarnings("unchecked")
    public Class<? extends Bundle> sourceType() {
        return (Class<? extends Bundle>) sourceType;
    }

    public ComponentStream stream() {
        return new ComponentConfigurationToComponentAdaptor(containerConfiguration).stream();
    }

    /**
     * Returns a new artifact image.
     * <p>
     * The specified wirelets are never evaluated until the new image is used to create a new artifact or bundle descriptor.
     * 
     * @param wirelets
     *            the wirelets to prefix
     * @return a new image
     */
    // If we use pipeline, the wirelets will be evaluated
    public ArtifactImage with(Wirelet... wirelets) {
        WireletList wl = this.wirelets.plus(wirelets);
        return wirelets.length == 0 ? this : new ArtifactImage(containerConfiguration, wl, sourceType);
    }

    /**
     * Creates a new image from the specified source.
     *
     * @param source
     *            the source of the image
     * @param wirelets
     *            any wirelets to use when construction the image. The wirelets will also be available when instantiating an
     *            actual artifact
     * @return the new image
     * @throws RuntimeException
     *             if the image could not be constructed
     */
    public static ArtifactImage of(ContainerSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).with(wirelets);
            // throw new UnsupportedOperationException("Cannot create a new image, from an existing image");
        }
        // Wirelet are added to the container configuration, and not the image
        PackedContainerConfiguration c = new PackedContainerConfiguration(ArtifactImageArtifactDriver.INSTANCE, source, wirelets);
        return new ArtifactImage(c.doBuild(), source.getClass());
    }
}

/** An dummy artifact driver for creating artifact images. */
// Den bliver kun brugt i forbindelse med build context...
// Vil godt af med den paa en eller anden maade
final class ArtifactImageArtifactDriver extends NonInstantiatingArtifactDriver<ArtifactImage> {

    /** The single instance. */
    static final ArtifactImageArtifactDriver INSTANCE = new ArtifactImageArtifactDriver();

    /** Singleton */
    private ArtifactImageArtifactDriver() {}
}

// De kunne jo strength taget vaere metoder paa imaged og ikke wirelets.
// Vi kan jo sagtens internt lave det om til wirelets...
// Der er bare ingen grund til at lave det public...
final class ArtifactImageWirelets {

    // retainStackTracesForEachInstantiation...
    /// Her ligger vi jo lige 1000 ns oveni hvis vi vil se hvor den er instantieret.

    // Maximum number of instantiations times...
    // Could, for example, be one for native.
    // The only think we want to instantiate the application once... And then forget everything

    // Ideen er at vi kun skal lave en container en gang. F.eks. NativeBoot
    static Wirelet oneShot() {
        throw new UnsupportedOperationException();
    }
}
