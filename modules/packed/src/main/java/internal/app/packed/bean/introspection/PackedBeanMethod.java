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
package internal.app.packed.bean.introspection;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector$AnnotationReader;
import app.packed.bean.BeanIntrospector$OnMethodHook;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.Wrapper.MethodWrapper;
import internal.app.packed.operation.PackedOperationHandle;

/** Internal implementation of BeanMethod */
// Taenker den bliver en inner class ad PackedBeanIntrospector?
public final class PackedBeanMethod implements BeanIntrospector$OnMethodHook {

    /** The internal introspector */
    public final BeanIntrospectionHelper introspector;

    /** The underlying method. */
    private final Method method;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    /** The operation type (lazily created). */
    @Nullable
    private OperationType type;

    private final Annotation[] annotations;

    PackedBeanMethod(BeanIntrospectionHelper introspector, ExtensionSetup operator, Method method, Annotation[] annotations, boolean allowInvoke) {
        this.introspector = introspector;
        this.operator = operator;
        this.method = method;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    @Override
    public BeanIntrospector$AnnotationReader annotations() {
        return new PackedAnnotationReader(annotations);
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

    // Taenker vi goer det her naar vi laver en operation
    public MethodHandle newMethodHandle() {
        return introspector.oc.unreflect(method);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        MethodHandle methodHandle = newMethodHandle();
        // Hvad hvis vi koere raw???? Tror det er det samme
        OperationSetup os = new OperationSetup(operationType(), operator, invocationType, introspector.bean, new MethodWrapper(methodHandle, method));
        introspector.bean.addOperation(os);
        return new PackedOperationHandle(os);
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
