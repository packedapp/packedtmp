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
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector.OnBindingHook;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.operation.binding.PackedOnBindingHook;

/** Implementation of {@link OperationHandle}. */
public record PackedOperationHandle(BeanOperationSetup os) implements OperationHandle {

    /** {@inheritDoc} */
    @Override
    public List<OnBindingHook> bindings() {
        OnBindingHook[] hooks = new OnBindingHook[os.type.parameterCount()];
        for (int i = 0; i < hooks.length; i++) {
            hooks[i] = new PackedOnBindingHook(os, i);
        }
        return List.of(hooks);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildInvoker() {
        // Hav en version der tager en ExtensionBeanConfiguration eller bring back ExtensionContext
        
        if (os.isComputed) {
            throw new IllegalStateException("This method can only be called once");
        }
        
        os.isComputed = true;
        // application.checkIsComputable
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType invocationType() {
        return os.invocationType;
    }

    /** {@inheritDoc} */
    public OperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier) {
        if (os.isComputed) {
            throw new IllegalStateException("Cannot set a mirror after an invoker has been computed");
        }
        os.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public OperationType type() {
        return os.type;
    }
}
