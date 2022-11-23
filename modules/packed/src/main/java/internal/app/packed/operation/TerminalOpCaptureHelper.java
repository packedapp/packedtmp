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
package internal.app.packed.operation;

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

import app.packed.operation.CapturingOp;
import app.packed.operation.Op0;
import app.packed.operation.Op1;
import app.packed.operation.Op2;
import app.packed.operation.OperationType;
import app.packed.service.TypeToken;
import internal.app.packed.operation.TerminalOp.PackedCapturingOp;
import internal.app.packed.operation.binding.InternalDependency;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MethodHandleUtil;

/**
 *
 */
class TerminalOpCaptureHelper {

    /** A cache of extracted type variables from subclasses of this class. */
    static final ClassValue<TypeToken<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected TypeToken<?> computeValue(Class<?> type) {
            return TypeToken.fromTypeVariable((Class) type, CapturingOp.class, 0);
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
    static final ClassValue<List<InternalDependency>> OP1_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<InternalDependency> computeValue(Class<?> type) {
            return InternalDependency.fromTypeVariables((Class) type, Op1.class, 0);
        }
    };

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<List<InternalDependency>> OP2_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<InternalDependency> computeValue(Class<?> type) {
            return InternalDependency.fromTypeVariables((Class) type, Op2.class, 0, 1);
        }
    };

    void analyze() {
        // Altsaa jeg ved ikke om vi spiller tiden ved ikke at afvente og se hvad der kommer med generiks

        Class<?> t = getClass();
        Class<?> baseClass = t.getSuperclass();
        while (baseClass.getSuperclass() != CapturingOp.class) {
            baseClass = baseClass.getSuperclass();
        }

        // Maaske er det fint at smide en error?
        Constructor<?>[] con = baseClass.getDeclaredConstructors();
        if (con.length != 1) {
            throw new Error(baseClass + " must declare a single constructor");
        }
        Constructor<?> c = con[0];
        if (c.getParameterCount() != 1) {
            throw new Error(baseClass + " must declare a single constructor taking a single parameter");
        }

        Parameter p = c.getParameters()[0];

        Class<?> samType = p.getType();
        Method m = samType.getMethods()[0];

        // check SAM interface type

        // Hvorfor skal det vaere public
        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new Error(m + " must be accessible via MethodHandles.publicLookup()", e);
        }

        System.out.println(mh);
    }

    static void checkReturnValue(Class<?> expectedType, Object value, Object supplierOrFunction) {
        if (!expectedType.isInstance(value)) {
            String type = Supplier.class.isAssignableFrom(supplierOrFunction.getClass()) ? "supplier" : "function";
            if (value == null) {
                // NPE???
                throw new NullPointerException("The " + type + " '" + supplierOrFunction + "' must not return null");
            } else {
                // throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " +
                // instance.getClass());
                throw new ClassCastException("The \" + type + \" '" + supplierOrFunction + "' was expected to return instances of type "
                        + expectedType.getName() + " but returned a " + value.getClass().getName() + " instance");
            }
        }
    }

    /**
     * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
     * constructor in this (super) class.
     * 
     * @param function
     *            the function instance
     */
    @SuppressWarnings("unchecked")
    public static <R> PackedOp<R> create(Class<?> clazz, Object function) {
        requireNonNull(function, "function is null"); // should have already been checked by subclasses
        TypeToken<R> typeLiteral = (TypeToken<R>) TerminalOpCaptureHelper.CACHE.get(clazz);
        // analyze();

        final MethodHandle methodHandle;
        Class<?> rawType = typeLiteral.rawType();

        if (Op0.class.isAssignableFrom(clazz)) {
            MethodHandle mh = CREATE0.bindTo(function).bindTo(rawType); // (Supplier, Class)Object -> ()Object
            methodHandle = MethodHandleUtil.castReturnType(mh, rawType); // ()Object -> ()R
        } else if (Op1.class.isAssignableFrom(clazz)) {
            List<InternalDependency> dependencies = OP1_CACHE.get(clazz);

            Class<?> param = dependencies.get(0).rawType();
            MethodHandle mh = CREATE1.bindTo(function).bindTo(rawType); // (Function, Class, Object)Object -> (Object)Object
            methodHandle = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawType, param)); // (Object)Object -> (T)R

        } else {
            List<InternalDependency> dependencies = OP2_CACHE.get(clazz);

            Class<?> parem1 = dependencies.get(0).rawType();
            Class<?> parem2 = dependencies.get(1).rawType();
            MethodHandle mh = CREATE2.bindTo(function).bindTo(rawType); // (Function, Class, Object, Object)Object -> (Object, Object)Object
            methodHandle = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawType, parem1, parem2)); // (Object, Object)Object -> (T, U)R
        }
        OperationType type = OperationType.ofMethodType(methodHandle.type()); // TODO fix
        return new PackedCapturingOp<>(type, methodHandle, function.getClass());

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
     * @throws NullPointerException
     *             if the created value is null
     * @throws ClassCastException
     *             if the created value is not assignable to the raw type of the factory
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
     * @throws NullPointerException
     *             if the created value is null
     * @throws ClassCastException
     *             if the created value is not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create1(Function<Object, ? extends T> function, Class<?> expectedType, Object object) {
        T value = function.apply(object);
        checkReturnValue(expectedType, value, function);
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
     * @param obj1
     *            the 1st argument to the function
     * @param obj2
     *            the 2nd argument to the function
     * @return the value that was supplied by the specified supplier
     * @throws NullPointerException
     *             if the created value is null
     * @throws ClassCastException
     *             if the created value is not assignable to the raw type of the factory
     */
    @SuppressWarnings("unused") // only invoked via #CREATE
    private static <T> T create2(BiFunction<Object, Object, ? extends T> function, Class<?> expectedType, Object obj1, Object obj2) {
        T value = function.apply(obj1, obj2);
        checkReturnValue(expectedType, value, function);
        return value;
    }

//    // Vi har 2 af dem, ind omkring Factory0 og en for ExtendsFactory0
//    // Den for Factory0 skal have MethodHandlen... og noget omkring antallet af dependencies
//
//    // Den kommer ind i InternalFactory
//    record FactoryMetadata() {
//        // find single Constructor... extract information about function type
//
//        // must be a public type readable for anyone
//
//        // create MH to access it
//
//        // store it
//
//        // and keep it for all furt
//    }
}
