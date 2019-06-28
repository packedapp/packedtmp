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
     * Returns the description of this artifact. Or an empty optional if no description was set
     * <p>
     * The description is always identical to the description of the root container in the artifact.
     *
     * @return the description of this artifact. Or an empty optional if no description was set
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the name of this artifact.
     * <p>
     * The name is always identical to the name of the root container in the artifact.
     * <p>
     * If no name is explicitly set when creating the artifact, the runtime will generate a name that is among any siblings.
     * 
     * @return the name of this artifact
     */
    String name();

    /**
     * Returns the path of this artifact.
     * <p>
     * The path is always identical to the path of the root container in the artifact.
     *
     * @return the path of this artifact
     * @see Component#path()
     */
    ComponentPath path();

    // Stream();
}
