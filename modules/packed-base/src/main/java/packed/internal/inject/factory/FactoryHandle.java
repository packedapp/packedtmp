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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;
import packed.internal.inject.dependency.DependencyDescriptor;

/**
 * The internal version of the {@link Factory} class.
 * <p>
 * Instances of this this class are <b>never</b> exposed to users.
 */
// Ideen er vel at lave hvad vi ville med MethodHandles her iestedet for...

// checker aldrig for null.
// Men checker den korrekte type???
public abstract class FactoryHandle<T> {

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

    public final Key<T> key;

    /** The dependencies for this factory. */
    private final Class<? super T> type;

    /** The type of objects this factory creates. */
    public final TypeLiteral<T> typeLiteral;

    FactoryHandle(TypeLiteral<T> typeLiteralOrKey, Class<?> actualType) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        this.typeLiteral = typeLiteralOrKey;
        this.key = Key.fromTypeLiteral(typeLiteral);
        this.type = typeLiteral.rawType();
        this.actualType = requireNonNull(actualType);
    }

    // TODO make package-private
    public FactoryHandle(TypeLiteral<T> typeLiteralOrKey) {
        this(typeLiteralOrKey, typeLiteralOrKey.rawType());
    }

    protected T checkLowerbound(T instance) {
        if (!type.isInstance(instance)) {
            // TODO I think this should probably be a Make Exception....
            // IDeen er at de har "løjet" om hvad de returnere.
            throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " + instance.getClass());
        }
        return instance;
    }

    /**
     * Returns the type of objects this operation returns on invocation.
     *
     * @return the type of objects this operation returns on invocation
     */
    public final TypeLiteral<T> returnType() {
        return typeLiteral;
    }

    public abstract List<DependencyDescriptor> dependencies();

    /**
     * Returns the raw type of objects this operation returns on invocation.
     *
     * @return the raw type of objects this operation returns on invocation
     */
    public final Class<? super T> returnTypeRaw() {
        return type;
    }

    public abstract MethodHandle toMethodHandle(Lookup lookup);

    FactoryHandle<T> withLookup(Lookup lookup) {
        throw new UnsupportedOperationException(
                "This method is only supported by factories created from a field, constructor or method. And must be applied as the first operation after creating the factory");
    }

    public static <T> FactoryHandle<T> of(BaseFactory<T> factory) {
        return factory.handle;
    }

    @SuppressWarnings("unchecked")
    public static <T> FactoryHandle<T> of(Class<T> implementation) {
        return (FactoryHandle<T>) FactoryHandle.of((BaseFactory<?>) Factory.of(implementation));
    }
}
