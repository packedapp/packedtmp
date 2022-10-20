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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.operation.Op;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.InvocationSite;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;
import internal.app.packed.util.LookupUtil;

/** An implementation of the {@link Op#peek(Consumer)}} method. */
final class PeekableOp<R> extends PackedOp<R> {

    /** A method handle for {@link Function#apply(Object)}. */
    static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

    final PackedOp<?> delegate;

    PeekableOp(PackedOp<R> delegate, MethodHandle methodHandle) {
        super(delegate.type, methodHandle);
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public OperationSetup newOperationSetup(BeanSetup bean, OperationType type, InvocationSite invocationSite, @Nullable NestedBindingSetup nestedBinding) {
        return delegate.newOperationSetup(bean, type, invocationSite, nestedBinding);
    }

    @SuppressWarnings({ "unused", "unchecked" })
    private static Object accept(@SuppressWarnings("rawtypes") Consumer consumer, Object object) {
        consumer.accept(object);
        return object;
    }
}