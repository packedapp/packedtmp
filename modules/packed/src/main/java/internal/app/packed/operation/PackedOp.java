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

import app.packed.binding.Variable;
import app.packed.operation.CapturingOp;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.IntermediateOp.BoundOp;
import internal.app.packed.operation.IntermediateOp.PeekingOp;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;

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

        MethodHandle newmh = MethodHandles.insertArguments(this.mhOperation, position, args);

        return new BoundOp<>(newType, newmh, this, position, args);
    }

    // Testing out claude
    public final Op<R> bindClaude(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        requireNonNull(additionalArguments, "additionalArguments is null");

        Objects.checkIndex(position, type.parameterCount());
        int len = 1 + additionalArguments.length;

        // Create new operation type
        Variable[] newVars = new Variable[type.parameterCount() - len];
        for (int i = 0; i < position; i++) {
            newVars[i] = type.parameter(i);
        }
        for (int i = position; i < newVars.length; i++) {
            newVars[i] = type.parameter(i + len);
        }
        OperationType newType = OperationType.of(type.returnVariable(), newVars);

        // Populate argument array
        Object[] args = new Object[len];
        args[0] = argument;
        System.arraycopy(additionalArguments, 0, args, 1, additionalArguments.length);

        // Create new MethodHandle
        MethodHandle newMh = MethodHandles.insertArguments(this.mhOperation, position, args);

        // Create and return the new BoundOp
        return new BoundOp<>(newType, newMh, this, position, args);
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    public abstract OperationSetup newOperationSetup(NewOS newos);

    /** {@inheritDoc} */
    @Override
    public final Op<R> peek(Consumer<? super R> action) {
        requireNonNull(action, "action is null");

        // Check if the operation returns void, in which case peeking is not supported
        if (type.returnRawType() == void.class) {
            throw new UnsupportedOperationException("This method is unsupported for Op's that have void return type, [ type = " + type + "]");
        }

        // Create a MethodHandle for the Consumer's accept method, bound to the provided (Consumer) action
        // (Consumer, Object)Object -> (Object)Object
        MethodHandle mh = PeekingOp.ACCEPT.bindTo(action);

        // Create a new MethodHandle that explicitly casts the arguments and return type
        // to match the original operation's return type
        MethodHandle consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(type().returnRawType(), type().returnRawType()));

        mh = MethodHandles.filterReturnValue(mh, consumer);

        // Ensure the final MethodHandle has the correct return type
        mh = mh.asType(mh.type().changeReturnType(type().returnRawType()));

        return new PeekingOp<>(this, mh);
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

    public record NewOS(BeanSetup bean, ExtensionSetup operator, PackedOperationTemplate template, @Nullable EmbeddedIntoOperation embeddedIn) {}
}
