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
package app.packed.bundle.x;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.app.App;
import app.packed.container.Component;

/**
 *
 */
public interface AppStream extends Stream<App> {

    default void undeploy() {
        // Hmmmm... We should have an undeploy method...
    }

    /********** Overridden to provide co-a ComponentStream as a return value. **********/

    /** {@inheritDoc} */
    @Override
    default AppStream distinct() {
        return this; // All components are distinct by default
    }

    /** {@inheritDoc} */
    @Override
    AppStream dropWhile(Predicate<? super App> predicate);

    /** {@inheritDoc} */
    @Override
    AppStream filter(Predicate<? super App> predicate);

    /** {@inheritDoc} */
    @Override
    AppStream limit(long maxSize);

    /** {@inheritDoc} */
    @Override
    AppStream peek(Consumer<? super App> action);

    /** {@inheritDoc} */
    @Override
    AppStream skip(long n);

    /** Returns a new component stream where components are sorted by their {@link Component#path()}. */
    @Override
    default AppStream sorted() {
        return sorted((a, b) -> a.name().compareTo(b.name()));
    }

    /** {@inheritDoc} */
    @Override
    AppStream sorted(Comparator<? super App> comparator);
}
