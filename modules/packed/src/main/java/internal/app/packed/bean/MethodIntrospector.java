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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnMethod;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.InvocationSite;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.MethodOperationTarget;
import internal.app.packed.operation.PackedOperationHandle;

/** Internal implementation of BeanMethod. Discard after use. */
public final class MethodIntrospector implements OnMethod {

    /** Annotations on the method read via {@link Method#getAnnotations()}. */
    private final Annotation[] annotations;

    /** The internal introspector */
    public final Introspector introspector;

    /** The underlying method. */
    private final Method method;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    /** The operation type (lazily created). */
    @Nullable
    private OperationType type;

    MethodIntrospector(Introspector introspector, ExtensionSetup operator, Method method, Annotation[] annotations, boolean allowInvoke) {
        this.introspector = introspector;
        this.operator = operator;
        this.method = method;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new BeanAnnotationReader(annotations);
    }

    /** {@return modifiers for the member.} */
    public int getModifiers() {
        return method.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInvokeAccess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Method method() {
        return method;
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        requireNonNull(operator, "operator is null");
        // TODO, we must check this.operator er samme som operator eller en child of
        // Maaske er det et speciel tilfaelde af man vil invoke fra en anden container...
        // Tag den med i compute() istedet for???
        OperationSetup os = newOperation(BeanSetup.crack(operator).extensionOwner, invocationType);
        return new PackedOperationHandle(os);
    }

    // We expose this directly do bean extension, entry point, service extension
    // Extensions that do not necessarily have an extension bean installed to invoke the methods
    public OperationSetup newOperation(ExtensionSetup extension, InvocationType invocationType) {
        // TODO check that we are still introspecting? Or maybe on bean.addOperation

        MethodHandle methodHandle;
        Lookup lookup = introspector.oc.lookup(method);
        try {
            methodHandle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("stuff", e);
        }

        MethodOperationTarget mot = new MethodOperationTarget(methodHandle, method);
        InvocationSite oi = new InvocationSite(invocationType, extension);
        OperationSetup bos = new OperationSetup(introspector.bean, operationType(), oi, mot, null);
        introspector.bean.operations.add(bos);
        return bos;
    }

    /** {@inheritDoc} */
    @Override
    public OperationType operationType() {
        OperationType t = type;
        if (t == null) {
            t = type = OperationType.ofExecutable(method);
        }
        return t;
    }
}