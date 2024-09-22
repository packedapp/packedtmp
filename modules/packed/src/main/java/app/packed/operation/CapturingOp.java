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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import app.packed.binding.Variable;
import app.packed.util.Nullable;
import internal.app.packed.binding.InternalDependency;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.SamType;
import internal.app.packed.operation.TerminalOp.FunctionInvocationOp;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 * A abstract op that captures the type an annotated return type and annotated type apra
 * <p>
 * Currently, this class cannot be extended outside of this package. This will likely change in the future.
 *
 * @see Op0
 * @see Op1
 * @see Op2
 */
public abstract non-sealed class CapturingOp<R> implements Op<R> {

    private static final ClassValue<Base> BASE = new ClassValue<>() {

        @Override
        protected Base computeValue(Class<?> type) {

            // Maaske er det fint at smide en error?
            Constructor<?>[] con = type.getDeclaredConstructors();
            if (con.length != 1) {
                throw new Error(type + " must declare a single constructor");
            }
            Constructor<?> c = con[0];
            if (c.getParameterCount() != 1) {
                throw new Error(type + " must declare a single constructor with a single parameter taking a function interface");
            }

            Parameter p = c.getParameters()[0];

            Class<?> functionalInterface = p.getType();

            SamType st = SamType.of(functionalInterface);

            TypeVariableExtractor tve = TypeVariableExtractor.of(type);

            return new Base(st, tve);
        }
    };

    private static final ClassValue<Top> TOP = new ClassValue<>() {

        @Override
        protected Top computeValue(Class<?> type) {
            Class<?> baseClass = type.getSuperclass();
            while (baseClass.getSuperclass() != CapturingOp.class) {
                baseClass = baseClass.getSuperclass();
            }
            Base b = BASE.get(baseClass);

            Variable[] types = b.tve.extractAllVariables(type, IllegalArgumentException::new);
            Variable last = types[types.length - 1];

            OperationType ot = OperationType.of(last, Arrays.copyOf(types, types.length - 1));

            return new Top(b, ot);
        }
    };

    /** The op that all calls delegate to. */
    private final PackedOp<R> op;

    /**
     * Create a new operation.
     *
     * @param function
     *            the function instance
     */
    CapturingOp(Object function) {
        requireNonNull(function, "function is null"); // should have already been checked by subclasses
        Top top = TOP.get(getClass());
        this.op = new FunctionInvocationOp<>(top.ot, top.create(function), top.base.samType, function.getClass().getMethods()[0]);
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        return op.bind(position, argument, additionalArguments);
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> bind(@Nullable Object argument) {
        return op.bind(argument);
    }

    /**
     * Returns a canonicalized version of this op.
     * <p>
     * Bla bla
     *
     * @return
     */
    public final Op<R> canonicalize() {
        return op;
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> peek(Consumer<? super R> action) {
        return op.peek(action);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationType type() {
        return op.type();
    }

    private record Base(SamType samType, TypeVariableExtractor tve) {}

    private static class Top {
        final Base base;
        @SuppressWarnings("unused")
        final List<InternalDependency> deps;

        final OperationType ot;

        Top(Base base, OperationType ot) {
            this.base = base;
            this.ot = ot;
            this.deps = InternalDependency.fromOperationType(ot);

        }

        // MethodHandle mh = null;

        MethodHandle create(Object function) {
            MethodHandle mh = base.samType.methodHandle().bindTo(function);
            return mh.asType(ot.toMethodType());

            // Think we need to validate it

//            if (!expectedType.isInstance(value)) {
//                String type = Supplier.class.isAssignableFrom(supplierOrFunction.getClass()) ? "supplier" : "function";
//                if (value == null) {
//                    // NPE???
//                    throw new NullPointerException("The " + type + " '" + supplierOrFunction + "' must not return null");
//                } else {
//                    // throw new ClassCastException("Expected factory to produce an instance of " + format(type) + " but was " +
//                    // instance.getClass());
//                    throw new ClassCastException("The \" + type + \" '" + supplierOrFunction + "' was expected to return instances of type "
//                            + expectedType.getName() + " but returned a " + value.getClass().getName() + " instance");
//                }
//            }
        }
    }
}
