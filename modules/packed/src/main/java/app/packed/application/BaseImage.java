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
package app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import app.packed.util.Result;
import internal.app.packed.application.Images.ImageEager;
import internal.app.packed.application.Images.ImageLazy;
import internal.app.packed.application.Images.ImageMapped;
import internal.app.packed.application.Images.ImageNonReusable;

/**
 * Represents a ,..
 * <p>
 * Instances of this class are typically not exposed to end-users. Instead it is typically wrapped in another image
 * class such as {@link App.Image}.
 */
public sealed interface BaseImage<A> permits ImageEager, ImageLazy, ImageNonReusable, ImageMapped {

    // Descriptors??? iisLazy, Mirror?

    // Do we want a more specific ApplicationResult? Something where the state is??
    // Maybe we can take a BiConsumer(ErrorContext, A)

    // Problem is here when is

    // Meningen er at prøve at styre fejl håndteringen bedre
    // <T> T BiFunction<@Nullable A, ErrorHandle e>

    default Result<A> compute(Object unhandledErrorHandler, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Failure before A is created,
    // Failure after A is created
    // Action -> Return something, or throw something
    // Tror ikke det giver mening foerend vi har en god error handling story
    default Result<A> compute(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Launches an instance of the application that this image represents.
     * <p>
     * What happens here is dependent on the underlying application template. The behaviour of this method is
     * identical to {@link BootstrapApp#launch(Assembly, Wirelet...)}.
     *
     * @param wirelets
     *            optional wirelets
     * @return an application instance
     *
     * @see BootstrapApp#launch(Assembly, Wirelet...)
     */
    A launch(RunState state, Wirelet... wirelets);

    /**
     * Returns a new base image that maps the result of a launch.
     *
     * @param <E>
     *            the type to map the launch result to
     * @param mapper
     *            the mapper
     * @return a new base image that maps the result of a launch
     */
    default <E> BaseImage<E> map(Function<? super A, ? extends E> mapper) {
        requireNonNull(mapper, "mapper is null");
        return new ImageMapped<>(this, mapper);
    }
}