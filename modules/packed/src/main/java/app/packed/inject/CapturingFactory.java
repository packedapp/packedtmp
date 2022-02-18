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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.TypeToken;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.inject.InternalFactory;
import packed.internal.inject.InternalFactory.CanonicalizedCapturingInternalFactory;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
public abstract non-sealed class CapturingFactory<R> extends Factory<R> {

    /** A cache of extracted type variables from subclasses of this class. */
    static final ClassValue<TypeToken<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })

        protected TypeToken<?> computeValue(Class<?> type) {
            return TypeToken.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    /** A method handle for invoking {@link #create(Supplier, Class)}. */
    private static final MethodHandle CREATE0 = LookupUtil.lookupStatic(MethodHandles.lookup(), "create0", Object.class, Supplier.class, Class.class);

    /** A method handle for invoking {@link #create(Function, Class, Object)}. */
    private static final MethodHandle CREATE1 = LookupUtil.lookupStatic(MethodHandles.lookup(), "create1", Object.class, Function.class, Class.class,
            Object.class);

    /** A method handle for invoking {@link #create(BiFunction, Class, Object, Object)}. */
    private static final MethodHandle CREATE2 = LookupUtil.lookupStatic(MethodHandles.lookup(), "create2", Object.class, BiFunction.class, Class.class,
            Object.class, Object.class);

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<List<InternalDependency>> DEPENDENCY_CACHE2 = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<InternalDependency> computeValue(Class<?> type) {
            return InternalDependency.fromTypeVariables((Class) type, Factory2.class, 0, 1);
        }
    };
    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    static final ClassValue<List<InternalDependency>> FACTORY1_DEPENDENCY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<InternalDependency> computeValue(Class<?> type) {
            return InternalDependency.fromTypeVariables((Class) type, Factory1.class, 0);
        }
    };

    static void checkReturnValue(Class<?> expectedType, Object value, Object supplierOrFunction) {
        if (!expectedType.isInstance(value)) {
            String type = Supplier.class.isAssignableFrom(supplierOrFunction.getClass()) ? "supplier" : "function";
            if (value == null) {
                // NPE???
                throw new FactoryException("The " + type + " '" + supplierOrFunction + "' must not return null");
            } else {
                // throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " +
                // instance.getClass());
                throw new FactoryException("The \" + type + \" '" + supplierOrFunction + "' was expected to return instances of type " + expectedType.getName()
                        + " but returned a " + value.getClass().getName() + " instance");
            }
        }
    }

    /** The dependencies of this factory, extracted from the type variables of the subclass. */
    // Taenker vi laver en private record delegate der holder begge ting...
    // Og saa laeser
    private final List<InternalDependency> dependencies;

    final MethodHandle methodHandle;

    /** The type of objects this factory creates. */
    private final TypeToken<R> typeLiteral;

    /** The type of objects this factory creates. */
    @SuppressWarnings("unused")
    private final CanonicalizedCapturingInternalFactory<R> factory;

    /**
     * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
     * constructor in this (super) class.
     * 
     * @param function
     *            the function instance
     */
    @SuppressWarnings("unchecked")
    CapturingFactory(Object function) {
        super();
        this.typeLiteral = (TypeToken<R>) CapturingFactory.CACHE.get(getClass());
        requireNonNull(function); // should have already been checked by subclasses
        // analyze();

        if (this instanceof Factory0) {
            MethodHandle mh = CREATE0.bindTo(function).bindTo(rawType()); // (Supplier, Class)Object -> ()Object
            this.methodHandle = MethodHandleUtil.castReturnType(mh, rawType()); // ()Object -> ()R
            this.dependencies = List.of();
        } else if (this instanceof Factory1) {
            this.dependencies = FACTORY1_DEPENDENCY_CACHE.get(getClass());

            Class<?> ret = rawType();
            Class<?> param = dependencies.get(0).rawType();
            MethodHandle mh = CREATE1.bindTo(function).bindTo(ret); // (Function, Class, Object)Object -> (Object)Object
            this.methodHandle = MethodHandles.explicitCastArguments(mh, MethodType.methodType(ret, param)); // (Object)Object -> (T)R

        } else {
            this.dependencies = DEPENDENCY_CACHE2.get(getClass());

            Class<?> ret = rawType();
            Class<?> parem1 = dependencies.get(0).rawType();
            Class<?> parem2 = dependencies.get(1).rawType();
            MethodHandle mh = CREATE2.bindTo(function).bindTo(rawType()); // (Function, Class, Object, Object)Object -> (Object, Object)Object
            this.methodHandle = MethodHandles.explicitCastArguments(mh, MethodType.methodType(ret, parem1, parem2)); // (Object, Object)Object -> (T, U)R
        }
        this.factory = new CanonicalizedCapturingInternalFactory<>(typeLiteral, methodHandle, dependencies);

    }

    void analyze() {
        // Altsaa jeg ved ikke om vi spiller tiden ved ikke at afvente og se hvad der kommer med generiks

        Class<?> t = getClass();
        Class<?> n = t.getSuperclass();
        while (n.getSuperclass() != CapturingFactory.class) {
            n = n.getSuperclass();
        }
        Constructor<?>[] con = n.getDeclaredConstructors();
        if (con.length != 1) {
            throw new Error(n + " must declare a single constructor");
        }
        Constructor<?> c = con[0];
        if (c.getParameterCount() != 1) {
            throw new Error(n + " must declare a single constructor taking a single parameter");
        }

        Parameter p = c.getParameters()[0];

        Class<?> samType = p.getType();
        Method m = samType.getMethods()[0];

        // check SAM interface type

        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new Error(m + " must be accessible via MethodHandles.publicLookup()", e);
        }
        System.out.println(mh);
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<R> typeLiteral() {
        return typeLiteral;
    }

    /**
     * Supplies a value.
     * 
     * @param <T>
     *            the type of value supplied
     * @param supplier
     *            the supplier that supplies the actual value
     * @param expectedType
     *            the type we expect the supplier to return
     * @return the value that was supplied by the specified supplier
     * @throws FactoryException
     *             if the created value is null or not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create0(Supplier<? extends T> supplier, Class<?> expectedType) {
        T value = supplier.get();
        checkReturnValue(expectedType, value, supplier);
        return value;
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
    private static <T> T create1(Function<Object, ? extends T> function, Class<?> expectedType, Object object) {
        T value = function.apply(object);
        checkReturnValue(expectedType, value, function);
        return value;
    }

    InternalFactory<R> canonicalize() {
        throw new UnsupportedOperationException();
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
     * @param obj1
     *            the 1st argument to the function
     * @param obj2
     *            the 2nd argument to the function
     * @return the value that was supplied by the specified supplier
     * @throws FactoryException
     *             if the created value is null or not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create2(BiFunction<Object, Object, ? extends T> function, Class<?> expectedType, Object obj1, Object obj2) {
        T value = function.apply(obj1, obj2);
        checkReturnValue(expectedType, value, function);
        return value;
    }

    // Vi har 2 af dem, ind omkring Factory0 og en for ExtendsFactory0
    // Den for Factory0 skal have MethodHandlen... og noget omkring antallet af dependencies

    // Den kommer ind i InternalFactory
    record FactoryMetadata() {
        // find single Constructor... extract information about function type

        // must be a public type readable for anyone

        // create MH to access it

        // store it

        // and keep it for all furt
    }
}
