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
package app.packed.inject;

import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link Factory} type that wraps a single {@link Function} to create new instances. The input to the function being
 * a single dependency.
 * <p>
 * Is typically used like this:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(new FunctionFactory<>(System::currentTimeMillis)).setDescription("Startup Time");}
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
 * You can also depend on a {@link org.cakeframework.inject.InjectionSite} to get greater detail about the client
 * requesting the service. For example, there we return a different logger to each component that requests a logger,
 * based on the components full path. For example, if a component with the path {@code /jobs/MyJob} requests a logger, a
 * logger with the name {@code jobs.MyJob} is returned. If it is not a component that requests the logger, an anonymous
 * logger is returned.
 *
 *
 *
 * <p>
 * As an alternative one of the static factory methods can be used:
 *
 * <pre>{@code
 *   InjectorBuilder builder = new InjectorBuilder();
 *   builder.bind(SupplierFactory.of(System::currentTimeMillis, Long.class)).setDescription("Startup Time");}
 * </pre>
 *
 * @param <T>
 *            the type of objects this factory constructs
 * @see Factory0
 * @see Factory2
 */
public abstract class Factory1<T, R> extends Factory<R> {

    protected Factory1(Function<? super T, ? extends R> function) {
        super(function);
    }

    /**
     * Creates a new factory that uses the specified function mapping existing instances of a specific type to new
     * instances.
     *
     * @param function
     *            the function mapping 1 type of dependencies to a new instances
     * @param objectType
     *            the type of objects the supplier creates
     * @return the new factory
     * @throws NullPointerException
     *             if the specified function, dependency type, or object type is null
     */

    // HMMMMMMMMM, Hvad gør vi med dependency????? Dependency<Optional<Foo>> kan jo aldrig match Foo....
    public static <T, R> Factory<R> of(Function<? super T, ? extends R> function, Class<T> dependencyType, Class<R> objectType) {
        throw new UnsupportedOperationException();
        // return new Factory1<>(Key.of(dependencyType), TypeLiteral.of(objectType), function) {};
    }

    public static <T, R> Factory<R> ofOptional(Function<Optional<? super T>, ? extends R> function, Class<T> dependencyType, Class<R> objectType) {
        throw new UnsupportedOperationException();
    }
}
