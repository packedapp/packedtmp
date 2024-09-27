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
import java.util.function.Consumer;

import app.packed.build.BuildActor;
import app.packed.operation.OperationType;
import internal.app.packed.binding.BindingAccessor.FromConstant;
import internal.app.packed.binding.BindingSetup.ManualBindingSetup;
import internal.app.packed.util.LookupUtil;

/** An intermediate (non-terminal) op. */
abstract sealed class IntermediateOp<R> extends PackedOp<R> {

    /** The next op in the chain. */
    private final PackedOp<?> nextOp;

    /**
     * @param type
     * @param operation
     */
    private IntermediateOp(PackedOp<?> nextOp, OperationType type, MethodHandle operation) {
        super(type, operation);
        this.nextOp = requireNonNull(nextOp);
    }

    /** {@inheritDoc} */
    @Override
    public OperationSetup newOperationSetup(NewOS newOs) {
        return nextOp.newOperationSetup(newOs);
    }

    /** A op that binds 1 or more constants. */
    static final class BoundOp<R> extends IntermediateOp<R> {

        /** The arguments to insert. */
        final Object[] arguments;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        final int index;

        final int[] indexes = new int[0];

        BoundOp(OperationType type, MethodHandle methodHandle, PackedOp<R> delegate, int index, Object[] arguments) {
            super(delegate, type, methodHandle);
            this.index = index;
            this.arguments = arguments;
        }

        /** {@inheritDoc} */
        @Override
        public OperationSetup newOperationSetup(NewOS newOs) {
            OperationSetup os = super.newOperationSetup(newOs);

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                Object argument = arguments[i];
                os.bindings[index] = new ManualBindingSetup(os, index, BuildActor.application(), new FromConstant(argument.getClass(), argument));
            }
            return os;
        }
    }

    /** An implementation of the {@link Op#peek(Consumer)}} method. */
    static final class PeekingOp<R> extends IntermediateOp<R> {

        /** A method handle for {@link #accept(Consumer, Object)}. */
        static final MethodHandle ACCEPT = LookupUtil.findStaticOwn(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

        PeekingOp(PackedOp<R> delegate, MethodHandle methodHandle) {
            super(delegate, delegate.type, methodHandle);
        }

        @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }
    }
}
