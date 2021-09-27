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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import app.packed.application.ApplicationDescriptor.ApplicationDescriptorOutput;
import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationLaunchMode;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationRuntime;
import app.packed.application.ExecutionWirelets;
import app.packed.base.Nullable;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.BundleDriver;
import app.packed.bundle.Composer;
import app.packed.bundle.ComposerAction;
import app.packed.bundle.Wirelet;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import app.packed.service.ServiceLocator;
import app.packed.state.sandbox.InstanceState;
import packed.internal.bundle.CompositeWirelet;
import packed.internal.bundle.PackedBundleDriver;
import packed.internal.bundle.WireletWrapper;
import packed.internal.component.RealmSetup;
import packed.internal.invoke.Infuser;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** The driver used for creating mirrors daemon driver. */
    // Hcad skal LaunchMode fx returnere... Det giver jo mening at checke hvis man fx gerne
    // vil sikre sig af en application goere x...
    public static final PackedApplicationDriver<?> MIRROR_DRIVER = new Builder()
            .buildOld(MethodHandles.empty(MethodType.methodType(Void.class, ApplicationLaunchContext.class)));

    final Set<Class<? extends Extension>> disabledExtensions;

    private final boolean isExecutable;

    /** The default launch mode, may be overridden via {@link ExecutionWirelets#launchMode(InstanceState)}. */
    private final ApplicationLaunchMode launchMode;

    /** The method handle used for creating new application instances. */
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /** Optional wirelets that will be applied to any component created by this driver. */
    @Nullable
    public final Wirelet wirelet;

    /**
     * Create a new application driver using the specified builder.
     * 
     * @param builder
     *            the used for construction
     */
    private PackedApplicationDriver(Builder builder) {
        this.wirelet = builder.wirelet;
        this.mhConstructor = requireNonNull(builder.mhConstructor);
        this.launchMode = builder.launchMode;
        this.disabledExtensions = Set.copyOf(builder.disabledExtensions);

        this.isExecutable = builder.isExecutable;
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
        this.wirelet = existing.wirelet;
        this.isExecutable = existing.isExecutable;
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

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        return disabledExtensions;
    }

    /**
     * Builds an application using the specified assembly and optional wirelets.
     * 
     * @param buildTarget
     *            the build target
     * @param assembly
     *            the assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return a build setup
     */
    private BuildSetup build(ApplicationDescriptorOutput buildTarget, Bundle<?> assembly, Wirelet[] wirelets) {
        // TODO we need to check that the assembly is not in the process of being built..
        // Both here and linking... We could call it from within build

        // Extract the driver from the field Bundle#driver
        PackedBundleDriver<?> componentDriver = PackedBundleDriver.getDriver(assembly);

        // Create the initial realm, typically we will have a realm per bundle
        RealmSetup realm = new RealmSetup(this, buildTarget, assembly, wirelets);

        // Create a new component configuration instance which are passed along to assembly that
        // then exposes the various methods on the configuration objects through
        // protected final methods such as ContainerAssembly#use(Class)
        ComponentConfiguration configuration = componentDriver.toConfiguration(realm.root);

        // Invoke Bundle::doBuild which in turn will invoke Bundle::build
        // This will recursively call down through any sub-bundles that are linked
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm if the application has been built successfully
        realm.close();

        return realm.build;
    }

    public <C extends Composer> A compose(BundleDriver<BundleConfiguration> containerDriver, Function<BundleConfiguration, C> composer,
            ComposerAction<? super C> consumer, Wirelet... wirelets) {
        requireNonNull(consumer, "consumer is null");
        requireNonNull(composer, "composer is null");

        // Extract the component driver from the composer
        PackedBundleDriver<?> componentDriver = (PackedBundleDriver<?>) containerDriver;

        // Create a new application realm
        RealmSetup realm = new RealmSetup(this, consumer, wirelets);

        // Create the component configuration that is needed by the composer
        BundleConfiguration componentConfiguration = componentDriver.toConfiguration(realm.root);

        Composer comp = composer.apply(componentConfiguration);
        // Invoke Composer#doCompose which in turn will invoke consumer.accept
        try {
            RealmSetup.MH_COMPOSER_DO_COMPOSE.invoke(comp, componentConfiguration, consumer);
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
    public ApplicationImage<A> imageOf(Bundle<?> assembly, Wirelet... wirelets) {
        BuildSetup build = build(ApplicationDescriptorOutput.IMAGE, assembly, wirelets);
        return new PackedApplicationImage<>(this, build.application);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExecutable() {
        return isExecutable;
    }

    /** {@inheritDoc} */
    @Override
    public A launch(Bundle<?> assembly, Wirelet... wirelets) {
        BuildSetup build = build(ApplicationDescriptorOutput.INSTANCE, assembly, wirelets);
        return ApplicationLaunchContext.launch(this, build.application, null);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationLaunchMode launchMode() {
        return launchMode;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror mirrorOf(Bundle<?> assembly, Wirelet... wirelets) {
        return build(ApplicationDescriptorOutput.MIRROR, assembly, wirelets).application.mirror();
    }

    /**
     * Create a new application instance using the specified launch context.
     * 
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    @SuppressWarnings("unchecked")
    A newInstance(ApplicationLaunchContext context) {
        Object result;
        try {
            result = mhConstructor.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return (A) result;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationImage<A> reusableImageOf(Bundle<?> assembly, Wirelet... wirelets) {
        BuildSetup build = build(ApplicationDescriptorOutput.REUSABLE_IMAGE, assembly, wirelets);
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

    @SuppressWarnings("unchecked")
    public PackedApplicationDriver<A> withDisabledExtensions(Class<? extends Extension>... extensionTypes) {
        // Ideen var lidt ikke at lave disse public... Men tvinge folk til bare at bruge extensions...
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationDriver<A> withLaunchMode(ApplicationLaunchMode launchMode) {
        if (!isExecutable()) {
            throw new UnsupportedOperationException("This method is only supported if the application is executable");
        }
        // Skal vel bare flyttes til implementeringen???
        throw new UnsupportedOperationException();
//            return with(ExecutionWirelets.launchMode(launchMode));

    }

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

        /** Whether or not the applications that will be produced are executable. */
        private boolean isExecutable;

        /** The default launch mode of the application. */
        private ApplicationLaunchMode launchMode = ApplicationLaunchMode.INITIALIZED;

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
            if (isExecutable) { // Conditional add ApplicationRuntime
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

//      /** {@inheritDoc} */
//      @Override
//      public Builder disable(@SuppressWarnings("unchecked") Class<? extends Extension>... extensionTypes) {
//          requireNonNull(extensionTypes, "extensionTypes is null");
//          for (Class<? extends Extension> c : extensionTypes) {
//              disabledExtensions.add(ClassUtil.checkProperSubclass(Extension.class, c));
//          }
//          return this;
//      }

        /** {@inheritDoc} */
        @Override
        public Builder disableExtension(Class<? extends Extension> extensionType) {
            requireNonNull(extensionType, "extensionType is null");
            disabledExtensions.add(ClassUtil.checkProperSubclass(Extension.class, extensionType));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder executable(ApplicationLaunchMode launchMode) {
            requireNonNull(launchMode, "launchMode is null");
            this.isExecutable = true;
            this.launchMode = launchMode;
            return this;
        }
    }

    /** Implementation of {@link ApplicationImage} used by {@link ApplicationDriver#imageOf(Bundle, Wirelet...)}. */
    public final /* primitive */ record PackedApplicationImage<A> (PackedApplicationDriver<A> driver, ApplicationSetup application)
            implements ApplicationImage<A> {

        /** {@inheritDoc} */
        @Override
        public A use(Wirelet... wirelets) {
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
        public ApplicationLaunchMode launchMode() {
            return application.launchMode;
        }
    }
}
// Uhh uhhh species... Job<R> kan vi lave det???

// Create an infuser (SomeExtension, Class)