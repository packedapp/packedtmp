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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import app.packed.util.InvalidDeclarationException;
import app.packed.util.Qualifier;
import app.packed.util.TypeLiteral;
import packed.internal.inject.InternalDependency;
import packed.internal.invokers.InternalFactory1;

/**
 * A {@link Factory} type that takes a single dependency and uses a {@link Function} to create new instances. The input
 * to the function being the single dependency.
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
 * You can also depend on a InjectionSite to get greater detail about the client requesting the service. For example,
 * there we return a different logger to each component that requests a logger, based on the components full path. For
 * example, if a component with the path {@code /jobs/MyJob} requests a logger, a logger with the name
 * {@code jobs.MyJob} is returned. If it is not a component that requests the logger, an anonymous logger is returned.
 *
 * 
 * @param <T>
 *            the type of objects this factory constructs
 * @see Factory0
 * @see Factory2
 */
public abstract class Factory1<T, R> extends Factory<R> {

    /** A cache of function factory definitions. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<InternalDependency>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<InternalDependency>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    InternalDependency.fromTypeVariables((Class) type, Factory1.class, 0));
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

    @SuppressWarnings("unchecked")
    static <T, R> InternalFactory<R> create(Function<?, ? extends T> supplier, Class<?> typeInfo) {
        Entry<TypeLiteral<?>, List<InternalDependency>> fs = CACHE.get(typeInfo);
        return new InternalFactory<>(new InternalFactory1<>((TypeLiteral<R>) fs.getKey(), (Function<? super T, ? extends R>) supplier), fs.getValue());
    }

}
// *
// * <p>
// * As an alternative one of the static factory methods can be used:
// *
// * <pre>{@code
// * InjectorBuilder builder = new InjectorBuilder();
// * builder.bind(SupplierFactory.of(System::currentTimeMillis, Long.class)).setDescription("Startup Time");}
// * </pre>
// *