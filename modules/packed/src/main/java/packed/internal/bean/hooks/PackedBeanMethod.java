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

import app.packed.bean.hooks.BeanMethod;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.FactoryType;
import app.packed.operation.InjectableOperationHandle;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.RawOperationHandle;
import packed.internal.bean.operation.RawOperationSetup;
import packed.internal.container.ExtensionSetup;

/**
 *
 */
public final class PackedBeanMethod extends PackedBeanMember implements BeanMethod {

    /** The method we are wrapping. */
    private final Method method;

    PackedBeanMethod(BeanScanner scanner, ExtensionSetup extension, Method method, boolean allowInvoke) {
        super(scanner, extension);
        this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public FactoryType factoryType() {
        return FactoryType.ofExecutable(method);
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public InjectableOperationHandle newOperation(ExtensionBeanConfiguration<?> operator) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RawOperationHandle<MethodHandle> newRawOperation() {
        return new RawOperationSetup<MethodHandle>(this, openClass.unreflect(method));
    }

    /**  */
    private record BuildTimeMethodTargetMirror(PackedBeanMethod pbm) implements OperationTargetMirror.OfMethodInvoke {

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return pbm.method;
        }
    }
}
