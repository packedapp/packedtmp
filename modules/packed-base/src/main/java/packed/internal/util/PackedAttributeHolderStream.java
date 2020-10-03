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

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.base.Attribute;
import app.packed.base.AttributedElement;
import app.packed.base.AttributedElementStream;

/**
 *
 */
public class PackedAttributeHolderStream<T extends AttributedElement> extends AbstractDelegatingStream<T> implements AttributedElementStream<T> {

    /**
     * @param stream
     */
    public PackedAttributeHolderStream(Stream<T> stream) {
        super(stream);
    }

    @Override
    public <A> AttributedElementStream<T> filter(Attribute<A> attribute, Predicate<? super A> predicate) {
        return with(filter(e -> e.attributes().testIfPresent(attribute, predicate, false)));
    }

    @Override
    public AttributedElementStream<T> ifPresent(Attribute<?> attribute) {
        return with(filter(e -> e.attributes().isPresent(attribute)));
    }

    /** {@inheritDoc} */
    @Override
    public <A extends Collection<E>, E> AttributedElementStream<T> valueContains(Attribute<A> attribute, E element) {
        return with(filter(e -> e.attributes().valueContains(attribute, element)));
        // return with(filter(e -> e.attributes().testIfPresent(attribute, f -> f.contains(element), false)));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> dropWhile(Predicate<? super T> predicate) {
        return with(stream.dropWhile(predicate));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> filter(Predicate<? super T> predicate) {
        return with(stream.filter(predicate));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> limit(long maxSize) {
        return with(stream.limit(maxSize));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> peek(Consumer<? super T> action) {
        return with(stream.peek(action));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> skip(long n) {
        return with(stream.skip(n));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> sorted(Comparator<? super T> comparator) {
        return with(stream.sorted(comparator));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> takeWhile(Predicate<? super T> predicate) {
        return with(stream.takeWhile(predicate));
    }

    /** {@inheritDoc} */
    @Override
    protected PackedAttributeHolderStream<T> with(Stream<T> s) {
        return new PackedAttributeHolderStream<T>(s);
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> distinct() {
        return with(stream.distinct());
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<T> sorted() {
        return with(stream.sorted());
    }
}
