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
package internal.app.packed.bean.scanning;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.binding.BindingMirror;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.context.Context;
import app.packed.context.UnavilableContextException;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedVariableCache;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingProvider.FromComputedConstant;
import internal.app.packed.binding.BindingProvider.FromConstant;
import internal.app.packed.binding.BindingProvider.FromInvocationArgument;
import internal.app.packed.binding.BindingProvider.FromEmbeddedOperation;
import internal.app.packed.binding.BindingSetup.HookBindingSetup;
import internal.app.packed.binding.SuppliedBindingKind;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOperation;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link BindableVariable}. */
public final class IntrospectorOnVariable extends IntrospectorOn implements OnVariable {

    /** Whether or not allow binding of static fields. */
    private boolean allowStaticFieldBinding;

    /** The extension that will manage the binding. */
    private final ExtensionSetup bindingExtension;

    /** The index of the binding into the operation. */
    private final int index;

    /** A specialized mirror for the binding. */
    @Nullable
    private Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation that will have a parameter bound. */
    public final OperationSetup operation;

    /** The bean scanner, used for resolving more nested operations. */
    private final BeanScanner scanner;

    /** The variable to bind. */
    private final Variable variable;

    public IntrospectorOnVariable(BeanScanner scanner, OperationSetup operation, int index, ExtensionSetup bindingExtension, Variable variable) {
        this.operation = requireNonNull(operation);
        this.scanner = requireNonNull(scanner);
        this.index = index;
        this.bindingExtension = requireNonNull(bindingExtension);
        this.variable = requireNonNull(variable);
    }

    /** {@inheritDoc} */
    @Override
    public OnVariable allowStaticFieldBinding() {
        checkNotBound();
        allowStaticFieldBinding = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationList annotations() {
        return variable().annotations();
    }

    /** {@inheritDoc} */
    @Override
    public List<Class<?>> availableInvocationArguments() {
        return operation.template.invocationType().parameterList();
    }

    /** {@inheritDoc} */
    @Override
    public BeanSetup bean() {
        return scanner.bean;
    }

    private void bind(BindingProvider accessor) {
        assert (operation.bindings[index] == null);
        operation.bindings[index] = new HookBindingSetup(operation, index, bindingExtension.authority(), accessor);
    }

    /** {@inheritDoc} */
    @Override
    public IntrospectorOnVariable bindComputedConstant(Supplier<?> supplier) {
        checkBeforeBind();
        // We can't really do any form of type checks until we call the supplier
        bind(new FromComputedConstant(supplier, SuppliedBindingKind.CODEGEN));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public IntrospectorOnVariable bindConstant(@Nullable Object obj) {
        checkBeforeBind();
        if (obj == null) {
            if (variable.rawType().isPrimitive()) {
                throw new IllegalArgumentException(variable + " is a primitive type and cannot be bound to null");
            }
        } else {
            // I think we want to have the hook type
            // And throw a better error msg

            if (!variable.rawType().isAssignableFrom(obj.getClass())) {
                // Maybe throw an InternalExtensionException?
                // As it is the responsibility of the extension
                // to throw a more fitting exception
                throw new ClassCastException("? of type " + variable.rawType() + " cannot be bound to object of type " + obj.getClass());
            }

        }
        bind(new FromConstant(Object.class, obj));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public IntrospectorOnVariable bindContext(Class<? extends Context<?>> context) {
        if (context != ExtensionContext.class) {
            ContextSetup findContext = operation.findContext(context);
            if (findContext == null) {
                throw new UnavilableContextException("oops " + context);
            }
        }
        MethodType mt = operation.template.invocationType();
        int indexOf = mt.parameterList().indexOf(context);
        // TODO fix. We need to look up the
        bindInvocationArgument(indexOf);
        return this;
    }

    @Override
    public IntrospectorOnVariable bindInvocationArgument(int argumentIndex) {
        checkBeforeBind();
        if (operation.installedByExtension != bindingExtension) {
            throw new UnsupportedOperationException("For binding " + variable);
        }
        checkIndex(argumentIndex, operation.template.invocationType().parameterCount());
        // TODO check type

        bind(new FromInvocationArgument(argumentIndex));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public IntrospectorOnVariable bindOp(Op<?> op) {
        checkBeforeBind();
        PackedOp<?> pop = PackedOp.crack(op);

        // Nested operation get the same arguments as this operation, but with op return type
        PackedOperationTemplate template = operation.template.withReturnType(pop.type().returnRawType());

        // Create the nested operation
        OperationSetup os = pop
                .newOperationSetup(new NewOperation(operation.bean, bindingExtension, template, OperationHandle::new, new EmbeddedIntoOperation(operation, index)));
        bind(new FromEmbeddedOperation(os));

        // Resolve the new operation immediately
        scanner.resolveBindings(os);
        return this;
    }

    private void checkBeforeBind() {
        checkNotBound();
        if (operation.target instanceof MemberOperationTarget mos && mos.target instanceof OperationFieldTarget fos && Modifier.isStatic(fos.modifiers())
                && !allowStaticFieldBinding) {
            throw new BeanInstallationException("Static field binding is not supported for");
        }
    }

    private void checkNotBound() {
        if (isBound()) {
            throw new IllegalStateException("A binding has already been created");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> invokedBy() {
        return operation.installedByExtension.extensionType;
    }

    /** {@return whether or not a binding has already been created.} */
    @Override
    public boolean isBound() {
        return operation.bindings[index] != null;
    }


    static void process(BeanIntrospectorSetup introspectorSetup, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations,
            OnAnnotatedVariableCache... annotatedFields) {
    }

    /** {@inheritDoc} */
    @Override
    public OnVariable mirror(Supplier<? extends BindingMirror> supplier) {
        checkNotBound();
        this.mirrorSupplier = requireNonNull(supplier);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromVariable(variable);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return variable;
    }
}
