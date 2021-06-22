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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.service.ServiceComposer;
import app.packed.service.ServiceLocator;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.component.PackedComponentDriver;
import packed.internal.util.LookupUtil;

/**
 * Composers does not usually have any public constructors.
 */
// Application Composer.. Nej vi bruger dem ogsaa andet steds fra
// Syntes bare den skal vaere ligesom Assembly
// Hmm, de her special ServiceComposer cases goer at maaske det er find med configuration
public abstract class Composer<C extends ComponentConfiguration> {

    /** A marker object to indicate that the assembly has already been used. */
    private static Object USED = Composer.class;

    /** A handle that can access #configuration. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", Object.class);

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
    private Object configuration;

    /**
     * The component driver of this assembly.
     * <p>
     * This field is read from {@link PackedComponentDriver#getDriver(Assembly)} via a varhandle.
     */
    @SuppressWarnings("unused")
    private final PackedComponentDriver<? extends C> driver;

    /**
     * Create a new composer.
     * 
     * @param configuration
     *            the underlying component configuration
     */
    protected Composer(C configuration) {
        this.configuration = configuration;
        driver = null;
        // Disabled because of test
        // requireNonNull(configuration, "configuration is null");
    }

    /**
     * Creates a new composer using the specified driver.
     * 
     * @param driver
     *            the component driver used to create the configuration objects this composer wraps
     */
    protected Composer(ComponentDriver<? extends C> driver) {
        this.driver = requireNonNull((PackedComponentDriver<? extends C>) driver, "driver is null");
    }

    /**
     * Checks that the underlying component is still configurable.
     * 
     */
    protected final void checkPreBuild() {
        configuration().component().realm.checkOpen();
    }

    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of a composer");
        } else if (c == USED) {
            throw new IllegalStateException("This method must be called while the composer is active.");
        }
        return (C) c;
    }

    /**
     * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the assembly is not
     * used more than once.
     * 
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private void doCompose(C configuration, @SuppressWarnings("rawtypes") ComposerConfigurator consumer) {
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                onNew();

                // call the actual configurations
                consumer.configure(this);

                onConfigured();
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, USED);
            }
        } else if (existing == USED) {
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
        configuration().component().realm.setLookup(lookup);
    }

    /**
     * Invoked by the runtime immediately after {@link ComposerConfigurator#configure(Composer)}.
     * <p>
     * This method will not be called if {@link ComposerConfigurator#configure(Composer)} throws an exception.
     */
    protected void onConfigured() {}

    /**
     * Invoked by the runtime immediately before {@link ComposerConfigurator#configure(Composer)}.
     */
    protected void onNew() {}

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
     * @param configurator
     *            the configurator specified by the end-user for configuring the composer
     * @param wirelets
     *            optional wirelets
     * @return the new application instance
     * 
     * @see Composer
     * @see ServiceComposer
     * @see ServiceLocator#of(Consumer)
     */
    // A standalone composer...

    // Skal have et andet sted hvor vi laver dem der er en ikke standalone
    // F.eks. ServiceLocator som extension
    // ExtensionConfiguration#compose(new ServiceComposer, configurator <- provided by user - inherit main
    // assemblies.lookup)
    protected static <A, C extends Composer<?>> A compose(ApplicationDriver<A> driver, C composer, ComposerConfigurator<? super C> configurator,
            Wirelet... wirelets) {
        return ((PackedApplicationDriver<A>) driver).compose(composer, configurator, wirelets);
    }
}

// Er det ikke noget vi skal definere i vores ArtifactDriver...
//@SafeVarargs
//protected static void $AllowExtensions(Class<? extends Extension>... extensions) {
//    throw new UnsupportedOperationException();
//}
//
//// Eller ogsaa har vi en anden driver??? ComposerDriver...
//
//// Hmm. vi kan jo godt have flere forskellige configurationer...
//// Altsaa fx tillader vi ikke andre extensions hvis vi laver en ServiceLocator i nogen tilfaelde
//// Mens vi nok goere det i andre...
//// Men
//
//// De her bliver kaldt fra en statisks initializer
//// Ikke hvis man skal bruge en ArtifactDriver...
//@SafeVarargs
//protected static void $RejectExtensions(Class<? extends Extension>... extensions) {
//    throw new UnsupportedOperationException();
//}
//Can take a CCC context. And cast it and provide lookup??
//Maaske er det altid en container????
//This class should be inlign with Assembly so Either ComponentComposer or just Composer
//Composer<T>?
//Maaske skal Composer bare vaere en special Assembly...
//Jamen, saa skal vi tage noget andet end Assembly til de forskellige metoder.
//Assembly extended by Composer|
//Class<?> configurator = Assembly|Composer | Extension????<-- naah Assembly|Composer  
