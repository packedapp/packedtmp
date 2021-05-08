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
package packed.internal.component;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import packed.internal.util.PackedAttributeHolderStream;

/** Implementation of {@link ComponentMirrorStream}. */
final class PackedComponentStream extends PackedAttributeHolderStream<ComponentMirror> implements ComponentMirrorStream {

    /**
     * Creates a new component stream.
     *
     * @param stream
     *            the stream that we wrap.
     */
    PackedComponentStream(Stream<ComponentMirror> stream) {
        super(stream);
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream distinct() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream dropWhile(Predicate<? super ComponentMirror> predicate) {
        return with(stream.dropWhile(predicate));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream filter(Predicate<? super ComponentMirror> predicate) {
        return with(stream.filter(predicate));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream limit(long maxSize) {
        return with(stream.limit(maxSize));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream peek(Consumer<? super ComponentMirror> action) {
        return with(stream.peek(action));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream skip(long n) {
        return with(stream.skip(n));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream sorted() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream sorted(Comparator<? super ComponentMirror> comparator) {
        return with(stream.sorted(comparator));
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentStream takeWhile(Predicate<? super ComponentMirror> predicate) {
        return with(stream.takeWhile(predicate));
    }

    /** {@inheritDoc} */
    @Override
    protected PackedComponentStream with(Stream<ComponentMirror> s) {
        return new PackedComponentStream(s);
    }
}
