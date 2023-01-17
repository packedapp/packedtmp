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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.application.BuildGoal;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.lifetime.ApplicationInitializationContext;
import internal.app.packed.lifetime.sandbox.OldLifetimeKind;
import internal.app.packed.util.ThrowableUtil;

/** Implementation of {@link OldBootstrapApp}. */
public final class PackedBootstrapApp<A> extends ApplicationDriver<A> {

    final Set<Class<? extends Extension<?>>> extensionDenyList;

    private final OldLifetimeKind lifetimeKind;

    /** The method handle used for creating new application instances. */
    // We need more info for bootstrap mirrors
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /** Supplies a mirror for the application. */
    public final Supplier<? extends ApplicationMirror> mirrorSupplier = ApplicationMirror::new;

    /** Optional (flattened) wirelets that will be applied to any applications created by this driver. */
    @Nullable
    public final Wirelet wirelet;

    public PackedBootstrapApp(OldLifetimeKind lifetimeKind, MethodHandle mh, Wirelet wirelet) {
        this.wirelet = wirelet;
        this.mhConstructor = requireNonNull(mh);
        this.lifetimeKind = requireNonNull(lifetimeKind);
        this.extensionDenyList = Set.of();
    }

    /**
     * Update an application driver with a new wirelet.
     * 
     * @param existing
     *            the existing driver
     * @param wirelet
     *            the new wirelet
     */
    private PackedBootstrapApp(PackedBootstrapApp<A> existing, Wirelet wirelet) {
        this.wirelet = existing.wirelet;
        this.lifetimeKind = existing.lifetimeKind;
        this.mhConstructor = existing.mhConstructor;
        this.extensionDenyList = existing.extensionDenyList;
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
        return extensionDenyList;
    }

    /** {@inheritDoc} */
    public A launch(Assembly assembly, Wirelet... wirelets) {
        // Build the application
        AssemblySetup as = new AssemblySetup(this, BuildGoal.LAUNCH, null, assembly, wirelets);
        as.build();

        // Launch the application
        RuntimeApplicationLauncher launcher = as.application.codeGenerator.launcher;
        // as= null? For GC?
        return launcher.launchImmediately(this);
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
    public Supplier<? extends ApplicationMirror> mirrorSupplier() {
        return mirrorSupplier;
    }

    /** {@inheritDoc} */
    public ApplicationLauncher<A> newImage(Assembly assembly, Wirelet... wirelets) {
        // Build the application
        AssemblySetup as = new AssemblySetup(this, BuildGoal.NEW_IMAGE, null, assembly, wirelets);
        as.build();

        // Create a reusable launcher
        return new ReusableApplicationImage<>(this, as.application);
    }

    /**
     * Create a new application instance using the specified launch context.
     * 
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    @SuppressWarnings("unchecked")
    public A newInstance(ApplicationInitializationContext context) {
        Object result;
        try {
            result = mhConstructor.invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return (A) result;
    }

    /** {@inheritDoc} */
    public ApplicationLauncher<A> newLauncher(Assembly assembly, Wirelet... wirelets) {
        // Build the application
        AssemblySetup as = new AssemblySetup(this, BuildGoal.NEW_LAUNCHER, null, assembly, wirelets);
        as.build();

        // Create single shop image
        return new SingleShotApplicationImage<>(this, as.application);
    }

    /** {@inheritDoc} */
    public ApplicationMirror newMirror(Assembly assembly, Wirelet... wirelets) {
        // Build the application
        AssemblySetup as = new AssemblySetup(this, BuildGoal.MIRROR, null, assembly, wirelets);
        as.build();

        // Create a mirror for the application
        return as.application.mirror();
    }

    /** {@inheritDoc} */
    public void verify(Assembly assembly, Wirelet... wirelets) {
        // Build (and verify) the application
        AssemblySetup as = new AssemblySetup(this, BuildGoal.VERIFY, null, assembly, wirelets);
        as.build();
    }

//    @SuppressWarnings("unchecked")
//    public PackedApplicationDriver<A> withDisabledExtensions(Class<? extends Extension<?>>... extensionTypes) {
//        // Ideen var lidt ikke at lave disse public... Men tvinge folk til bare at bruge extensions...
//        throw new UnsupportedOperationException();
//    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wirelet() {
        return wirelet;
    }

    /** {@inheritDoc} */
    public PackedBootstrapApp<A> with(Wirelet... wirelets) {
        // Skal vi checke noget med components
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new PackedBootstrapApp<>(this, w);
    }

