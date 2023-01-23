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

import java.util.function.Consumer;

import app.packed.framework.Nullable;
import internal.app.packed.operation.CapturingOpHelper;
import internal.app.packed.operation.PackedOp;

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

    /** The op that all calls delegate to. */
    private final PackedOp<R> op;

    /**
     * Create a new operation.
     * 
     * @param function
     *            the function instance
     */
    protected CapturingOp(Object function) {
        this.op = CapturingOpHelper.capture(getClass(), function);
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

    public final Op<R> canonicalize() {
        return op;
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
