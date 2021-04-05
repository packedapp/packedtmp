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
package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationRuntime;
import app.packed.application.ApplicationWirelets;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import app.packed.inject.ServiceLocator;
import app.packed.state.RunState;
import app.packed.validate.Validation;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver;
import packed.internal.component.WireletArray;
import packed.internal.component.WireletWrapper;
import packed.internal.invoke.Infuser;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** The applications default launch mode, may be overridden via {@link ApplicationWirelets#launchMode(RunState)}. */
    private final RunState launchMode;

    /** The method handle used for creating new application instances. */
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /** The modifiers of this application */
    public final int modifiers;

    /** Wirelet(s) that must be processed before any wirelets supplied by the user. */
    @Nullable
    public final Wirelet wirelet;

    /**
     * Create a new application driver using the specified builder.
     * 
     * @param builder
     *            the used for construction
     */
    private PackedApplicationDriver(Builder builder) {
        this.mhConstructor = requireNonNull(builder.mhConstructor);
        this.modifiers = builder.modifiers;
        this.wirelet = builder.prefix;
        this.launchMode = requireNonNull(builder.launchMode);
    }

    /**
     * Create a new driver by updating an existing driver with a new wirelet.
     * 
     * @param existing
     *            the existing driver
     * @param wirelet
     *            the new wirelet
     */
    private PackedApplicationDriver(PackedApplicationDriver<A> existing, Wirelet wirelet) {
        this.mhConstructor = existing.mhConstructor;
        this.modifiers = existing.modifiers;
        this.launchMode = existing.launchMode;
        this.wirelet = wirelet;
    }

    /** {@inheritDoc} */
    @Override
    public Component analyze(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = buildFromAssembly(assembly, wirelets, PackedComponentModifierSet.I_ANALYSIS);
        return build.container.adaptor();
    }

    /**
     * Returns the raw type of the artifacts that this driver creates.
     * 
     * @return the raw type of the artifacts that this driver creates
     */
    // Virker ikke rigtig pga mhConstructors signature... vi maa gemme den andet steds
    public Class<?> artifactRawType() {
        return mhConstructor.type().returnType();
    }

    /**
     * @param assembly
     *            the root assembly
     * @param wirelets
     *            optional wirelets
     * @param isAnalysis
     *            is it an analysis
     * @param isImage
     *            is it an image
     * @return a build setup
     */
    private BuildSetup buildFromAssembly(Assembly<?> assembly, Wirelet[] wirelets, int modifiers) {

        // Extract the component driver from the assembly
        WireableComponentDriver<?> componentDriver = WireableComponentDriver.getDriver(assembly);

        RealmSetup realm = new RealmSetup(assembly);

        // Create a new build and root application/container/component
        BuildSetup build = new BuildSetup(this, realm, componentDriver, modifiers, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = componentDriver.toConfiguration(build.container);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        realm.close(build.container);
        return build;
    }

    /** {@inheritDoc} */
    @Override
    public <C extends Composer<?>> A compose(C composer, Consumer<? super C> consumer, Wirelet... wirelets) {
        requireNonNull(consumer, "consumer is null");

        // Extract the component driver from the composer
        WireableComponentDriver<?> componentDriver = WireableComponentDriver.getDriver(composer);

        // Create a new realm
        RealmSetup realm = new RealmSetup(consumer);

        // Create a new build and root application/container/component
        BuildSetup build = new BuildSetup(this, realm, componentDriver, 0, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration componentConfiguration = componentDriver.toConfiguration(build.container);

        // Invoke Consumer::doConsumer which in turn will invoke consumer.accept
        try {
            RealmSetup.MH_COMPOSER_DO_COMPOSE.invoke(composer, componentConfiguration, consumer);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        realm.close(build.container);

        // Initialize the application. And start it if necessary (if it is a guest)
        return ApplicationLaunchContext.launch(this, build.application, null);

    }

    /** {@inheritDoc} */
    @Override
    public A launch(Assembly<?> assembly, Wirelet... wirelets) {
        // Build the system
        BuildSetup build = buildFromAssembly(assembly, wirelets, 0);

        // Initialize the system. And start it if necessary (if it is a guest)
        return ApplicationLaunchContext.launch(this, build.application, null);
    }

    /** {@inheritDoc} */
    @Override
    public RunState launchMode() {
        return launchMode;
    }

    /**
     * Create a new application using the specified initialization context.
     * 
     * @param pic
     *            the initialization context to wrap
     * @return the new application instance
     */
    // application interface???
    @SuppressWarnings("unchecked")
    public A newApplication(ApplicationLaunchContext pic) {
        Object result;
        try {
            result = mhConstructor.invoke(pic);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return (A) result;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationImage<A> newImage(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = buildFromAssembly(assembly, wirelets, PackedComponentModifierSet.I_IMAGE);
        return new PackedApplicationImage<>(this, build.application);
    }

    /** {@inheritDoc} */
    @Override
    public Validation validate(Assembly<?> assembly, Wirelet... wirelets) {
        // Denne metoder siger ikke noget om at alle kontrakter er fullfilled.
        // Det er fuldt ud "Lovligt" ikke at specificere alt muligt...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet... wirelets) {
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new PackedApplicationDriver<>(this, w);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        Wirelet w = this.wirelet == null ? wirelet : wirelet.andThen(wirelet);
        return new PackedApplicationDriver<>(this, w);
    }

    /** Single implementation of {@link ApplicationDriver.Builder}. */
    public static final class Builder implements ApplicationDriver.Builder {

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#component()}. */
        private static final MethodHandle MH_NAME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "name",
                String.class);

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#runtime()}. */
        private static final MethodHandle MH_RUNTIME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "runtime",
                ApplicationRuntime.class);

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#services()}. */
        private static final MethodHandle MH_SERVICES = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "services",
                ServiceLocator.class);

        /** The default launch mode of the application. */
        private RunState launchMode;

        MethodHandle mhConstructor;

        /** The modifiers of the application. We have a runtime modifier by default. */
        private int modifiers = PackedComponentModifierSet.I_APPLICATION + PackedComponentModifierSet.I_RUNTIME;

        private Wirelet prefix;

        boolean useShellAsSource;

        /** {@inheritDoc} */
        @Override
        public <S> ApplicationDriver<S> build(Lookup caller, Class<? extends S> implementation, Wirelet... wirelets) {

            // Find a method handle for the application shell's constructor
            Infuser.Builder builder = Infuser.builder(caller, implementation, ApplicationLaunchContext.class);
            //builder.provide(Component.class).invokeExact(MH_COMPONENT, 0);
            builder.provide(ServiceLocator.class).invokeExact(MH_SERVICES, 0);
            builder.provide(String.class).invokeExact(MH_NAME, 0);
            if ((modifiers & PackedComponentModifierSet.I_RUNTIME) != 0) { // Conditional add ApplicationRuntime
                builder.provide(ApplicationRuntime.class).invokeExact(MH_RUNTIME, 0);
            }
            mhConstructor = builder.findConstructor(Object.class, s -> new IllegalArgumentException(s));

            return new PackedApplicationDriver<>(this);
        }

        /** {@inheritDoc} */
        @Override
        public <A> ApplicationDriver<A> build(Lookup caller, Class<A> artifactType, MethodHandle mh, Wirelet... wirelets) {
            // mh = mh.asType(mh.type().changeReturnType(Object.class));
            // TODO fix....
            this.mhConstructor = mh;

            return new PackedApplicationDriver<>(this);
        }

        /** {@inheritDoc} */
        @Override
        public Builder launchMode(RunState launchMode) {
            requireNonNull(launchMode, "launchMode is null");
            if (launchMode == RunState.INITIALIZING) {
                throw new IllegalArgumentException("Cannot specify '" + RunState.INITIALIZING + "'");
            }
            this.launchMode = launchMode;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <A> ApplicationDriver<A> old(MethodHandle mhNewShell, Wirelet... wirelets) {
            mhConstructor = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
            return new PackedApplicationDriver<>(this);
        }

        /** {@inheritDoc} */
        @Override
        public Builder stateless() {
            modifiers &= ~PackedComponentModifierSet.I_RUNTIME;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder useShellAsSource() {
            useShellAsSource = true;
            return this;
        }
    }

    /** Implementation of {@link ApplicationImage} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}. */
    private final /* primitive */ record PackedApplicationImage<A> (PackedApplicationDriver<A> driver, ApplicationSetup application)
            implements ApplicationImage<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletWrapper wrapper = null;
            if (wirelets.length > 0) {
                Wirelet[] ws = WireletArray.flatten(wirelets);
                wrapper = new WireletWrapper(ws);
            }

            return ApplicationLaunchContext.launch(driver, application, wrapper);
        }

        /** {@inheritDoc} */
        @Override
        public RunState launchMode() {
            return application.launchMode;
        }
    }
}
// Uhh uhhh species... Job<R> kan vi lave det???

// Create an infuser (SomeExtension, Class)