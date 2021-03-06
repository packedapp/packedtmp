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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.util.LookupUtil;

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
public abstract class Factory1<T, R> extends Factory<R> {

    /** A method handle for invoking {@link #create(Function, Class, Object)}. */
    private static final MethodHandle CREATE = LookupUtil.lookupStatic(MethodHandles.lookup(), "create", Object.class, Function.class, Class.class,
            Object.class);

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    static final ClassValue<List<DependencyDescriptor>> FACTORY1_DEPENDENCY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<DependencyDescriptor> computeValue(Class<?> type) {
            return DependencyDescriptor.fromTypeVariables((Class) type, Factory1.class, 0);
        }
    };

    /** The dependencies of this factory, extracted from the type variables of the subclass. */
    private final List<DependencyDescriptor> dependencies;

    /** The method handle responsible for providing the actual values. */
    private MethodHandle methodHandle;

    /**
     * Creates a new factory, that uses the specified function to provide instances.
     *
     * @param function
     *            the function that provide instances. The function should never return null, but should instead throw a
     *            relevant exception if unable to provide a value
     * @throws FactoryException
     *             if any of type variables could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory1(Function<? super T, ? extends R> function) {
        requireNonNull(function, "function is null");
        this.dependencies = FACTORY1_DEPENDENCY_CACHE.get(getClass());

        Class<?> ret = rawType();
        Class<?> param = dependencies.get(0).rawType();
        MethodHandle mh = CREATE.bindTo(function).bindTo(ret); // (Function, Class, Object)Object -> (Object)Object
        this.methodHandle = MethodHandles.explicitCastArguments(mh, MethodType.methodType(ret, param)); // (Object)Object -> (T)R
    }

    /** {@inheritDoc} */
    @Override
    List<DependencyDescriptor> dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    MethodHandle toMethodHandle(Lookup ignore) {
        return methodHandle;
    }

    /**
     * Supplies a value.
     * 
     * @param <T>
     *            the type of value supplied
     * @param function
     *            the function that supplies the actual value
     * @param expectedType
     *            the type we expect the supplier to return
     * @param object
     *            the single argument to the function
     * @return the value that was supplied by the specified supplier
     * @throws FactoryException
     *             if the created value is null or not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create(Function<Object, ? extends T> function, Class<?> expectedType, Object object) {
        T value = function.apply(object);
        checkReturnValue(expectedType, value, function);
        return value;
    }
}
