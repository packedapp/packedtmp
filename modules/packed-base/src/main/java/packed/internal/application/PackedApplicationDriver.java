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
import java.lang.invoke.VarHandle;
import java.util.HashSet;
import java.util.Set;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationRuntime;
import app.packed.application.ApplicationRuntimeExtension;
import app.packed.application.ApplicationRuntimeWirelets;
import app.packed.application.BuildTarget;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Composer;
import app.packed.component.ComposerConfigurator;
import app.packed.component.Wirelet;
import app.packed.container.BaseContainerConfiguration;
import app.packed.extension.Extension;
import app.packed.service.ServiceLocator;
import app.packed.state.sandbox.InstanceState;
import packed.internal.component.CompositeWirelet;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireletWrapper;
import packed.internal.container.PackedContainerDriver;
import packed.internal.invoke.Infuser;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> extends PackedContainerDriver<BaseContainerConfiguration> implements ApplicationDriver<A> {

    /** The driver used for creating mirrors daemon driver. */
    // Hcad skal LaunchMode fx returnere... Det giver jo mening at checke hvis man fx gerne
    // vil sikre sig af en application goere x...
    public static final PackedApplicationDriver<?> MIRROR_DRIVER = new Builder()
            .buildOld(MethodHandles.empty(MethodType.methodType(Void.class, ApplicationLaunchContext.class)));

    /** A handle that can access the component driver stored in Composer#driver. */
    private static final VarHandle VH_COMPOSER_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Composer.class, "driver",
            PackedComponentDriver.class);

    final Set<Class<? extends Extension>> disabledExtensions;

    /** The default launch mode, may be overridden via {@link ApplicationRuntimeWirelets#launchMode(InstanceState)}. */
    private final InstanceState launchMode;

    /** The method handle used for creating new application instances. */
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /**
     * Create a new application driver using the specified builder.
     * 
     * @param builder
     *            the used for construction
     */
    private PackedApplicationDriver(Builder builder) {
        super(builder.wirelet);
        this.mhConstructor = requireNonNull(builder.mhConstructor);
        this.launchMode = builder.launchMode == null ? InstanceState.INITIALIZED : builder.launchMode;
        this.disabledExtensions = Set.copyOf(builder.disabledExtensions);

        // Cannot disable ApplicationRuntimeExtension and then at the same time set a launch mode
        if (disabledExtensions.contains(ApplicationRuntimeExtension.class) && builder.launchMode != null) {
            throw new IllegalStateException("This method cannot be called when a launch mode has been set");
        }
    }

    /**
     * Update an application driver with a new wirelet.
     * 
     * @param existing
     *            the existing driver
     * @param wirelet
     *            the new wirelet
     */
    private PackedApplicationDriver(PackedApplicationDriver<A> existing, Wirelet wirelet) {
        super(wirelet);
        this.mhConstructor = existing.mhConstructor;
        this.launchMode = existing.launchMode;
        this.disabledExtensions = existing.disabledExtensions;
    }

    /**
     * Returns the raw type of the artifacts that this driver creates.
     * 
     * @return the raw type of the artifacts that this driver creates
     */
    // Virker ikke rigtig pga mhConstructors signature... vi maa gemme den andet steds
    public Class<?> applicationRawType() {
        return mhConstructor.type().returnType();
    }

    /**
     * Builds an application from the specified assembly and optional wirelets.
     * <p>
     * This
     * 
     * @param assembly
     *            the root assembly
     * @param wirelets
     *            optional wirelets
     * @param buildTarget
     *            the build target
     * @return a build setup
     */
    public BuildSetup build(BuildTarget buildTarget, Assembly<?> assembly, Wirelet[] wirelets) {
        // TODO we need to check that the assembly is not in the process of being built..
        // Both here and linking... We could call it from within build

        // Extract the component driver from the field Assembly#driver
        PackedComponentDriver<?> componentDriver = PackedComponentDriver.getDriver(assembly);

        // Create the initial realm realm, typically we will have a realm per container
        RealmSetup realm = new RealmSetup(this, buildTarget, assembly, wirelets);

        // Create a new component configuration instance which are passed along to assembly that
        // then exposes the various methods on the configuration objects through
        // protected final methods such as ContainerAssembly#use(Class)
        ComponentConfiguration configuration = componentDriver.toConfiguration(realm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        // This will recursively call down through any sub-assemblies that are linked
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm if the application has been built successfully
        realm.close();

        return realm.build;
    }

    public <C extends Composer<?>> A compose(C composer, ComposerConfigurator<? super C> consumer, Wirelet... wirelets) {
        requireNonNull(consumer, "consumer is null");
        requireNonNull(composer, "composer is null");

        // Extract the component driver from the composer
        PackedComponentDriver<?> componentDriver = (PackedComponentDriver<?>) VH_COMPOSER_DRIVER.get(composer);

        // Create a new application realm
        RealmSetup realm = new RealmSetup(this, consumer, wirelets);

        // Create the component configuration that is needed by the composer
        ComponentConfiguration componentConfiguration = componentDriver.toConfiguration(realm.root);

        // Invoke Composer#doCompose which in turn will invoke consumer.accept
        try {
            RealmSetup.MH_COMPOSER_DO_COMPOSE.invoke(composer, componentConfiguration, consumer);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm if the application has been built successfully
        realm.close();

        // Return the launched application
        return ApplicationLaunchContext.launch(this, realm.build.application, null);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> disabledExtensions() {
        return disabledExtensions;
    }

    @SuppressWarnings("unchecked")
    public PackedApplicationDriver<A> withDisabledExtensions(Class<? extends Extension>... extensionTypes) {
        // Ideen var lidt ikke at lave disse public... Men tvinge folk til bare at bruge extensions...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public A launch(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = build(BuildTarget.INSTANCE, assembly, wirelets);
        return ApplicationLaunchContext.launch(this, build.application, null);
    }

    // Kan kun saette launch mode paa application runtime
    /** {@inheritDoc} */
    @Override
    public InstanceState launchMode() {
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
        BuildSetup build = build(BuildTarget.IMAGE, assembly, wirelets);
        return new PackedApplicationImage<>(this, build.application);
    }

    @Override
    public Class<?> type() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet... wirelets) {
        // Skal vi checke noget med components
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new PackedApplicationDriver<>(this, w);
    }
//
//    /** {@inheritDoc} */
//    @Override
//    public ApplicationDriver<A> with(Wirelet wirelet) {
//        requireNonNull(wirelet, "wirelet is null");
//        Wirelet w = this.wirelet == null ? wirelet : wirelet.andThen(wirelet);
//        return new PackedApplicationDriver<>(this, w);
//    }

    /** Single implementation of {@link ApplicationDriver.Builder}. */
    public static final class Builder implements ApplicationDriver.Builder {

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#name()}. */
        private static final MethodHandle MH_NAME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "name",
                String.class);

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#runtime()}. */
        private static final MethodHandle MH_RUNTIME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "runtime",
                ApplicationRuntime.class);

        /** A MethodHandle for invoking {@link ApplicationLaunchContext#services()}. */
        private static final MethodHandle MH_SERVICES = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationLaunchContext.class, "services",
                ServiceLocator.class);

        private final HashSet<Class<? extends Extension>> disabledExtensions = new HashSet<>();

        /** The default launch mode of the application. */
        private InstanceState launchMode;

        MethodHandle mhConstructor;

        private Wirelet wirelet;

        /** {@inheritDoc} */
        @Override
        public <S> ApplicationDriver<S> build(Lookup caller, Class<? extends S> implementation, Wirelet... wirelets) {

            // Find a method handle for the application shell's constructor
            Infuser.Builder builder = Infuser.builder(caller, implementation, ApplicationLaunchContext.class);
            // builder.provide(Component.class).invokeExact(MH_COMPONENT, 0);
            builder.provide(ServiceLocator.class).invokeExact(MH_SERVICES, 0);
            builder.provide(String.class).invokeExact(MH_NAME, 0);
            if (!disabledExtensions.contains(ApplicationRuntimeExtension.class)) { // Conditional add ApplicationRuntime
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
        public <A> PackedApplicationDriver<A> buildOld(MethodHandle mhNewShell, Wirelet... wirelets) {
            mhConstructor = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
            return new PackedApplicationDriver<>(this);
        }

//        /** {@inheritDoc} */
//        @Override
//        public Builder disable(@SuppressWarnings("unchecked") Class<? extends Extension>... extensionTypes) {
//            requireNonNull(extensionTypes, "extensionTypes is null");
//            for (Class<? extends Extension> c : extensionTypes) {
//                disabledExtensions.add(ClassUtil.checkProperSubclass(Extension.class, c));
//            }
//            return this;
//        }

        /** {@inheritDoc} */
        @Override
        public Builder disableExtension(Class<? extends Extension> extensionType) {
            requireNonNull(extensionType, "extensionType is null");
            disabledExtensions.add(ClassUtil.checkProperSubclass(Extension.class, extensionType));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder launchMode(InstanceState launchMode) {
            requireNonNull(launchMode, "launchMode is null");
            if (launchMode == InstanceState.INITIALIZING) {
                throw new IllegalArgumentException("'" + InstanceState.INITIALIZING + "' is not a valid launch mode");
            }
            this.launchMode = launchMode;
            return this;
        }
    }

    /** Implementation of {@link ApplicationImage} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}. */
    public final /* primitive */ record PackedApplicationImage<A> (PackedApplicationDriver<A> driver, ApplicationSetup application)
            implements ApplicationImage<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletWrapper wrapper = null;
            if (wirelets.length > 0) {
                wrapper = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
            }

            return ApplicationLaunchContext.launch(driver, application, wrapper);
        }

        /** {@inheritDoc} */
        @Override
        public InstanceState launchMode() {
            return application.launchMode;
        }
    }
}
// Uhh uhhh species... Job<R> kan vi lave det???

// Create an infuser (SomeExtension, Class)