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
package packed.inject.factory;

import static java.util.Objects.requireNonNull;
import static packed.util.Formatter.format;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.BaseStream;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.inject.TypeLiteralOrKey;

/** An internal factory that keeps returns the same instance. */
public final class InternalFactoryOfIteratorOrStream<T> extends InternalFactory<T> {

    final boolean fromStream;

    /** The instance that is returned every time. */
    private final Iterator<? extends T> iterator;

    /** A lock to make sure multiple concurrent threads to not attempt to simultaneous create a thread. */
    private final Semaphore lock = new Semaphore(1);

    public InternalFactoryOfIteratorOrStream(TypeLiteralOrKey<T> objectType, BaseStream<? extends T, ? extends BaseStream<T, ?>> stream) {
        super(objectType);
        requireNonNull(stream, "stream is null");
        this.iterator = requireNonNull(stream.iterator(), "stream().iterator() is null");
        this.fromStream = true;
    }

    public InternalFactoryOfIteratorOrStream(TypeLiteralOrKey<T> objectType, Iterator<? extends T> iterator) {
        super(objectType);
        this.iterator = requireNonNull(iterator, "iterator is null");
        this.fromStream = false;
    }

    /** {@inheritDoc} */
    @Override
    public Class<T> forScanning() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> getDependencies() {
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public T instantiate(Object[] ignore) {
        lock.acquireUninterruptibly();
        try {
            if (iterator.hasNext()) {
                T instance = iterator.next();
                if (!getRawType().isInstance(instance)) {
                    String name = fromStream ? "stream" : "iterator";
                    throw new InjectionException("The " + name + " used when creating a Factory instance was expected to return instances of '"
                            + format(getRawType()) + "', but it returned an instance of '" + format(instance.getClass()) + "'");
                }
                return instance;
            }
            throw new RuntimeException("Iterator is empty nothing more to do");
        } finally {
            lock.release();
        }
    }

    public static <T> InternalFactory<T> ofIterator(Iterator<? extends T> iterator, Class<T> type) {
        return ofIterator(iterator, TypeLiteral.of(type));
    }

    /**
     * Creates a new factory that returns the specified constructor on every invocation.
     *
     * @param instance
     *            the instance to return on every invocation
     * @param type
     *            a type literal
     * @return the new factory
     * @see #ofConstructor(Constructor)
     */
    public static <T> InternalFactory<T> ofIterator(Iterator<? extends T> iterator, TypeLiteralOrKey<T> type) {
        return new InternalFactoryOfIteratorOrStream<>(type, iterator);
    }

    public static <T> InternalFactory<T> ofStream(BaseStream<? extends T, ? extends BaseStream<T, ?>> stream, Class<T> type) {
        return ofStream(stream, TypeLiteral.of(type));
    }

    /**
     * Creates a new factory that returns the specified constructor on every invocation.
     *
     * @param instance
     *            the instance to return on every invocation
     * @param type
     *            a type literal
     * @return the new factory
     * @see #ofConstructor(Constructor)
     */
    public static <T> InternalFactory<T> ofStream(BaseStream<? extends T, ? extends BaseStream<T, ?>> stream, TypeLiteralOrKey<T> type) {
        return new InternalFactoryOfIteratorOrStream<>(type, stream);
    }
}
