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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.application.BootstrapApp.Image;
import app.packed.binding.Key;
import app.packed.runtime.RunState;
import internal.app.packed.ValueBased;
import internal.app.packed.application.deployment.FutureApplicationSetup;
import internal.app.packed.lifecycle.lifetime.runtime.ApplicationLaunchContext;

/** Various implementations of {@link BaseImage} */

// Versions
//// Mapped
//// Lazy   (Maybe take a Lazy constant)
//// Handle (Eager)
public sealed interface PackedBootstrapImage<A> extends BootstrapApp.Image<A> {

    @Override
    default ApplicationMirror mirror() {
        throw new UnsupportedOperationException();
    }

    @Override
    default String name() {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T> ApplicationLauncher provide(Key<? super T> key, T value) {
        throw new UnsupportedOperationException();
    }

    /** A application image that maps the result of a launch. */
    @ValueBased
    public record ImageMapped<A, F>(Image<F> image, Function<? super F, ? extends A> mapper) implements PackedBootstrapImage<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state) {
            F result = image.launch(state);
            return mapper.apply(result);
        }

        /** {@inheritDoc} */
        @Override
        public <E> Image<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            Function<? super F, ? extends E> andThen = this.mapper.andThen(mapper);
            return new ImageMapped<>(image, andThen);
        }
//
//        /** {@inheritDoc} */
//        @Override
//        public A checkedLaunch(RunState state, Wirelet... wirelets) throws UnhandledApplicationException {
//            F result = image.checkedLaunch(state, wirelets);
//            return mapper.apply(result);
//        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    @ValueBased
    public record ImageNonReusable<A>(AtomicReference<Image<A>> ref) implements PackedBootstrapImage<A> {

        public ImageNonReusable(Image<A> image) {
            this(new AtomicReference<>(image));
        }

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state) {
            Image<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException(
                        "This image has already been used. You can use ApplicationWirelets.resuableImage() to allow repeatable usage of an application image");
            }
            // Not sure we can GC anything here
            // Think we need to extract a launcher and call it
            return img.launch(state);
        }
//
//        /** {@inheritDoc} */
//        @Override
//        public A checkedLaunch(RunState state, Wirelet... wirelets) throws UnhandledApplicationException {
//            BaseImage<A> img = ref.getAndSet(null);
//            if (img == null) {
//                throw new IllegalStateException(
//                        "This image has already been used. You can use ApplicationWirelets.resuableImage() to allow repeatable usage of an application image");
//            }
//            // Not sure we can GC anything here
//            // Think we need to extract a launcher and call it
//            return img.checkedLaunch(state, wirelets);
//        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link OldBootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    @ValueBased
    public record ImageEager<A>(ApplicationHandle<A, ?> handle) implements PackedBootstrapImage<A> {

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror mirror() {
            return handle.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return handle.name();
        }

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state) {
            return ApplicationLaunchContext.launch(handle, state);
        }
    }

    @ValueBased
    public record ImageLazy<A>(PackedApplicationTemplate<?> template, FutureApplicationSetup application) implements PackedBootstrapImage<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(RunState state) {
            ApplicationHandle<?, ?> ah = application.lazyBuild().handle();
            return (A) ApplicationLaunchContext.launch(ah, state);
        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("unchecked")
//        @Override
//        public A checkedLaunch(RunState state, Wirelet... wirelets) throws UnhandledApplicationException {
//            ApplicationHandle<?, ?> ah = application.lazyBuild().handle();
//            return (A) ApplicationLaunchContext.checkedLaunch(ah, state, wirelets);
//        }
    }
}
