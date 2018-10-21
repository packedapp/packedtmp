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

import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;
import app.packed.inject.TypeLiteralOrKey;
import packed.inject.InjectAPI;

/**
 * An internal factory.
 * <p>
 * Instances of this this class are <b>never</b> exposed to users.
 */
public abstract class InternalFactory<T> {

    // Correctness
    // Instance -> Lowerbound correct, upper correct
    // Executable -> Lower bound maybe correct (if exposedType=return type), upper correct if final return type
    // Rest, unknown all
    // Bindable -> has no effect..

    static {
        Dependency.of(String.class);// Initializes InternalApis for InternalFactory
    }

    /** The type of objects this factory creates. */
    private final TypeLiteral<T> typeLiteral;

    private final Key<T> key;
    private final Class<? super T> type;

    // Ideen er her. at for f.eks. Factory.of(XImpl, X) saa skal der stadig scannes paa Ximpl og ikke paa X
    final Class<?> actualType;

    public InternalFactory(TypeLiteralOrKey<T> typeLiteralOrKey) {
        this(typeLiteralOrKey, typeLiteralOrKey.getRawType());
    }

    /** Creates a factory with no dependencies. */
    public InternalFactory(TypeLiteralOrKey<T> typeLiteralOrKey, Class<?> actualType) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        if (typeLiteralOrKey instanceof Key) {
            this.key = (Key<T>) typeLiteralOrKey;
            this.typeLiteral = key.getTypeLiteral();
        } else {
            this.key = typeLiteralOrKey.toKey();
            this.typeLiteral = (TypeLiteral<T>) typeLiteralOrKey;
        }
        this.type = typeLiteral.getRawType();
        this.actualType = requireNonNull(actualType);

    }

    public abstract Class<T> forScanning();

    public InternalFactory<T> withMethodLookup(MethodHandles.Lookup l) {
        throw new UnsupportedOperationException("This method is only supported by factories that was created using a Constructor, Method or MethodHandler");
    }

    /**
     * @return the dependencies
     */
    public abstract List<Dependency> getDependencies();

    public Key<T> getKey() {
        return key;
    }

    public int getNumberOfUnresolvedDependencies() {
        return getDependencies().size();
    }

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
     * {@link org.cakeframework.lifecycle.OnStart} and {@link app.packed.inject.Provides}. This might differ from the
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
     * @param params
     * @return
     */
    public abstract T instantiate(Object[] params);

    protected T checkLowerbound(T instance) {
        if (!type.isInstance(instance)) {
            throw new InjectionException("Expected factory to produce an instance of " + format(type) + " but was " + instance.getClass());
        }
        return instance;
    }

    public static <T> InternalFactory<T> from(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return InjectAPI.fromFactory(factory);
    }

    static class CachedFactoryDefinition {
        final List<Dependency> dependencies;

        final TypeLiteral<?> objectType;

        CachedFactoryDefinition(TypeLiteral<?> objectType, List<Dependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }
}
