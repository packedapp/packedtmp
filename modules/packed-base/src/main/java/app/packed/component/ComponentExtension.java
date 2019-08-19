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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.container.PackedContainerConfiguration;

/**
 * An extension that provides basic functionality for installing components in a container.
 */
public final class ComponentExtension extends Extension {

    /** The configuration of the container. */
    private final PackedContainerConfiguration configuration;

    /** Creates a new component extension. */
    ComponentExtension(PackedContainerConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    // install
    // noget med Main, Entry points....
    // Man kan f.eks. disable et Main.... EntryPointExtension....

    // @Main skal jo pege et paa en eller anden extension...

    // Selvfoelelig er det hele komponenter... Ogsaa scoped
    // Vi skal ikke til at have flere scans...

    public ComponentConfiguration install(Class<?> implementation) {
        return configuration.install(implementation);
    }

    // AllowRuntimeInstallationOfComponents();

    // @Scoped
    // @Install()

    // Why export, Need to export

    /**
     * Creates a link to another container represented by a bundle.
     * <p>
     * All links made using this method are permanent. If you need dynamic stuff you can use hosts and applications.
     * 
     * @param bundle
     *            a bundle representing the child
     * @param wirelets
     *            optional wirelets
     */
    public void link(Bundle bundle, Wirelet... wirelets) {
        // I think I want to move this back to ContainerConfiguration
        configuration.link(bundle, wirelets);
    }

    public ComponentConfiguration install(Factory<?> factory) {
        return configuration.install(factory);
    }

    public ComponentConfiguration install(Object instance) {
        return configuration.install(instance);
    }

    public ComponentConfiguration installHelper(Class<?> implementation) {
        return configuration.installHelper(implementation);
    }

    public void scan(String... packages) {}

    // Alternative to ComponentScan
    public void scanForInstall(Class<?>... classesInPackages) {}
}
