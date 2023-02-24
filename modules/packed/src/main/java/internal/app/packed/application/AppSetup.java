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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.container.CompositeWirelet;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.lifetime.PackedContainerLifetimeChannel;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;
import internal.app.packed.util.ThrowableUtil;

/** The internal representation of a bootstrap app. */
public final class AppSetup extends ApplicationDriver {

    public final List<PackedContainerLifetimeChannel> channels;

    private final LifetimeKind lifetimeKind;

    /** The method handle used for creating new application instances. */
    // We need more info for bootstrap mirrors
    private final MethodHandle mhConstructor; // (ApplicationLaunchContext)Object

    /** Supplies a mirror for the application. */
    public final Supplier<? extends ApplicationMirror> mirrorSupplier;

    /** Optional (flattened) wirelets that will be applied to any applications created by this driver. */
    @Nullable
    public final Wirelet wirelet;

    public AppSetup(LifetimeKind lifetimeKind, Supplier<? extends ApplicationMirror> mirrorSupplier, List<PackedContainerLifetimeChannel> channels,
            MethodHandle mh, Wirelet wirelet) {
        this.wirelet = wirelet;
        this.mhConstructor = requireNonNull(mh);
        this.mirrorSupplier = requireNonNull(mirrorSupplier);
        this.lifetimeKind = requireNonNull(lifetimeKind);
        this.channels = List.copyOf(channels);
    }

    /** {@inheritDoc} */
    @Override
    public List<PackedContainerLifetimeChannel> channels() {
        return channels;
    }

    /**
     * Returns whether or not applications produced by this driver have an {@link ManagedLifetimeController}.
     * <p>
     * Applications that are not runnable will always be launched in the Initial state.
     *
     * @return whether or not the applications produced by this driver are runnable
     */
    @Override
    public LifetimeKind lifetimeKind() {
        return lifetimeKind;
    }

    /** {@inheritDoc} */
    @Override
    public Supplier<? extends ApplicationMirror> mirrorSupplier() {
        return mirrorSupplier;
    }

    /**
     * Create a new application instance using the specified launch context.
     *
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    public Object newInstance(ApplicationInitializationContext context) {
        try {
            return mhConstructor.invokeExact(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wirelet() {
        return wirelet;
    }

    /** {@inheritDoc} */
    public AppSetup with(Wirelet... wirelets) {
        // Skal vi checke noget med components
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new AppSetup(lifetimeKind, mirrorSupplier, channels, mhConstructor, w);
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    public static final class SingleShotApplicationImage<A> implements ApplicationLauncher<A> {

        private final AtomicReference<ReusableApplicationImage<A>> ref;

        public SingleShotApplicationImage(AppSetup driver, ApplicationSetup application) {
            this.ref = new AtomicReference<>(new ReusableApplicationImage<>(driver, application));
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
    public /* primitive */ record ReusableApplicationImage<A>(AppSetup driver, ApplicationSetup application) implements ApplicationLauncher<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletWrapper wrapper = null;
            if (wirelets.length > 0) {
                wrapper = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationInitializationContext aic = ApplicationInitializationContext.launch2(application, wrapper);

            return (A) driver.newInstance(aic);
        }
    }

    /** A application launcher that maps the result of the launch. */
    public /* primitive */ record MappedApplicationImage<A, F>(ApplicationLauncher<F> image, Function<? super F, ? extends A> mapper)
            implements ApplicationLauncher<A> {

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
