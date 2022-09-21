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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import app.packed.bean.BeanIntrospector$BeanMethod;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ExtensionBeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.PackedOperationHandle;
import internal.app.packed.operation.PackedOperationTarget;
import internal.app.packed.util.OpenClass;

/** Internal implementation of BeanMethod */
public final class PackedBeanMethod implements PackedOperationTarget, BeanIntrospector$BeanMethod {

    /** The bean that declares the member */
    public final BeanSetup bean;

    /** The underlying method. */
    protected final Method method;

    final OpenClass openClass;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    PackedBeanMethod(BeanSetup bean, BeanIntrospectionHelper scanner, ExtensionSetup operator, Method method, boolean allowInvoke) {
        this.openClass = scanner.oc;
        this.bean = bean;
        this.operator = operator;
        this.method = method;
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
    public OperationTargetMirror mirror() {
        return new BuildTimeMethodTargetMirror(this);
    }

    // Taenker vi goer det her naar vi laver en operation
    public MethodHandle newMethodHandle() {
        return openClass.unreflect(method);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator) {
        return new PackedOperationHandle(bean, this, ExtensionBeanSetup.crack(operator));
    }

    /** {@inheritDoc} */
    @Override
    public OperationType operationType() {
        return OperationType.ofExecutable(method);
    }

    /** An operation target mirror for a bean method. */
    public record BuildTimeMethodTargetMirror(PackedBeanMethod pbm) implements OperationTargetMirror.OfMethodInvoke {

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return pbm.method();
        }
    }
}
