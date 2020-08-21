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
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.base.Attribute;
import app.packed.base.AttributeHolder;
import app.packed.base.AttributeStream;

/**
 *
 */
public abstract class AbstractAttributeHolderStream<T extends AttributeHolder> extends AbstractDelegatingStream<T> implements AttributeStream<T> {

    /**
     * @param stream
     */
    protected AbstractAttributeHolderStream(Stream<T> stream) {
        super(stream);
    }

    @Override
    public <A> AttributeStream<T> filter(Attribute<A> attribute, Predicate<? super A> predicate) {
        return with(filter(e -> e.attributes().testIfPresent(attribute, predicate, false)));
    }

    @Override
    public AttributeStream<T> ifPresent(Attribute<?> attribute) {
        return with(filter(e -> e.attributes().isPresent(attribute)));
    }

    /** {@inheritDoc} */
    @Override
    public <A extends Collection<B>, B> AttributeStream<T> contains(Attribute<A> attribute, B element) {
        return with(filter(e -> e.attributes().testIfPresent(attribute, f -> f.contains(element), false)));
    }

    @Override
    protected abstract AbstractAttributeHolderStream<T> with(Stream<T> s);
}
