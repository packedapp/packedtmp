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
package packed.internal.hooks.impl;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import app.packed.hooks.BeanMethod;
import app.packed.hooks.BeanOperation;
import app.packed.inject.FactoryType;

/**
 *
 */
public class HookedBeanMethod implements BeanMethod {

    public final BeanScanner scanner;

    private final Method method;

    HookedBeanMethod(BeanScanner scanner, Method method) {
        this.scanner = scanner;
        this.method = method;
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
    public MethodHandle methodHandle() {
        return scanner.oc.unreflect(method);
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperation operation() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public FactoryType type() {
        return FactoryType.ofExecutable(method);
    }

}
