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
package app.packed.service;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;
import packed.internal.inject.factoryhandle.Factory1FactoryHandle;

/**
 * A special {@link Factory} type that takes a single dependency as input and uses a {@link Function} to dynamically provide new instances. The input
 * to the function being the single dependency.
 * <p>
 * Is typically used like this:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(new Factory1<>(System::currentTimeMillis).setDescription("Startup Time"){};}
 * </pre>
 * <p>
 * You can also use annotations on the dependency's type parameter. For example, say you have a {@link Qualifier} that
 * can read system properties, you can use something like this:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(new FunctionFactory<@SystemProperty("threads") Integer, ExecutorService>(t -> Executors.newFixedThreadPool(t)){});}
 * </pre>
 *
 * <p>
 * You can also depend on a InjectionSite to get greater detail about the client requesting the service. For example,
 * there we return a different logger to each component that requests a logger, based on the components full path. For
 * example, if a component with the path {@code /jobs/MyJob} requests a logger, a logger with the name
 * {@code jobs.MyJob} is returned. If it is not a component that requests the logger, an anonymous logger is returned.
 *
 * 
 * @see Factory0
 * @see Factory2
 */

//TODO fix example
/**
 * A special {@link Factory} type that wraps a {@link Supplier} in order to dynamically provide new instances.
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Factory<Long> f = new Factory1<>(System::currentTimeMillis) {}};</pre>
 * <p>
 * In this example we create a new class inheriting from Factory0 is order to capture information about the suppliers
 * type variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining
 * type information at runtime.
 * 
 * @param <T>
 *            The type of the single dependency that this factory takes
 * @param <R>
 *            the type of objects this factory constructs
 * @see Factory0
 * @see Factory2
 */
public abstract class Factory1<T, R> extends Factory<R> {

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<ServiceDependency>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<ServiceDependency>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    ServiceDependency.fromTypeVariables((Class) type, Factory1.class, 0));
        }
    };

    /**
     * Creates a new factory.
     *
     * @param function
     *            the function to use for creating new instances
     * @throws InvalidDeclarationException
     *             if the type variable T or R could not be determined. Or if T does not represent a proper dependency, or R
     *             does not represent a proper key
     */
    protected Factory1(Function<? super T, ? extends R> function) {
        super(function);
    }

    /**
     * Creates a new factory support instance from an implementation of this class and a function.
     * 
     * @param implementation
     *            the class extending this class
     * @param function
     *            the function used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    static <T, R> FactorySupport<R> create(Class<?> implementation, Function<?, ? extends T> function) {
        Entry<TypeLiteral<?>, List<ServiceDependency>> fs = CACHE.get(implementation);
        return new FactorySupport<>(new Factory1FactoryHandle<>((TypeLiteral<R>) fs.getKey(), (Function<? super T, ? extends R>) function), fs.getValue());
    }
}
