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

import java.util.Optional;

import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;

/**
 * An artifact is the output of a build process
 */
// Kunne ContainerConfiguration ogsaa implementere denne????
// Nah
// Der er ogsaa lige noget med noget live...

// A build produces one or more artifacts, such as a container image or an app.
// Or do only produce one?
public interface Artifact {

    /**
     * Returns the configuration site of this artifact.
     * 
     * @return the configuration site of this artifact
     */
    ConfigSite configSite();

    /**
     * Returns the description of this artifact. Or an empty optional if no description has been set
     * <p>
     * The returned description is always identical to the description of the artifact's root container.
     *
     * @return the description of this artifact. Or an empty optional if no description has been set
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the name of this artifact.
     * <p>
     * The returned name is always identical to the name of the artifact's root container.
     * <p>
     * If no name is explicitly set when creating the artifact, the runtime will generate a name that guaranteed to be
     * unique among any of the artifact's siblings.
     * 
     * @return the name of this artifact
     */
    String name();

    /**
     * Returns the component path of this artifact.
     * <p>
     * The returned path is always identical to the path of the artifact's root container.
     *
     * @return the component path of this artifact
     * @see Component#path()
     */
    ComponentPath path();

    ComponentStream stream();
}

// Kan puttes paa en artifact, og angiver hvilke stages man skal koere...
@interface ArtifactModel {

}
// Stream(); Hmm lad os se paa det.. Det kraever jo vi en ens api om vi er i ContainerConfiguration eller Container.
