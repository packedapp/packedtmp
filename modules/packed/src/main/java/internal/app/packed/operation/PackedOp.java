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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.binding.Variable;
import app.packed.operation.CapturingOp;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.invoke.OpSupport;
import internal.app.packed.operation.IntermediateOp.BoundOp;
import internal.app.packed.operation.IntermediateOp.PeekingOp;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import internal.app.packed.util.types.ClassUtil;

/** The internal implementation of Op. */
public abstract sealed class PackedOp<R> implements Op<R> permits IntermediateOp, TerminalOp {

    /** The method handle. */
    public final MethodHandle mhOperation;

    /** The type of this op. */
    public final OperationType type;

    PackedOp(OperationType type, MethodHandle operation) {
        this.type = requireNonNull(type, "type is null");
        this.mhOperation = requireNonNull(operation);
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        // Validate input parameters
        requireNonNull(additionalArguments, "additionalArguments is null");
        Objects.checkIndex(position, type.parameterCount());

        // Calculate new operation length and validate argument count
        int argumentsLength = 1 + additionalArguments.length;
        int newParameterCount = type.parameterCount() - argumentsLength;

        if (newParameterCount < 0) {
            throw new IllegalArgumentException("Cannot specify more than " + (argumentsLength - position) + " arguments for position = " + position
                    + ", but arguments array was size " + argumentsLength);
        }

        // Type check the arguments against parameter types
        validateArgument(argument, type.parameter(position), position);
        for (int i = 0; i < additionalArguments.length; i++) {
            validateArgument(additionalArguments[i], type.parameter(position + i + 1), position + i + 1);
        }

        // Create new operation type with updated parameters
        Variable[] newParameters = new Variable[newParameterCount];

        // Copy parameters before the binding position
        for (int i = 0; i < position; i++) {
            newParameters[i] = type.parameter(i);
        }

        // Copy parameters after the bound arguments
        for (int i = position; i < newParameters.length; i++) {
            newParameters[i] = type.parameter(i + argumentsLength);
        }

        OperationType newType = OperationType.of(type.returnVariable(), newParameters);

        // Prepare the arguments array for binding
        Object[] boundArgs = new Object[argumentsLength];
        boundArgs[0] = argument;
        System.arraycopy(additionalArguments, 0, boundArgs, 1, additionalArguments.length);

        // Create new method handle with bound arguments
        MethodHandle newMethodHandle = MethodHandles.insertArguments(this.mhOperation, position, boundArgs);

        // Create and return new bound operation
        return new BoundOp<>(newType, newMethodHandle, this, position, boundArgs);
    }

    /**
     * Validates that an argument matches the expected parameter type.
     *
     * @param argument
     *            The argument to validate
     * @param parameter
     *            The parameter variable containing type information
     * @param position
     *            The position of the parameter for error reporting
     * @throws IllegalArgumentException
     *             if the argument doesn't match the parameter type
     */
    private void validateArgument(@Nullable Object argument, Variable parameter, int position) {
        Class<?> parameterType = parameter.rawType();

        // Handle null argument case
        if (argument == null) {
            if (parameter.rawType().isPrimitive()) {
                throw new IllegalArgumentException(
                        String.format("Cannot bind null to primitive parameter at position %d of type %s", position, parameterType.getSimpleName()));
            }

            return;
        }

        // Handle primitive types
        if (parameterType.isPrimitive()) {
            Class<?> boxedType = ClassUtil.box(parameterType);
            if (!boxedType.isInstance(argument)) {
                throw new IllegalArgumentException(String.format("Type mismatch at position %d: expected %s, but got %s", position,
                        parameterType.getSimpleName(), argument.getClass().getSimpleName()));
            }
            return;
        }

        // Handle reference types
        if (!parameterType.isInstance(argument)) {
            throw new IllegalArgumentException(
                    String.format("Type mismatch at position %d: cannot convert from %s to %s", position, argument.getClass().getName(), parameter.toString()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    /**
     * Returns the composed method handle for this operation.
     * Intermediate ops override this to compose their transformations.
     */
    public MethodHandle getComposedMethodHandle() {
        return mhOperation;
    }

    /**
     * Creates the operation setup.
     */
    public abstract OperationSetup newOperationSetup(NewOperation newOs);

    /** {@inheritDoc} */
    @Override
    public final Op<R> peek(Consumer<? super R> action) {
        requireNonNull(action, "action is null");

        // Check if the operation returns void, in which case peeking is not supported
        if (type.returnRawType() == void.class) {
            throw new UnsupportedOperationException("This method is unsupported for Op's that have void return type, [ type = " + type + "]");
        }

        // Create a MethodHandle for the Consumer's accept method, bound to the provided (Consumer) action
        // ACCEPT_CONSUMER_OBJECT signature: (Consumer, Object) -> Object
        // After binding: (Object) -> Object - calls consumer.accept(obj) and returns obj
        MethodHandle peekFilter = OpSupport.ACCEPT_CONSUMER_OBJECT.bindTo(action);

        // Cast to the actual return type for proper type safety
        Class<?> returnType = type.returnRawType();
        peekFilter = MethodHandles.explicitCastArguments(peekFilter,
                MethodType.methodType(returnType, returnType));

        return new PeekingOp<>(this, peekFilter);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationType type() {
        return type;
    }

    public static <R> PackedOp<R> crack(Op<R> op) {
        requireNonNull(op, "op is null");
        if (op instanceof PackedOp<R> pop) {
            return pop;
        } else {
            return (PackedOp<R>) ((CapturingOp<R>) op).canonicalize();
        }
    }

    /** Record containing all the information needed to create an operation setup. */
    public record NewOperation(BeanSetup bean, ExtensionSetup operator, PackedOperationTemplate template,
            Function<? super OperationInstaller, OperationHandle<?>> newHandle, @Nullable EmbeddedIntoOperation embeddedIn,
            MethodHandle composedMH) {}
}
