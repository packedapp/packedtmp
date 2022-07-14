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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import app.packed.application.ApplicationBuildInfo.ApplicationBuildType;
import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLaunchException;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationWirelets;
import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceLocator;
import app.packed.lifetime.RunState;
import app.packed.lifetime.managed.ManagedLifetimeController;
import internal.app.packed.container.AssemblyUserRealmSetup;
import internal.app.packed.container.CompositeWirelet;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.inject.invoke.InternalInfuser;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** The driver used for creating mirrors daemon driver. */
    // Hcad skal LaunchMode fx returnere... Det giver jo mening at checke hvis man fx gerne
    // vil sikre sig af en application goere x...
    public static final PackedApplicationDriver<Void> MIRROR_DRIVER = new Builder().buildVoid();

    final Set<Class<? extends Extension<?>>> disabledExtensions;

    private final boolean isExecutable;

    /** The default launch mode, may be overridden via {@link ApplicationWirelets#launchMode(RunState)}. */
    private final RunState launchMode;

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
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        return disabledExtensions;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExecutable() {
        return isExecutable;
    }

    /** {@inheritDoc} */
    @Override
    public A launch(Assembly assembly, Wirelet... wirelets) {
        // Create a new assembly realm which
        AssemblyUserRealmSetup realm = new AssemblyUserRealmSetup(this, ApplicationBuildType.LAUNCH, assembly, wirelets);
        realm.build();
        return ApplicationInitializationContext.launch(this, realm.application, null);
    }

    /** {@inheritDoc} */
    @Override
    public RunState launchMode() {
        return launchMode;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        AssemblyUserRealmSetup realm = new AssemblyUserRealmSetup(this, ApplicationBuildType.MIRROR, assembly, wirelets);
        realm.build();
        return realm.application.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        AssemblyUserRealmSetup realm = new AssemblyUserRealmSetup(this, ApplicationBuildType.BUILD, assembly, wirelets);
        realm.build();
        return new PackedApplicationLauncher<>(this, realm.application);
    }

    /**
     * Create a new application instance using the specified launch context.
     * 
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    @SuppressWarnings("unchecked")
    A newInstance(ApplicationInitializationContext context) {
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
    public ApplicationLauncher<A> newReusableImage(Assembly assembly, Wirelet... wirelets) {
        AssemblyUserRealmSetup realm = new AssemblyUserRealmSetup(this, ApplicationBuildType.BUILD_MULTIPLE, assembly, wirelets);
        realm.build();
        return new PackedApplicationLauncher<>(this, realm.application);
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
    public PackedApplicationDriver<A> withDisabledExtensions(Class<? extends Extension<?>>... extensionTypes) {
        // Ideen var lidt ikke at lave disse public... Men tvinge folk til bare at bruge extensions...
        throw new UnsupportedOperationException();
    }

//    @Override
//    public ApplicationDriver<A> withLaunchMode(RunState launchMode) {
//        if (!isExecutable()) {
//            throw new UnsupportedOperationException("This method is only supported if the application is executable");
//        }
//        // Skal vel bare flyttes til implementeringen???
//        throw new UnsupportedOperationException();
////            return with(ExecutionWirelets.launchMode(launchMode));
//
//    }

    /** Single implementation of {@link ApplicationDriver.Builder}. */
    public static final class Builder implements ApplicationDriver.Builder {

        /** A MethodHandle for invoking {@link ApplicationInitializationContext#name()}. */
        private static final MethodHandle MH_NAME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationInitializationContext.class, "name",
                String.class);

        /** A MethodHandle for invoking {@link ApplicationInitializationContext#runtime()}. */
        private static final MethodHandle MH_RUNTIME = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationInitializationContext.class,
                "runtime", ManagedLifetimeController.class);

        /** A MethodHandle for invoking {@link ApplicationInitializationContext#serviceLocator()}. */
        private static final MethodHandle MH_SERVICE_LOCATOR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationInitializationContext.class,
                "serviceLocator", ServiceLocator.class);
        
        private final HashSet<Class<? extends Extension<?>>> disabledExtensions = new HashSet<>();

        /** Whether or not the applications that will be produced are executable. */
        private boolean isExecutable = true;

        /** The default launch mode of the application. */
        private RunState launchMode = RunState.TERMINATED;

        MethodHandle mhConstructor;

        private Wirelet wirelet;

        /** {@inheritDoc} */
        @Override
        public <S> ApplicationDriver<S> build(Lookup caller, Class<? extends S> implementation, Wirelet... wirelets) {

            // Find a method handle for the application shell's constructor
            InternalInfuser.Builder builder = InternalInfuser.builder(caller, implementation, ApplicationInitializationContext.class);
            // builder.provide(Component.class).invokeExact(MH_COMPONENT, 0);
            builder.provide(ServiceLocator.class).invokeExact(MH_SERVICE_LOCATOR, 0);
            builder.provide(String.class).invokeExact(MH_NAME, 0);
            if (isExecutable) { // Conditional add ApplicationRuntime
                builder.provide(ManagedLifetimeController.class).invokeExact(MH_RUNTIME, 0);
            }

            // builder(caller).addParameter(implementation).addParameter(AIC);
            // builder.provideService(ServiceLocator.class, builder.addComputed(MH_SERVICES, 0));

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

        private <A> PackedApplicationDriver<A> buildOld(MethodHandle mhNewShell, Wirelet... wirelets) {
            mhConstructor = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationInitializationContext.class));
            return new PackedApplicationDriver<>(this);
        }

//      /** {@inheritDoc} */
//      @Override
//      public Builder disable(@SuppressWarnings("unchecked") Class<? extends Extension<?>>... extensionTypes) {
//          requireNonNull(extensionTypes, "extensionTypes is null");
//          for (Class<? extends Extension<?>> c : extensionTypes) {
//              disabledExtensions.add(ClassUtil.checkProperSubclass(Extension.class, c));
//          }
//          return this;
//      }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationDriver<Void> buildVoid(Wirelet... wirelets) {
            return buildOld(MethodHandles.empty(MethodType.methodType(Void.class, ApplicationInitializationContext.class)));
        }

        /** {@inheritDoc} */
        @Override
        public Builder disableExtension(Class<? extends Extension<?>> extensionType) {
            ClassUtil.checkProperSubclass(Extension.class, extensionType, "extensionType");
            disabledExtensions.add(extensionType);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder executable(RunState launchMode) {
            requireNonNull(launchMode, "launchMode is null");
            this.isExecutable = true;
            this.launchMode = launchMode;
            return this;
        }
    }

    /** Implementation of {@link ApplicationLauncher} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}. */
    public final /* primitive */ record PackedApplicationLauncher<A> (PackedApplicationDriver<A> driver, ApplicationSetup application)
            implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletWrapper wrapper = null;
            if (wirelets.length > 0) {
                wrapper = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
            }

            return ApplicationInitializationContext.launch(driver, application, wrapper);
        }

        /** {@inheritDoc} */
        @Override
        public A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public <E> ApplicationLauncher<E> map(Function<A, E> mapper) {
            throw new UnsupportedOperationException();
        }
    }
}
///** {@return the type (typically an interface) of the application instances created by this driver.} */
//public Class<?> launchType() {
//  // It is a bit strange this method I think. Now since the AD is not exposed to end users
//  throw new UnsupportedOperationException();
//}


// Uhh uhhh species... Job<R> kan vi lave det???

// Create an infuser (SomeExtension, Class)