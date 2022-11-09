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

import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.container.Extension;
import app.packed.container.User;
import app.packed.framework.Nullable;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.operation.Variable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.binding.BindingTarget;
import internal.app.packed.operation.binding.BindingTarget.BindingHookTarget;
import internal.app.packed.operation.binding.ConstantBindingSetup;
import internal.app.packed.operation.binding.OperationalBindingSetup;

/** Implementation of {@link BeanIntrospector.OnBinding}. */
public final class IntrospectedBeanBinding implements OnBinding {

    /** The extension that manages the binding. */
    private final ExtensionSetup bindingExtension;

    @Nullable
    final Class<?> bindingHookClass;

    /** The index of the binding. */
    private final int index;

    /** A specialized mirror for the binding. */
    @Nullable
    private Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation that will have a parameter bound. */
    public final OperationSetup operation;

    Variable variable;

    final IntrospectedBean iBean;

    public IntrospectedBeanBinding(IntrospectedBean iBean, OperationSetup operation, int index, ExtensionSetup bindingExtension,
            @Nullable Class<?> bindingHookClass, Variable var) {
        this.operation = requireNonNull(operation);
        this.iBean = iBean;
        this.index = index;
        this.bindingExtension = requireNonNull(bindingExtension);
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
            if (!variable.getType().isAssignableFrom(obj.getClass())) {
                // Maybe throw an InternalExtensionException?
                // As it is the responsibility of the extension
                // to throw a more fitting exception
                throw new ClassCastException("? of type " + variable.getType() + " cannot be bound to object of type " + obj.getClass());
            }

        }
        // Check assignable to
        // Create a bound thing
        //
        BindingTarget bt = new BindingHookTarget();
        operation.bindings[index] = new ConstantBindingSetup(operation, index, User.extension(bindingExtension.extensionType), bt, Object.class, obj);
    }

    /** {@inheritDoc} */
    @Override
    public void bindEmpty() {
        throw new UnsupportedOperationException();
    }

    private void checkIsBindable() {
        if (isBound()) {
            throw new IllegalStateException("A binding has already been created");
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
        return operation.operator.extensionType;
    }

    /** {@return whether or not a binding has already been created.} */
    public boolean isBound() {
        return operation.bindings[index] != null;
    }

    /** {@inheritDoc} */
    @Override
    public void provide(Op<?> op) {
        PackedOp<?> pop = PackedOp.crack(op);
       
        BindingHookTarget bht = new BindingHookTarget();
        OperationSetup os = pop.newOperationSetup(operation.bean, pop.type(), bindingExtension);
        OperationalBindingSetup obs = new OperationalBindingSetup(os, index, User.application(), bht, os);
        if (variable.getType() != os.target.methodHandle.type().returnType()) {
//            System.out.println("FixIt");
        }
        iBean.unBoundOperations.add(os);
        operation.bindings[index] = obs;
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
