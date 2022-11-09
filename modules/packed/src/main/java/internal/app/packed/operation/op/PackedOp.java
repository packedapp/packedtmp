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
package internal.app.packed.operation.op;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.function.Consumer;

import app.packed.framework.Nullable;
import app.packed.operation.CapturingOp;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;
import internal.app.packed.operation.op.IntermediateOp.BoundOp;
import internal.app.packed.operation.op.IntermediateOp.PeekableOp;
import internal.app.packed.util.MethodHandleUtil;

/** The internal implementation of Op. */
@SuppressWarnings("rawtypes")
public abstract sealed class PackedOp<R> implements Op<R> permits IntermediateOp, TerminalOp {

    /** The method handle. */
    public final MethodHandle operation;

    /** The operation type of this op. */
    public final OperationType type;

    PackedOp(OperationType type, MethodHandle operation) {
        this.type = requireNonNull(type, "type is null");
        this.operation = requireNonNull(operation);
    }

    /** {@inheritDoc} */
    public final Op<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        requireNonNull(additionalArguments, "additionalArguments is null");

        Objects.checkIndex(position, type.parameterCount());
        int len = 1 + additionalArguments.length;
        int newLen = type.parameterCount() - len;
        if (newLen < 0) {
            throw new IllegalArgumentException(
                    "Cannot specify more than " + (len - position) + " arguments for position = " + position + ", but arguments array was size " + len);
        }

        // Create new operation type
        Variable[] vars = new Variable[newLen];
        for (int i = 0; i < position; i++) {
            vars[i] = type.parameter(i);
        }
        for (int i = position; i < vars.length; i++) {
            vars[i] = type.parameter(i + len);
        }
        OperationType newType = OperationType.of(type.returnVariable(), vars);

        // Populate argument array
        Object[] args = new Object[len];
        args[0] = argument;
        for (int i = 0; i < additionalArguments.length; i++) {
            args[i + 1] = additionalArguments[i];
        }
        // TODO check types...

        MethodHandle newmh = MethodHandles.insertArguments(this.operation, position, args);

        return new BoundOp<>(newType, newmh, this, position, args);
    }

    /** {@inheritDoc} */
    public final Op<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    public abstract OperationSetup newOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, @Nullable NestedBindingSetup nestedBinding);

    /** {@inheritDoc} */
    public final Op<R> peek(Consumer<? super R> action) {
        requireNonNull(action, "action is null");
        if (type.returnType() == void.class) {
            throw new UnsupportedOperationException("This method cannot be used on Op's that have void return type, [ type = " + type + "]");
        }
        MethodHandle mh = PeekableOp.ACCEPT.bindTo(action);
        MethodHandle consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(type().returnType(), type().returnType()));

        MethodHandle mhNew = MethodHandles.filterReturnValue(mh, consumer);
        mhNew = MethodHandleUtil.castReturnType(mhNew, type().returnType());
        return new PeekableOp<>(this, mhNew);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationType type() {
        return type;
    }

    public static <R> PackedOp<R> capture(Class<?> clazz, Object function) {
        return CapturingOpHelper.create(clazz, function);
    }

    public static <R> PackedOp<R> crack(Op<R> op) {
        requireNonNull(op, "op is null");
        if (op instanceof PackedOp<R> pop) {
            return pop;
        } else {
            return (PackedOp<R>) ((CapturingOp<R>) op).canonicalize();
        }
    }
}
