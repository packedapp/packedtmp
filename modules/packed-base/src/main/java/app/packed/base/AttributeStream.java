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
package app.packed.base;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** A stream of elements that hold attributes. */
public interface AttributeStream<T extends AttributeHolder> extends Stream<T> {

    <A extends Collection<B>, B> AttributeStream<T> contains(Attribute<A> attribute, B element);

    <A> AttributeStream<T> filter(Attribute<A> attribute, Predicate<? super A> predicate);

    // Kunne ogsaa returnere Stream<A>????
    // mapTo
    AttributeStream<T> ifPresent(Attribute<?> attribute);

    default <A> Stream<A> mapTo(Attribute<A> attribute) {
        return flatMap(c -> c.attributes().orStuff(attribute, t -> Stream.of(t), Stream.empty()));
    }
}
