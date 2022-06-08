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
package packed.internal.bean.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import app.packed.bean.BeanMethod;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.FactoryType;
import app.packed.operation.OperationBuilder;
import app.packed.operation.mirror.OperationTargetMirror;
import packed.internal.bean.ExtensionBeanSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.operation.InjectableOperationSetup;

/** Internal implementation of BeanMethod */
public final class PackedBeanMethod extends PackedBeanMember<Method> implements BeanMethod {

    PackedBeanMethod(BeanMemberScanner scanner, ExtensionSetup operator, Method method, boolean allowInvoke) {
        super(scanner, operator, method);
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
    public OperationBuilder operationBuilder(ExtensionBeanConfiguration<?> operator) {
        return new InjectableOperationSetup(this, ExtensionBeanSetup.from(operator), newMethodHandle());
    }

    /** The operation target mirror of a bean method.  */
    public record BuildTimeMethodTargetMirror(PackedBeanMethod pbm) implements OperationTargetMirror.OfMethodInvoke {

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return pbm.member;
        }
    }
}
