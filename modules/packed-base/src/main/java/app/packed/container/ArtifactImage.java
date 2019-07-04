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
import packed.internal.container.PackedApp;
import packed.internal.container.PackedContainerConfiguration;

/** The default implementation of {@link ArtifactImageInterface}. */
// TODO Drop the interface, we only want the class
public final class ArtifactImage implements ArtifactImageInterface {

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

    public <T extends Artifact> T newArtifact(ArtifactDriver<T> driver, Wirelet... wirelets) {
        return driver.create(containerConfiguration.doInstantiate());
    }

    public ArtifactImageInterface newImage(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public Injector newInjector(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ContainerBundle> sourceType() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactImageInterface with(Wirelet... wirelets) {
        // We need to check that they can be used at image instantion time.
        throw new UnsupportedOperationException();
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

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return new ComponentConfigurationToComponentAdaptor(containerConfiguration).stream();
    }
}
