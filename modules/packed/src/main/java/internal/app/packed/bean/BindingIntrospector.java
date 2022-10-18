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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.container.Extension;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.operation.Variable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.ConstantBindingSetup;

/** Implementation of {@link BeanIntrospector.OnBinding}. */
public final class BindingIntrospector implements OnBinding {

    /** The extension that will manage the binding. */
    private final ExtensionSetup bindingExtension;

    /** The index of the binding. */
    private final int index;

    /** A specialized mirror for the binding. */
    @Nullable
    private Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation that will have a parameter bound. */
    private final OperationSetup operation;

    ///////////////

    @Nullable
    final Class<?> bindingHookClass;

    Variable variable;

    public BindingIntrospector(OperationSetup operation, int index, ExtensionSetup bindingExtension, @Nullable Class<?> bindingHookClass, Variable var) {
        this.operation = operation;
        this.index = index;
        this.bindingExtension = bindingExtension;
        this.variable = var;
        this.bindingHookClass = bindingHookClass;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new BeanAnnotationReader(variable().getAnnotations());
    }

    /** {@inheritDoc} */
    @Override
    public void bind(@Nullable Object obj) {
        checkIsBindable();
        if (obj == null) {
            if (variable.getType().isPrimitive()) {
                throw new IllegalArgumentException(variable + " is a primitive type and cannot be bound to null");
            }
        } else {

        }
        // Check assignable to
        // Create a bound thing
        //
        operation.bindings[index] = new ConstantBindingSetup(operation, index, obj, mirrorSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public void bindEmpty() {
        throw new UnsupportedOperationException();
    }

    public boolean isBound() {
        return operation.bindings[index] != null;
    }

    private void checkIsBindable() {
        if (isBound()) {
            throw new IllegalStateException("A binding has already been created");
        }
        // Eller er det introspectoren???
        if (bindingExtension.extensionRealm.isClosed()) {
            throw new IllegalStateException("Cannot create a binding after " + bindingExtension.extensionType + " has been closed");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> hookClass() {
        return bindingHookClass;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> invokingExtension() {
        return operation.invocationSite.operator.extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public void provide(MethodHandle methodHandle) {
        provide(Op.ofMethodHandle(methodHandle));
    }

    /** {@inheritDoc} */
    @Override
    public void provide(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public OnBinding runtimeBindable() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public OnBinding specializeMirror(Supplier<? extends BindingMirror> supplier) {
        checkIsBindable();
        this.mirrorSupplier = requireNonNull(supplier);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return variable;
    }
}