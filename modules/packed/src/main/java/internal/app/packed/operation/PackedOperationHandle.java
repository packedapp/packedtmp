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
import java.util.function.Supplier;

import app.packed.operation.OperationMirror;
import app.packed.operation.invokesandbox.OperationHandle;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ExtensionBeanSetup;

/** Implementation of {@link OperationHandle}. */
public final class PackedOperationHandle implements OperationHandle {

    /** The bean the operation is a part of. */
    final BeanSetup bean;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The bean that invokes the operation. */
    final ExtensionBeanSetup operatorBean;

    /** The target of the operation. Typically a bean member, a function or a plain MethodHandle. */
    final PackedOperationTarget target;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    public PackedOperationHandle(BeanSetup bean, PackedOperationTarget target, ExtensionBeanSetup operatorBean) {
        this.bean = bean;
        this.target = target;
        this.operatorBean = operatorBean;
    }

    public void build() {
        OperationSetup os = new OperationSetup(this);
        bean.addOperation(os);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle computeMethodHandle() {
        if (isComputed) {
            throw new IllegalStateException("This method can only be called once");
        }
        isComputed = true;
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public OperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier) {
        if (isComputed) {
            throw new IllegalStateException("Cannot set a mirror after an invoker has been computed");
        }
        this.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }
}
