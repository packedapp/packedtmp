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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.app.App;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.inject.Injector;
import packed.internal.container.ComponentConfigurationToComponentAdaptor;
import packed.internal.container.ComponentNameWirelet;
import packed.internal.container.ContainerSource;
import packed.internal.container.PackedApp;
import packed.internal.container.PackedContainerConfiguration;

/**
 * Artifact images are immutable ahead-of-time configured {@link Artifact artifacts}. By configuring an artifact ahead
 * of time, the actual time to instantiation an artifact can be severely decreased.
 * 
 * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
 * by using artificat images you can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases:
 * 
 * 
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link App}, {@link Injector}, {@link BundleDescriptor} or other
 * artifact images. It can not be used with {@link ContainerBundle#link(ContainerBundle, Wirelet...)}.
 */
public final class ArtifactImage implements ArtifactSource {

    /** The configuration of the future artifact's root container. */
    private final PackedContainerConfiguration containerConfiguration;

    /** Additional wirelets */
    private final WireletList wirelets;

    /**
     * Creates a new image from the specified configuration.
     * 
     * @param containerConfiguration
     *            the configuration this image will wrap
     */
    private ArtifactImage(PackedContainerConfiguration containerConfiguration) {
        this(containerConfiguration, WireletList.of());
    }

    private ArtifactImage(PackedContainerConfiguration containerConfiguration, WireletList wirelets) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
        this.wirelets = requireNonNull(wirelets);
    }

    /**
     * Returns the configuration site of this image.
     * 
     * @return the configuration site of this image
     */
    public ConfigSite configSite() {
        return containerConfiguration.configSite();
    }

    public Optional<String> description() {
        return Optional.ofNullable(containerConfiguration.getDescription());
    }

    public String name() {
        Optional<ComponentNameWirelet> nw = wirelets.last(ComponentNameWirelet.class);
        return nw.map(e -> e.name).orElse(containerConfiguration.getName());
    }

    public String name2() {
        ComponentNameWirelet nw = wirelets.lastOrNull(ComponentNameWirelet.class);
        return nw == null ? containerConfiguration.getName() : nw.name;
    }

    App newApp(Wirelet... wirelets) {
        WireletList.of(wirelets);
        return new PackedApp(containerConfiguration.doInstantiate(this.wirelets));
    }

    <T> T newArtifact(ArtifactDriver<T> driver, Wirelet... wirelets) {
        return driver.newArtifact(containerConfiguration.doInstantiate(this.wirelets.plus(wirelets)));
    }

    public Injector newInjector(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
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
    public Class<? extends ContainerBundle> sourceType() {
        throw new UnsupportedOperationException();
    }

    public ComponentStream stream() {
        return new ComponentConfigurationToComponentAdaptor(containerConfiguration).stream();
    }

    public ArtifactImage with(Wirelet... wirelets) {
        return new ArtifactImage(containerConfiguration, this.wirelets.plus(wirelets));
    }

    /**
     * Returns a new container image original functionality but re
     * 
     * @param name
     *            the name
     * @return the new container image
     */
    public final ArtifactImage withName(String name) {
        return with(Wirelet.name(name));
    }

    /**
     * Creates a new image from the specified source.
     *
     * @param source
     *            the source of the image
     * @param wirelets
     *            any wirelets to use in the consWtruction of the image
     * @return a new image
     * @throws RuntimeException
     *             if the image could not be constructed properly
     */
    public static ArtifactImage of(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).with(wirelets);
        }
        // Wirelet are added to the container configuration, and not the imge
        PackedContainerConfiguration c = new PackedContainerConfiguration(ArtifactType.ARTIFACT_IMAGE, ContainerSource.forImage(source), wirelets);
        return new ArtifactImage(c.doBuild());
    }

    interface InjectorFactory {
        // Tager disse to objekter, laver en injector fra bundlen.
        // Og outputter String
        String spawn(long str1, int str2);

        Injector spawn(String httpRequest, String httpResponse);
    }

    interface UserDefinedSpawner {
        // App spawn(Host h, String httpRequest, String httpResponse);
    }
}
// ofRepeatable();
// wirelet.checkNotFrom();
//
/// **
// * Returns the configuration site of this artifact.
// *
// * @return the configuration site of this artifact
// */
// ConfigSite configSite();
//
/// **
// * Returns the description of this artifact. Or an empty optional if no description has been set
// * <p>
// * The returned description is always identical to the description of the artifact's root container.
// *
// * @return the description of this artifact. Or an empty optional if no description has been set
// *
// * @see ComponentConfiguration#setDescription(String)
// */
// Optional<String> description();
//
/// **
// * Returns the name of this artifact.
// * <p>
// * The returned name is always identical to the name of the artifact's root container.
// * <p>
// * If no name is explicitly set when creating the artifact, the runtime will generate a name that guaranteed to be
// * unique among any of the artifact's siblings.
// *
// * @return the name of this artifact
// */
// String name();