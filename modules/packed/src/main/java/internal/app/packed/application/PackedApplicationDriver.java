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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.lifetime.managed.ManagedLifetimeController;
import app.packed.lifetime.sandbox.OldLifetimeKind;
import app.packed.operation.Op;
import app.packed.service.ServiceLocator;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.deprecated.invoke.InternalInfuser;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** An application driver for application drivers. */
    public static PackedApplicationDriver<Void> PRIMORDIAL = new PackedApplicationDriver<>();

    final Set<Class<? extends Extension<?>>> bannedExtensions;

    private final OldLifetimeKind lifetimeKind;

    /** The method handle used for creating new application instances. */
    // We need more info for bootstrap mirrors
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /** Supplies a mirror for the application. */
    public final Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

    /** Optional (flattened) wirelets that will be applied to any applications created by this driver. */
    @Nullable
    public final Wirelet wirelet;

    private PackedApplicationDriver() {
        this.bannedExtensions = Set.of();
        this.lifetimeKind = OldLifetimeKind.UNMANAGED; // The primordial application does not need to be closed
        // We need to create the exception as well
        this.mhConstructor = MethodHandles.throwException(void.class, Error.class);
        this.wirelet = null;
    }

    /**
     * Create a new application driver using the specified builder.
     * 
     * @param builder
     *            the used for construction
     */
    private PackedApplicationDriver(Builder<?> builder) {
        this.wirelet = builder.wirelet;
        this.mhConstructor = requireNonNull(builder.mhConstructor);
        this.lifetimeKind = builder.lifetimeKind;
        this.bannedExtensions = Set.copyOf(builder.disabledExtensions);
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
        this.lifetimeKind = existing.lifetimeKind;
        this.mhConstructor = existing.mhConstructor;
        this.bannedExtensions = existing.bannedExtensions;
    }

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        return bannedExtensions;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationLauncher<A> newLauncher(Assembly assembly, Wirelet... wirelets) {
        AssemblySetup as = new AssemblySetup(this, BuildGoal.NEW_LAUNCHER, assembly, wirelets);
        as.build();
        return new SingleShotApplicationImage<>(this, as.application);
    }

    /** {@inheritDoc} */
    @Override
    public A launch(Assembly assembly, Wirelet... wirelets) {
        AssemblySetup as = new AssemblySetup(this, BuildGoal.LAUNCH, assembly, wirelets);
        as.build();
        return as.application.launcher.launchImmediately(this);
    }

    /**
     * Returns whether or not applications produced by this driver have an {@link ManagedLifetimeController}.
     * <p>
     * Applications that are not runnable will always be launched in the Initial state.
     * 
     * @return whether or not the applications produced by this driver are runnable
     */
    public OldLifetimeKind lifetimeKind() {
        return lifetimeKind;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror newMirror(Assembly assembly, Wirelet... wirelets) {
        AssemblySetup as = new AssemblySetup(this, BuildGoal.NEW_MIRROR, assembly, wirelets);
        as.build();
        return as.application.mirror();
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
    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        AssemblySetup as = new AssemblySetup(this, BuildGoal.NEW_IMAGE, assembly, wirelets);
        as.build();
        return new ReusableApplicationImage<>(this, as.application);
    }

    /** {@inheritDoc} */
    @Override
    public void verify(Assembly assembly, Wirelet... wirelets) {
        AssemblySetup as = new AssemblySetup(this, BuildGoal.VERIFY, assembly, wirelets);
        as.build();
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

    /** Single implementation of {@link ApplicationDriver.Builder}. */
    public static final class Builder<A> implements ApplicationDriver.Builder<A> {

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

        @Nullable
        PackedOp<A> factory;

        /**
         * All application drivers except {@link PackedApplicationDriver#PRIMORDIAL} has either an unmanaged or managed
         * lifetime.
         */
        private OldLifetimeKind lifetimeKind = OldLifetimeKind.UNMANAGED;

        MethodHandle mhConstructor;

        private Wirelet wirelet;

        public Builder(Op<A> factory) {
            this.factory = factory == null ? null : PackedOp.crack(factory);

            // Problemet med at komme laengere er lidt InternalInfuser som er bygget op omkring den faar en klasse
            // og ikke et internal factory

            // Maybe we will make an actual application????
            // Paa den maade kan vi ogsaa lettere expose mirroret

        }

        /** {@inheritDoc} */
        @Override
        public <S> ApplicationDriver<S> build(Class<S> artifactType, MethodHandle mh, Wirelet... wirelets) {
            // mh = mh.asType(mh.type().changeReturnType(Object.class));
            // TODO fix....
            this.mhConstructor = mh;

            return new PackedApplicationDriver<>(this);
        }

        /** {@inheritDoc} */
        @Override
        public <S> ApplicationDriver<S> build(Lookup caller, Class<? extends S> implementation, Wirelet... wirelets) {
            // Find a method handle for the application shell's constructor
            InternalInfuser.Builder builder = InternalInfuser.builder(caller, implementation, ApplicationInitializationContext.class);
            // builder.provide(Component.class).invokeExact(MH_COMPONENT, 0);
            builder.provide(ServiceLocator.class).invokeExact(MH_SERVICE_LOCATOR, 0);
            builder.provide(String.class).invokeExact(MH_NAME, 0);
            if (lifetimeKind == OldLifetimeKind.MANAGED) { // Conditional add ApplicationRuntime
                builder.provide(ManagedLifetimeController.class).invokeExact(MH_RUNTIME, 0);
            }

            // builder(caller).addParameter(implementation).addParameter(AIC);
            // builder.provideService(ServiceLocator.class, builder.addComputed(MH_SERVICES, 0));

            mhConstructor = builder.findConstructor(Object.class, s -> new IllegalArgumentException(s));

            return new PackedApplicationDriver<>(this);
        }

        private <S> PackedApplicationDriver<S> buildOld(MethodHandle mhNewShell, Wirelet... wirelets) {
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
        public Builder<A> disableExtension(Class<? extends Extension<?>> extensionType) {
            ClassUtil.checkProperSubclass(Extension.class, extensionType, "extensionType");
            disabledExtensions.add(extensionType);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder<A> managedLifetime() {
            this.lifetimeKind = OldLifetimeKind.MANAGED;
            return this;
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}.
     */
    public static final class SingleShotApplicationImage<A> extends AtomicReference<ReusableApplicationImage<A>> implements ApplicationLauncher<A> {

        private static final long serialVersionUID = 1L;

        SingleShotApplicationImage(PackedApplicationDriver<A> driver, ApplicationSetup application) {
            super(new ReusableApplicationImage<>(driver, application));
        }
        
        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            ReusableApplicationImage<A> img = getAndSet(null);
            if (img == null) {
                throw new IllegalStateException("This image has already been used");
            }
            return img.launch(wirelets);
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}.
     */
    public /* primitive */ record ReusableApplicationImage<A> (PackedApplicationDriver<A> driver, ApplicationSetup application) implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            return application.launcher.launchFromImage(driver, wirelets);
        }
    }

    /** A mapped wrapper of an {@link ApplicationLauncher}. */
    public record MappedApplicationImage<A, F> (ApplicationLauncher<F> image, Function<? super F, ? extends A> mapper) implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            F result = image.launch(wirelets);
            return mapper.apply(result);
        }

        /** {@inheritDoc} */
        @Override
        public <E> ApplicationLauncher<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            Function<? super F, ? extends E> andThen = this.mapper.andThen(mapper);
            return new MappedApplicationImage<>(image, andThen);
        }
    }
}
