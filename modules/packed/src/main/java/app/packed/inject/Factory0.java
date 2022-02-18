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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;

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
 * @see Factory
 * @see Factory2
 */
// Maybe define a CustomFactory we can override...
// I think so... And maybe install a MethodHand use $toConcract... Nahh kan ikke bruge $ fordi vi overskriver...
public abstract class Factory0<R> extends CapturingFactory<R> {

    /**
     * Creates a new factory, that use the specified supplier to provide values.
     *
     * @param supplier
     *            the supplier that will provide the actual values. The supplier should never return null, but should
     *            instead throw a relevant exception if unable to provide a value
     * @throws FactoryException
     *             if the type variable R could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory0(Supplier<? extends R> supplier) {
        super(requireNonNull(supplier, "supplier is null"));
    }
}
