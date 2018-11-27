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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.inject.Factory;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;
import app.packed.lifecycle.OnStart;
import app.packed.util.Nullable;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.InternalDependency;

/**
 * The internal version of the {@link Factory} class.
 * <p>
 * Instances of this this class are <b>never</b> exposed to users.
 */
public abstract class InternalFactory<T> {

    // Dependencies
    // Key -> The key under which the factory will be register unless registered under another key

    //////// TYPES (Raw)
    // ExactType... -> Instance, Constructor
    // LowerBoundType, Field, Method
    // PromisedType -> Fac0,Fac1,Fac2,

    /// TypeLiteral<- Always the promised, key must be assignable via raw type
    ///////////////

    // TypeLiteral
    // actual type

    // Correctness
    // Instance -> Lowerbound correct, upper correct
    // Executable -> Lower bound maybe correct (if exposedType=return type), upper correct if final return type
    // Rest, unknown all
    // Bindable -> has no effect..

    // static {
    // Dependency.of(String.class);// Initializes InternalApis for InternalFactory
    // }

    // Ideen er her. at for f.eks. Factory.of(XImpl, X) saa skal der stadig scannes paa Ximpl og ikke paa X
    final Class<?> actualType;

    /** The dependencies for this factory. */
    private final List<InternalDependency> dependencies;

    /** The key that this factory will be registered under by default with an injector. */
    private final Key<T> key;

    private final Class<? super T> type;

    /** The type of objects this factory creates. */
    private final TypeLiteral<T> typeLiteral;

    public InternalFactory(TypeLiteral<T> typeLiteralOrKey, List<InternalDependency> dependencies) {
        this(typeLiteralOrKey, dependencies, typeLiteralOrKey.getRawType());
    }

    public InternalFactory(TypeLiteral<T> typeLiteralOrKey, List<InternalDependency> dependencies, Class<?> actualType) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        this.dependencies = requireNonNull(dependencies);
        this.key = typeLiteralOrKey.toKey();
        this.typeLiteral = typeLiteralOrKey;
        this.type = typeLiteral.getRawType();
        this.actualType = requireNonNull(actualType);
    }

    protected T checkLowerbound(T instance) {
        if (!type.isInstance(instance)) {
            throw new InjectionException("Expected factory to produce an instance of " + format(type) + " but was " + instance.getClass());
        }
        return instance;
    }

    /**
     * Returns a list of all of this factory's dependencies.
     * 
     * @return a list of all of this factory's dependencies
     */
    public final List<InternalDependency> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the key that this factory will be made available under if registering with an injector.
     * 
     * @return the key that this factory will be made available under if registering with an injector
     */
    public final Key<T> getKey() {
        return key;
    }

    public abstract Class<?> getLowerBound();

    /**
     * Returns the raw type of objects this factory creates.
     *
     * @return the raw type of objects this factory creates
     */
    public final Class<? super T> getRawType() {
        return type;
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for annotations such as
     * {@link OnStart} and {@link app.packed.inject.Provides}. This might differ from the
     *
     * @return
     */
    Class<? super T> getScannableType() {
        return getRawType();
    }

    /**
     * Returns the type of objects this factory creates.
     *
     * @return the type of objects this factory creates
     */
    public final TypeLiteral<T> getType() {
        return typeLiteral;
    }

    /**
     * Instantiates a new object using the specified parameters
     * 
     * @param params
     *            the parameters to use
     * @return the new instance
     */
    @Nullable
    public abstract T instantiate(Object[] params);

    /**
     * Converts the specified factory to an internal factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory convert
     * @return the converted factory
     */
    public static <T> InternalFactory<T> from(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return InjectSupport.toInternalFactory(factory);
    }

    public InternalFactory<T> withLookup(Lookup lookup) {
        throw new UnsupportedOperationException("This method is only supported by factories that were created from a field, constructor or method");
    }

    static class FunctionalSignature {

        final List<InternalDependency> dependencies;

        final TypeLiteral<?> objectType;

        FunctionalSignature(TypeLiteral<?> objectType, List<InternalDependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }

}
//
/// **
// * @param lookup
// * the lookup object to test against
// * @return whether or not the this factory can create using the specified lookup object
// */
// public boolean isAccessibleWith(Lookup lookup) {
// return true;
// }