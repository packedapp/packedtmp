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

import java.util.function.Consumer;

import app.packed.base.Nullable;
import internal.app.packed.operation.op.PackageCapturingOpHelper;
import internal.app.packed.operation.op.PackedOp;

/**
 * A abstract op that captures the type an annotated return type and annotated type apra
 */
public abstract non-sealed class CapturingOp<R> implements Op<R> {

    /** The op that we delegate everything to. */
    private final PackedOp<R> op;

    /**
     * Used by the various FactoryN constructor, because we cannot call {@link Object#getClass()} before calling a
     * constructor in this (super) class.
     * 
     * @param function
     *            the function instance
     */
    CapturingOp(Object function) {
        requireNonNull(function, "function is null"); // should have already been checked by subclasses
        op = PackageCapturingOpHelper.create(getClass(), function);
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

    PackedOp<R> canonicalize() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final Op<R> peek(Consumer<? super R> action) {
        return op.peek(action);
    }

    /** {@inheritDoc} */
    public final OperationType type() {
        return op.type();
    }
}
