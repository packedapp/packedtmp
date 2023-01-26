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

import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanInstallationException;
import app.packed.bindings.BindableVariable;
import app.packed.bindings.Variable;
import app.packed.bindings.mirror.BindingMirror;
import app.packed.container.Realm;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.AnnotationList;
import app.packed.framework.Nullable;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.binding.BindingProvider.FromArgument;
import internal.app.packed.binding.BindingProvider.FromCodeGenerated;
import internal.app.packed.binding.BindingProvider.FromConstant;
import internal.app.packed.binding.BindingProvider.FromOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.binding.BindingSetup.HookBindingSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;
import internal.app.packed.operation.PackedOp;

/** Implementation of {@link BindableVariable}. */
public class PackedBindableVariable implements BindableVariable {

    /** Whether or not allow binding of static fields. */
    private boolean allowStaticFieldBinding;

    /** The extension that manages the binding. */
    private final ExtensionSetup bindingExtension;

    /** The index of the binding into the operation. */
    private final int index;

    /** A specialized mirror for the binding. */
    @Nullable
    private Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation that will have a parameter bound. */
    public final OperationSetup operation;

    final BeanScanner scanner;

    Variable variable;

    public PackedBindableVariable(BeanScanner scanner, OperationSetup operation, int index, ExtensionSetup bindingExtension, Variable var) {
        this.operation = requireNonNull(operation);
        this.scanner = scanner;
        this.index = index;
        this.bindingExtension = requireNonNull(bindingExtension);
        this.variable = var;
    }

    /** {@inheritDoc} */
    @Override
    public BindableVariable allowStaticFieldBinding() {
        allowStaticFieldBinding = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationList annotations() {
        return new PackedAnnotationList(variable().getAnnotations());
    }

    /** {@inheritDoc} */
    @Override
    public List<Class<?>> availableInvocationArguments() {
        return operation.template.invocationType().parameterList();
    }

    /** {@inheritDoc} */
    @Override
    public void bindConstant(@Nullable Object obj) {
        checkBeforeBinding();
        if (obj == null) {
            if (variable.getRawType().isPrimitive()) {
                throw new IllegalArgumentException(variable + " is a primitive type and cannot be bound to null");
            }
        } else {
            // I think we want to have the hook type
            // And throw a better error msg

            if (!variable.getRawType().isAssignableFrom(obj.getClass())) {
                // Maybe throw an InternalExtensionException?
                // As it is the responsibility of the extension
                // to throw a more fitting exception
                throw new ClassCastException("? of type " + variable.getRawType() + " cannot be bound to object of type " + obj.getClass());
            }

        }
        // Check assignable to
        // Create a bound thing

        BindingSetup bs = new HookBindingSetup(operation, index, Realm.extension(bindingExtension.extensionType));
        bs.provider = new FromConstant(Object.class, obj);

        operation.bindings[index] = bs;
    }

    /** {@inheritDoc} */
    @Override
    public void bindGeneratedConstant(Supplier<?> consumer) {
        checkBeforeBinding();
        BindingSetup bs = new HookBindingSetup(operation, index, Realm.application());
        bs.provider = new FromCodeGenerated(consumer);
        operation.bindings[index] = bs;
    }

    /** {@inheritDoc} */
    @Override
    public void bindInvocationArgument(Class<?> argumentType) {
        requireNonNull(argumentType, "argumentType is null");
        MethodType mt = operation.template.invocationType();
        for (int i = 0; i < mt.parameterCount(); i++) {
            if (argumentType == mt.parameterType(i)) {
                bindInvocationArgument(i);
            }
        }
        throw new InternalExtensionException("oops");
    }

    /** {@inheritDoc} */
    @Override
    public void bindInvocationArgument(int argumentIndex) {
        checkBeforeBinding();
        BindingSetup bs = new HookBindingSetup(operation, index, Realm.application());
        bs.provider = new FromArgument(argumentIndex);
        operation.bindings[index] = bs;
    }

    /** {@inheritDoc} */
    @Override
    public void bindOp(Op<?> op) {
        checkBeforeBinding();
        PackedOp<?> pop = PackedOp.crack(op);

        OperationTemplate ot = operation.template.withReturnType(pop.type().returnType());
        OperationSetup os = pop.newOperationSetup(operation.bean, bindingExtension, ot);

        os.parent = operation;

        BindingSetup bs = new HookBindingSetup(operation, index, Realm.application());
        bs.provider = new FromOperation(os);

        // We resolve the operation immediately
        scanner.resolveNow(os);

        operation.bindings[index] = bs;
    }

    private void checkNotBound() {
        if (isBound()) {
            throw new IllegalStateException("A binding has already been created");
        }
    }

    private void checkBeforeBinding() {
        checkNotBound();
        if (operation instanceof FieldOperationSetup fos && Modifier.isStatic(fos.modifiers()) && !allowStaticFieldBinding) {
            throw new BeanInstallationException("Static field binding is not supported for");
        }
        // TODO check if field static
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> invokedBy() {
        return operation.operator.extensionType;
    }

    /** {@return whether or not a binding has already been created.} */
    public boolean isBound() {
        return operation.bindings[index] != null;
    }

    /** {@inheritDoc} */
    @Override
    public BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier) {
        checkNotBound();
        this.mirrorSupplier = requireNonNull(supplier);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return variable;
    }
}
