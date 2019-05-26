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
package app.packed.component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A specialized stream of component instances.
 */
public interface NewComponentInstanceStream<T> extends Stream<NewComponentInstance<T>> {

    // ComponentInstanceStream<T> inState

    @SuppressWarnings("unchecked")
    default <S> NewComponentInstanceStream<S> filterOnComponent(Predicate<? super Component> predicate) {
        return (NewComponentInstanceStream<S>) filter(c -> {
            throw new UnsupportedOperationException();
        });
    }

    @SuppressWarnings("unchecked")
    default <S> NewComponentInstanceStream<S> filterOnInstanceType(Class<S> type) {
        return (NewComponentInstanceStream<S>) filter(c -> {
            throw new UnsupportedOperationException();
            // return c.instance().getClass().isAssignableFrom(type);
        });
    }

    default void forEachInstance(Consumer<? super T> action) {
        instances().forEach(action);
    }

    /**
     * Returns a stream of the object that each component instance wraps.
     * <p>
     * This is a <em>stateful intermediate operation</em>.
     * 
     * @return a stream of the object that each component instance wraps
     */
    Stream<T> instances();

    /**
     * Returns a new list containing all of the instances of each component in this stream in the order they where
     * encountered. Is identical to invoking {@code stream.instances().collect(Collectors.toList())}.
     * <p>
     * This is a <em>terminal operation</em>.
     *
     * @return a new list containing all of the components in this stream
     */
    default List<T> toInstanceList() {
        return instances().collect(Collectors.toList());
    }

    /**
     * Returns a new list containing all of the components in this stream in the order they where encountered. Is identical
     * to invoking {@code stream.collect(Collectors.toList())}.
     * <p>
     * This is a <em>terminal operation</em>.
     *
     * @return a new list containing all of the components in this stream
     */
    default List<NewComponentInstance<T>> toList() {
        return collect(Collectors.toList());
    }

    /********** Overridden to provide a ComponentInstanceStream as return value. **********/

    /** {@inheritDoc} */
    @Override
    default NewComponentInstanceStream<T> distinct() {
        return this; // All components are distinct by default
    }

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> dropWhile(Predicate<? super NewComponentInstance<T>> predicate);

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> filter(Predicate<? super NewComponentInstance<T>> predicate);

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> limit(long maxSize);

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> peek(Consumer<? super NewComponentInstance<T>> action);

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> skip(long n);

    /** Returns a new component stream where components are sorted by their {@link Component#path()}. */
    @Override
    default NewComponentInstanceStream<T> sorted() {
        throw new UnsupportedOperationException();
        // return sorted((a, b) -> a.path().compareTo(b.path()));
    }

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> sorted(Comparator<? super NewComponentInstance<T>> comparator);

    /** {@inheritDoc} */
    @Override
    NewComponentInstanceStream<T> takeWhile(Predicate<? super NewComponentInstance<T>> predicate);
}
