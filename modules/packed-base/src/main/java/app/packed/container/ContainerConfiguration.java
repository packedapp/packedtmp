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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.bundle.AnyBundle;
import app.packed.bundle.BundleLink;
import app.packed.bundle.WiringOption;
import app.packed.inject.Factory;
import app.packed.util.Nullable;

/**
 * The configuration of a container.
 */
// checkConfigurable??? Or only via extensions?
// Basic functionality
/// Name
/// Extensions
/// Wiring to other containers
/// Attachments
/// Lookup

/// Layers
/// Components?? Or is this an extension.... It would be nice if you could install components from other extensions....
/// Which you cannot currently, as you cannot inject ContainerConfiguration... And what about attachments????
/// Maybe directly on the extension.. containerAttachements().. maybe also use?? Then we can always lazy initialize....
/// And we do not need the fields

// Optional<Class<? extends AnyBundle>> bundleType();
// Environment <- Immutable??, Attachable??
// See #Extension Implementation notes for information about how to make sure it can be instantiated...
public interface ContainerConfiguration {

    /**
     * Returns an immutable view of all of the extension types that are used by this container.
     * 
     * @return an immutable view of all of the extension types that are used by this container
     */
    // Map<Class, Extension>?????
    Set<Class<? extends Extension<?>>> extensionTypes(); // Hmm, kan vi have hidden extensions???

    /**
     * Returns the description of this container. Or null if the description has not been set.
     *
     * @return the description of this container. Or null if the description has not been set.
     * @see #setDescription(String)
     * @see Container#description()
     */
    @Nullable
    String getDescription();

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     */
    @Nullable
    String getName();

    ComponentConfiguration install(Class<?> implementation);

    ComponentConfiguration install(Factory<?> factory);

    ComponentConfiguration install(Object instance);

    ComponentConfiguration installStatics(Class<?> implementation);

    /**
     * Registers a {@link Lookup} object that can is primarily used for accessing fields and methods on registered
     * components.
     * <p>
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    void lookup(Lookup lookup);

    /**
     * Sets the description of this container.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @see #getDescription()
     * @see Container#description()
     */
    ContainerConfiguration setDescription(@Nullable String description);

    /**
     * Sets the {@link Container#name() name} of the container. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Container#name()
     */
    void setName(@Nullable String name);

    Set<String> tags();

    /**
     * Returns an extension of the specified type. If an extension of the specified type has not already been installed this
     * method will automatically instantiate an extension of the specified type and install it. Returning the instantiated
     * extension for all subsequent calls to this method with the specified type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     */
    <T extends Extension<T>> T use(Class<T> extensionType);

    /**
     * Creates a link to another container.
     * 
     * @param child
     *            the child bundle
     * @param options
     *            optional wiring options
     * @return a bundle link
     */
    BundleLink wire(AnyBundle child, WiringOption... options);
}
// static void ruddn(Consumer<? super ContainerConfiguration> configurator, Consumer<App> consumer, WiringOption...
// options) {
// requireNonNull(consumer, "configurator is null");
// requireNonNull(consumer, "consumer is null");
// DefaultAppConfiguration dac = new DefaultAppConfiguration();
// configurator.accept(dac.root());
// consumer.accept(dac.build());
// }
// ContainerConfiguration immutable().. can read name, installed extensions, and use extensions have already been
// installed??
/// **
// * @param <T>
// * @param extension
// * @return
// * @throws IllegalStateException
// * if the specified extension is already used by another container configuration
// */
/// Tror grunden til jeg fjernede den, er at vi altid laegger op til at
// <T extends Extension> T use(T extension);
