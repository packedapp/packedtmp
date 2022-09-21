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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.operation.CapturingOp;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MethodHandleUtil;

/**
 * Internal implementation of Op.
 */
public abstract non-sealed class PackedOp<R> implements Op<R> {

    /** A var handle that can update the {@link #container()} field in this class. */
    private static final VarHandle VH_CAPTURING_OP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), CapturingOp.class, "op", PackedOp.class);

    /** The operation type of this op. */
    private final OperationType type;

    PackedOp(OperationType type) {
        this.type = requireNonNull(type, "type is null");
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

        return new BoundOp<>(newType, this, position, args);
    }

    /** {@inheritDoc} */
    public final Op<R> bind(@Nullable Object argument) {
        return bind(0, argument);
    }

    /** {@inheritDoc} */
    public final Op<R> peek(Consumer<? super R> action) {
        return new PeekableOp<>(this, action);
    }

    public abstract MethodHandle toMethodHandle(Lookup lookup);

    /** {@inheritDoc} */
    @Override
    public final OperationType type() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <R> PackedOp<R> crack(Op<R> op) {
        requireNonNull(op, "op is null");
        if (op instanceof PackedOp<R> pop) {
            return pop;
        } else {
            // if capturingop had a canonicalizde we could just call that and get an IFa
            Object result = VH_CAPTURING_OP.get(op);
            return (PackedOp<R>) result;
        }
    }

    /** Adapts an existing op with a new operation type */
    static final class AdaptedOp<R> extends PackedOp<R> {

        /** The previous op. */
        private final PackedOp<R> delegate;

        /**
         * @param type
         */
        AdaptedOp(OperationType type, PackedOp<R> delegate) {
            super(type);
            this.delegate = delegate;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            return delegate.toMethodHandle(lookup);
        }
    }

    /** A binding op. */
    static final class BoundOp<R> extends PackedOp<R> {

        /** The arguments to insert. */
        private final Object[] arguments;

        /** The previous op. */
        private final PackedOp<R> delegate;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final int index;

        BoundOp(OperationType type, PackedOp<R> delegate, int index, Object[] arguments) {
            super(type);
            this.index = index;
            this.delegate = delegate;
            this.arguments = arguments;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = delegate.toMethodHandle(lookup);
            return MethodHandles.insertArguments(mh, index, arguments);
        }
    }

    /** An op taking no the same value every time, used by {@link Op#ofInstance(Object)}. */
    public static final class ConstantOp<R> extends PackedOp<R> {

        /** A precomputed constant method handle. */
        private final MethodHandle methodHandle;

        public ConstantOp(R instance) {
            super(OperationType.of(instance.getClass()));
            this.methodHandle = MethodHandles.constant(instance.getClass(), instance);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup ignore) {
            return methodHandle;
        }
    }

    // Should just create eagerly create a MH and use Op.ofMethodHandle
    static final class PackedCapturingOp<R> extends PackedOp<R> {

        public final MethodHandle methodHandle;

        /**
         * @param typeLiteralOrKey
         */
        PackedCapturingOp(OperationType type, MethodHandle methodHandle) {
            super(type);
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            return methodHandle;
        }
    }

    /** An implementation of the {@link Op#peek(Consumer)}} method. */
    static final class PeekableOp<R> extends PackedOp<R> {

        /** A method handle for {@link Function#apply(Object)}. */
        private static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

        /** The method handle that was unreflected. */
        private final MethodHandle consumer;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final PackedOp<R> delegate;

        PeekableOp(PackedOp<R> delegate, Consumer<? super R> action) {
            super(delegate.type);
            MethodHandle mh = ACCEPT.bindTo(requireNonNull(action, "action is null"));
            this.consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(type().returnType(), type().returnType()));
            this.delegate = delegate;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = delegate.toMethodHandle(lookup);
            mh = MethodHandles.filterReturnValue(mh, consumer);
            return MethodHandleUtil.castReturnType(mh, type().returnType());
        }

        @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }
    }
}
