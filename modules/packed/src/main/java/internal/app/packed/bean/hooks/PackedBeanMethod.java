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
package internal.app.packed.bean.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import app.packed.bean.BeanMethod;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.FactoryType;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInvocationType;
import app.packed.operation.OperationTargetMirror;
import internal.app.packed.bean.ExtensionBeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.PackedOperationBuilder;

/** Internal implementation of BeanMethod */
public final class PackedBeanMethod extends PackedBeanMember<Method> implements BeanMethod {

    PackedBeanMethod(BeanHookScanner scanner, ExtensionSetup operator, Method method, boolean allowInvoke) {
        super(scanner.bean, scanner, operator, method);
    }

    /** {@inheritDoc} */
    @Override
    public FactoryType factoryType() {
        return FactoryType.ofExecutable(member);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInvokeAccess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Method method() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationTargetMirror mirror() {
        return new BuildTimeMethodTargetMirror(this);
    }

    public MethodHandle newMethodHandle() {
        return openClass.unreflect(member);
    }

    /** {@inheritDoc} */
    @Override
    public OperationConfiguration newOperation(ExtensionBeanConfiguration<?> operator, OperationInvocationType operationType) {
        return new PackedOperationBuilder(this, ExtensionBeanSetup.from(operator), newMethodHandle());
    }

    /** An operation target mirror for a bean method.  */
    public record BuildTimeMethodTargetMirror(PackedBeanMethod pbm) implements OperationTargetMirror.OfMethodInvoke {

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return pbm.method();
        }
    }
}