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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.inject.service.ServiceLocator;
import packed.internal.application.ApplicationInitializationContext;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.container.AssemblySetupOfComposer;
import packed.internal.util.LookupUtil;

/**
 * Composers does not usually have any public constructors.
 */
// -> AbstractComposer (synthetic Assembly -> Generaeted per module-per composer type???)
// AbstractComposer og saa ServiceLocator.Composer
public abstract class Composer {

    /** A handle that can access #configuration. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

    /**
     * The configuration of this assembly.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly is not use or has not yet been used.
     * <li>Then, as a part of the build process, it is initialized with the actual configuration object of the component.
     * <li>Finally, {@link #USED} is set to indicate that the assembly has been used
     * </ul>
     * <p>
     * This field is updated via a VarHandle.
     */
    @Nullable
    private ContainerConfiguration configuration;

    /**
     * Checks that the underlying container is still configurable.
     */
    protected final void checkConfigurable() {
        configuration().container.assembly.checkOpen();
    }

    protected final ContainerConfiguration configuration() {
        ContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of a composer");
        } else if (c == ContainerConfiguration.USED) {
            throw new IllegalStateException("This method must be called while the composer is active.");
        }
        return c;
    }

    /**
     * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the composer is not
     * used more than once.
     * 
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private void doBuild(ContainerConfiguration configuration, @SuppressWarnings("rawtypes") ComposerAction consumer) {
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                onNew();

                // call the actual configurations
                consumer.build(this);

                onConfigured();
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, ContainerConfiguration.USED);
            }
        } else if (existing == ContainerConfiguration.USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This composer has already been used, composer = " + getClass());
        } else {
            // Can be this thread or another thread that is already using the assembly.
            throw new IllegalStateException("This composer is currently being used elsewhere, composer = " + getClass());
        }
    }

    /**
     * Sets a {@link Lookup lookup object} that will be used to access members (fields, constructors and methods) on
     * registered objects. The lookup object will be used for all service bindings and component installations that happens
     * after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * Lookup objects that have been explicitly set using {@link Factory#withLookup(java.lang.invoke.MethodHandles.Lookup)}
     * are never overridden by any lookup object set by this method.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup object
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object
     */
    public final void lookup(MethodHandles.Lookup lookup) {
        configuration().container.assembly.lookup(lookup);
    }

    /**
     * Invoked by the runtime immediately after {@link ComposerAction#configure(Composer)}.
     * <p>
     * This method will not be called if {@link ComposerAction#configure(Composer)} throws an exception.
     */
    protected void onConfigured() {} // onComposed or onBuilt, onPreConfigure/ onPostConfigur

    /**
     * Invoked by the runtime immediately before {@link ComposerAction#configure(Composer)}.
     */
    protected void onNew() {} // navngivningen skal alines med AssemblyHook

    /**
     * Create a new application instance by using the specified consumer and configurator.
     * <p>
     * This method is is rarely called directly by end-users. But indirectly through methods such as
     * {@link ServiceLocator#of(Consumer)}.
     * 
     * @param <C>
     *            the type of composer that is exposed to the end-user
     * @param composer
     *            the composer
     * @param consumer
     *            the configurator specified by the end-user for configuring the composer
     * @param wirelets
     *            optional wirelets
     * @return the new application instance
     * 
     * @see Composer
     */
    // A standalone composer...

    // Skal have et andet sted hvor vi laver dem der er en ikke standalone
    // F.eks. ServiceLocator som extension
    // ExtensionConfiguration#compose(new ServiceComposer, configurator <- provided by user - inherit main
    // assemblies.lookup)
    protected static <A, C extends Composer> A compose(ApplicationDriver<A> driver, C composer,
            ComposerAction<? super C> consumer, Wirelet... wirelets) {
        requireNonNull(consumer, "consumer is null");
        requireNonNull(composer, "composer is null");

        // Create a new application realm
        AssemblySetupOfComposer realm = new AssemblySetupOfComposer(((PackedApplicationDriver<A>) driver), consumer, wirelets);

        realm.build(composer, consumer);

        // Return the launched application
        return ApplicationInitializationContext.launch(((PackedApplicationDriver<A>) driver), realm.application, null);
    }
}