    /**
     * A builder for an application driver. An instance of this interface is acquired by calling
     * {@link OldBootstrapApp#builder()}.
     */
    // /* sealed */ interface IBuilder /* permits PackedApplicationDriver.Builder */ {
    // Environment + Application Interface + Result

    // Refactoring
    //// En build(Wirelet... wirelets) metode
    //// Companion objects must be added in order of the recieving MethodHandle

    // Maaske konfigure man dem direkte paa extension support klassen
    //// Det jeg taener er at man maaske har mulighed for at konfigure dem. F.eks.
    // ServiceApplicationController.alwaysWrap();

    // Problemet her er at vi gerne maaske fx vil angive LaunchState for Lifetime.
    // Hvilket ikke er muligt

    // noget optional??? ellers
    /**
     * @param companions
     * @return this builder
     * @throws UnsupportedOperationException
     *             if this builder does not have a wrapper
     */
//        default Builder addCompanion(ExtensionBridge... companions) {
//            return this;
//        }

//        /**
//         * Disables 1 or more extensions. Attempting to use a disabled extension will result in an RestrictedExtensionException
//         * being thrown
//         * 
//         * @param extensionTypes
//         *            the types of extension to disable
//         * @return
//         */
//        Builder<A> disableExtension(Class<? extends Extension<?>> extensionType);

//        @SuppressWarnings("unchecked")
//        default Builder<A> requireExtension(Class<? extends Extension>... extensionTypes) {
//
//            return this;
//        }
//
//        default Builder<A> restartable() {
//            return this;
//        }
//
//        // Det er jo ogsaa en companion
//        default Builder<A> resultType(Class<?> resultType) {
//            throw new UnsupportedOperationException();
//        }
//
//        // overrideMirror???
//        default Builder<A> specializeMirror(Supplier<? extends ApplicationMirror> supplier) {
//            throw new UnsupportedOperationException();
//        }

    // Maaske kan man have et form for accept filter...

    // Vi skal soerge for vi ikke klasse initialisere... Det er det

    // Bliver de arvet??? Vil mene ja...
    // Naa men vi laver bare en host/app der saa kan goere det...

    // Kan ogsaa lave noget BiPredicate der tager
    // <Requesting extension, extension that was requested>

    // Spies

    // Kan jo altsaa ogsaa vaere en Wirelet...
    // WireletScope...

    /**
     * Indicates that the any application create by this driver is not runnable.
     * 
     * @return this builder
     */
    // https://en.wikipedia.org/wiki/Runtime_system
    // noRuntimeEnvironment appénwerwer wer

    // Add ApplicationRuntimeExtension to list of unsupported extensions
    // noApplicationRuntime
//        Builder disableApplicationRuntime(); // or notRunnable() (it was this originally)

    // Application can only take an assembly of this type...

    // fx disallow(BytecodeGenExtension.class);
    // fx disallow(ThreadExtension.class);
    // fx disallow(FileExtension.class);
    // fx disallow(NetExtension.class); -> you want to use network.. to bad for you...

//        default Builder linkExtensionBean(Class<? extends Extension> extensionType, Class<?> extensionBean) {
//            
//            // Taenker lidt den bliver erstattet af ApplicationController?
//            
//            // extension must be available...
//            // An extensionBean of the specified type must be installed by the extension in the root container
//            return this;
//        }
    // }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    public static final class SingleShotApplicationImage<A> implements ApplicationLauncher<A> {

        private final AtomicReference<ReusableApplicationImage<A>> ref;

        SingleShotApplicationImage(PackedBootstrapApp<A> driver, ApplicationSetup application) {
            this.ref = new AtomicReference<>(new ReusableApplicationImage<>(driver, application));
        }

        @SuppressWarnings("unused")
        private RuntimeApplicationLauncher launcher() {
            ReusableApplicationImage<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException("This image has already been used");
            }
            return img.application.codeGenerator.launcher;
        }

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            ReusableApplicationImage<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException("This image has already been used");
            }
            // Not sure we can GC anything here
            // Think we need to extract a launcher and call it
            return img.launch(wirelets);
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link OldBootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    public /* primitive */ record ReusableApplicationImage<A>(PackedBootstrapApp<A> driver, ApplicationSetup application) implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(Wirelet... wirelets) {
            return application.codeGenerator.launcher.launchFromImage(driver, wirelets);
        }
    }

    /** A application launcher that maps the result of the launch. */
    public record MappedApplicationImage<A, F>(ApplicationLauncher<F> image, Function<? super F, ? extends A> mapper) implements ApplicationLauncher<A> {

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
