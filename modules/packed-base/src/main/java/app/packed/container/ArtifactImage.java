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
import packed.internal.container.ContainerSource;
import packed.internal.container.PackedApp;
import packed.internal.container.PackedContainerConfiguration;

/**
 * Artifact images are immutable ahead-of-time configured {@link Artifact artifacts}. Creating artifacts in Packed is
 * already really fast, and you can easily create one 10 or hundres of microseconds. But by using artificat images you
 * can into hundres or thousounds of nanoseconds.
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
public final class ArtifactImage implements Artifact, ArtifactSource {

    /** The configuration of the future artifact's root container. */
    private final PackedContainerConfiguration containerConfiguration;

    /**
     * Creates a new image.
     * 
     * @param containerConfiguration
     *            the configuration of the container we wrap
     */
    ArtifactImage(PackedContainerConfiguration containerConfiguration) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return containerConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(containerConfiguration.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        // If we set naming wirelets we need to run through them
        return containerConfiguration.getName();
    }

    public App newApp(Wirelet... wirelets) {
        WireletList.of(wirelets);
        return new PackedApp(containerConfiguration.doInstantiate());
    }

    public <T> T newArtifact(ArtifactDriver<T> driver, Wirelet... wirelets) {
        return driver.newArtifact(containerConfiguration.doInstantiate());
    }

    public ArtifactImage newImage(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
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

    public ArtifactImage with(Wirelet... wirelets) {
        // We need to check that they can be used at image instantion time.
        throw new UnsupportedOperationException();
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

    interface InjectorFactory {
        // Tager disse to objekter, laver en injector fra bundlen.
        // Og outputter String
        String spawn(long str1, int str2);

        Injector spawn(String httpRequest, String httpResponse);
    }

    /**
     * Creates a new image from the specified source.
     *
     * @param source
     *            the source of the image
     * @param wirelets
     *            any wirelets to use in the construction of the image
     * @return a new image
     * @throws RuntimeException
     *             if the image could not be constructed properly
     */
    public static ArtifactImage of(ArtifactSource source, Wirelet... wirelets) {
        if (source instanceof ArtifactImage) {
            return ((ArtifactImage) source).newImage(wirelets);
        }
        PackedContainerConfiguration c = new PackedContainerConfiguration(ArtifactType.ARTIFACT_IMAGE, ContainerSource.forImage(source), wirelets);
        return new ArtifactImage(c.doBuild());
    }

    interface UserDefinedSpawner {
        // App spawn(Host h, String httpRequest, String httpResponse);
    }

    public ComponentStream stream() {
        return new ComponentConfigurationToComponentAdaptor(containerConfiguration).stream();
    }
}
// ofRepeatable();
// wirelet.checkNotFrom();
