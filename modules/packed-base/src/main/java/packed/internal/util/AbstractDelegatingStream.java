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
package packed.internal.util;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/** A stream that delegates most methods to another stream. */
public abstract class AbstractDelegatingStream<T> implements Stream<T> {

    /** The stream we are wrapping. */
    protected final Stream<T> stream;

    /**
     * Creates a new AbstractDelegatingStream.
     *
     * @param stream
     *            the stream to delegate to
     */
    public AbstractDelegatingStream(Stream<T> stream) {
        this.stream = requireNonNull(stream);
    }

    /** {@inheritDoc} */
    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream.allMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream.anyMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        stream.close();
    }

    /** {@inheritDoc} */
    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    /** {@inheritDoc} */
    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        return stream.count();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<T> findAny() {
        return stream.findAny();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    /** {@inheritDoc} */
    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public void forEach(Consumer<? super T> action) {
        stream.forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return stream.map(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream.max(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream.min(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream.noneMatch(predicate);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return stream.onClose(closeHandler);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<T> parallel() {
        return stream.parallel();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    /** {@inheritDoc} */
    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    /** {@inheritDoc} */
    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<T> sequential() {
        return stream.sequential();
    }

    /** {@inheritDoc} */
    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<T> unordered() {
        return stream.unordered();
    }

    /**
     * Returns a new delegated stream.
     *
     * @param s
     *            the stream that is being delegated to
     * @return a wrapped stream
     */
    protected abstract AbstractDelegatingStream<T> with(Stream<T> s);
}
