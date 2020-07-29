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
package packed.internal.host.api;

import java.util.Optional;

import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactSource;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.Wirelet;

/**
 *
 * This class is normally never extended by users
 */
// Behoever altsaa ikke vaere et interfaace...
// Vi implementere ikke component configuration direkte her.
// Mere fordi vi ikke gider have den dukker op

// @ExtensiableApi -> API if you extends this class you are whacked

// Eller ogsaa kan man bruge hostdriver.doStuff(hostConfiguration)
public interface HostConfigurationContext {

    /**
     * Checks that the host is still configurable or throws an {@link IllegalStateException}.
     * 
     * @throws IllegalStateException
     *             if the host is no long configurable.
     */
    void checkConfigurable();

    /**
     * Returns the configuration site that created this configuration.
     * 
     * @return the configuration site that created this configuration
     */
    ConfigSite configSite();

    // Do we need an ArtifactReference???
    // Kunne ogsaa godt bruge noget fra Bundle.link

    // Will be made available from guests
    void deploy(ArtifactSource source, ArtifactDriver<?> driver, Wirelet... wirelets);

    /**
     * If this component has been installed from an extension, returns the extension. Otherwise returns empty.
     * 
     * @return any extension this component belongs to
     */
    Optional<Class<? extends Extension>> extension();

    /**
     * Returns the description of this component. Or null if the description has not been set.
     *
     * @return the description of this component. Or null if the description has not been set.
     * @see #setDescription(String)
     * @see Component#description()
     */
    @Nullable
    String getDescription();

    /**
     * Returns the name of the component. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the component
     * @see #setName(String)
     */
    String getName();

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #setName(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    ComponentPath path();

    /**
     * Sets the description of this component.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @see #getDescription()
     * @see Component#description()
     */
    HostConfigurationContext setDescription(String description);

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique name other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    HostConfigurationContext setName(String name);
}