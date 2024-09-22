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

import app.packed.application.BaseImage;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import internal.app.packed.ValueBased;
import internal.app.packed.application.deployment.FutureApplicationSetup;
import internal.app.packed.container.wirelets.CompositeWirelet;
import internal.app.packed.container.wirelets.WireletSelectionArray;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/** Various implementations of {@link BaseImage} */
public sealed interface PackedBaseImage<A> extends BaseImage<A> {

    /** A application image that maps the result of a launch. */
    @ValueBased
    public record ImageMapped<A, F>(BaseImage<F> image, Function<? super F, ? extends A> mapper) implements PackedBaseImage<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state, Wirelet... wirelets) {
            F result = image.launch(state, wirelets);
            return mapper.apply(result);
        }

        /** {@inheritDoc} */
        @Override
        public <E> BaseImage<E> map(Function<? super A, ? extends E> mapper) {
            requireNonNull(mapper, "mapper is null");
            Function<? super F, ? extends E> andThen = this.mapper.andThen(mapper);
            return new ImageMapped<>(image, andThen);
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link BootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    @ValueBased
    public record ImageNonReusable<A>(AtomicReference<BaseImage<A>> ref) implements PackedBaseImage<A> {

        public ImageNonReusable(BaseImage<A> image) {
            this(new AtomicReference<>(image));
        }

        public ImageNonReusable(PackedApplicationTemplate<?> template, ApplicationSetup application) {
            this(new AtomicReference<>(new ImageEager<>(application)));
        }

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state, Wirelet... wirelets) {
            BaseImage<A> img = ref.getAndSet(null);
            if (img == null) {
                throw new IllegalStateException(
                        "This image has already been used. You can use ApplicationWirelets.resuableImage() to allow repeatable usage of an application image");
            }
            // Not sure we can GC anything here
            // Think we need to extract a launcher and call it
            return img.launch(state, wirelets);
        }
    }

    /**
     * Implementation of {@link ApplicationLauncher} used by {@link OldBootstrapApp#newImage(Assembly, Wirelet...)}.
     */
    @ValueBased
    public /* value */ record ImageEager<A>(ApplicationSetup application) implements PackedBaseImage<A> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public A launch(RunState state, Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletSelectionArray<?> wrapper = null;
            if (wirelets.length > 0) {
                wrapper = WireletSelectionArray.of(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationLaunchContext aic = ApplicationLaunchContext.launch(state, application, wrapper);

            return (A) application.template.newHolder(aic);
        }
    }

    @ValueBased
    public record ImageLazy<A>(PackedApplicationTemplate<A> template, FutureApplicationSetup application) implements PackedBaseImage<A> {

        /** {@inheritDoc} */
        @Override
        public A launch(RunState state, Wirelet... wirelets) {
            requireNonNull(wirelets, "wirelets is null");

            // If launching an image, the user might have specified additional runtime wirelets
            WireletSelectionArray<?> wrapper = null;
            if (wirelets.length > 0) {
                wrapper = WireletSelectionArray.of(CompositeWirelet.flattenAll(wirelets));
            }
            ApplicationLaunchContext aic = ApplicationLaunchContext.launch(state, application.lazyBuild(), wrapper);

            return template.newHolder(aic);
        }
    }
}
