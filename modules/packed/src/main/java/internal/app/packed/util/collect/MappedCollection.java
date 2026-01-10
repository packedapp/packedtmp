/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.collect;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 */
public class MappedCollection<E, M> extends ImmutableCollection<M> {

    final Collection<E> collection;

    final ValueMapper<E, ? extends M> mapper;

    public MappedCollection(Collection<E> collection, ValueMapper<E, ? extends M> mapper) {
        this.collection = collection;
        this.mapper = mapper;
    }


    /** {@inheritDoc} */
    @Override
    public Iterator<M> iterator() {
        return new MappedIterator<>(collection.iterator(), mapper);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return collection.size();
    }
}
