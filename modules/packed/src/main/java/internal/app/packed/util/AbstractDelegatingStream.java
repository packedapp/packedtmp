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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A partial implemented stream that delegates most methods to another stream.
 * <p>
 * The methods that have not been implemented by this class, is all the methods that has Stream as the returned. And
 * which specifically does not change the type of elements. For example {@link #map(Function)} should return Stream and not
 * SubClassStream.
 */
public abstract class AbstractDelegatingStream<T> implements Stream<T> {

    /** The stream we are wrapping. */
    protected final Stream<T> stream;

    /**
     * Creates a new AbstractDelegatingStream.
     *
     * @param stream
     *            the stream to delegate to
     */
    protected AbstractDelegatingStream(Stream<T> stream) {
        this.stream = requireNonNull(stream, "stream is null");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean allMatch(Predicate<? super T> predicate) {
        return stream.allMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean anyMatch(Predicate<? super T> predicate) {
        return stream.anyMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public final void close() {
        stream.close();
    }

    /** {@inheritDoc} */
    @Override
    public final <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    /** {@inheritDoc} */
    @Override
    public final <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    /** {@inheritDoc} */
    @Override
    public final long count() {
        return stream.count();
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<T> findAny() {
        return stream.findAny();
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<T> findFirst() {
        return stream.findFirst();
    }

    /** {@inheritDoc} */
    @Override
    public final <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(Consumer<? super T> action) {
        stream.forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public final void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);
    }

    @SuppressWarnings("preview")
    @Override
    public final <R> Stream<R> gather(Gatherer<? super T, ?, R> gatherer) {
        return stream.gather(gatherer);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isParallel() {
        return stream.isParallel();
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<T> iterator() {
        return stream.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public final <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return stream.map(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final <R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        return stream.mapMulti(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleStream mapMultiToDouble(BiConsumer<? super T, ? super DoubleConsumer> mapper) {
        return stream.mapMultiToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final IntStream mapMultiToInt(BiConsumer<? super T, ? super IntConsumer> mapper) {
        return stream.mapMultiToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final LongStream mapMultiToLong(BiConsumer<? super T, ? super LongConsumer> mapper) {
        return stream.mapMultiToLong(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<T> max(Comparator<? super T> comparator) {
        return stream.max(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<T> min(Comparator<? super T> comparator) {
        return stream.min(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean noneMatch(Predicate<? super T> predicate) {
        return stream.noneMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public final Stream<T> onClose(Runnable closeHandler) {
        return stream.onClose(closeHandler);
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    /** {@inheritDoc} */
    @Override
    public final T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    /** {@inheritDoc} */
    @Override
    public final <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    /** {@inheritDoc} */
    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray() {
        return stream.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public final <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    /** {@inheritDoc} */
    @Override
    public final List<T> toList() {
        return stream.toList();
    }
}
