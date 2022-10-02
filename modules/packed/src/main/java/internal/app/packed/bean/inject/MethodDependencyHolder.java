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
package internal.app.packed.bean.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;

import app.packed.base.Key;
import app.packed.bean.BeanIntrospector.OnMethod;
import internal.app.packed.operation.bindings.DependencyProducer;
import internal.app.packed.operation.bindings.InternalDependency;

/**
 *
 */
public class MethodDependencyHolder extends DependencyHolder {

    /** The modifiers of the field. */
    private final boolean isStatic;

    /** A direct method handle to the field. */
    public final MethodHandle methodHandle;

    public MethodDependencyHolder(OnMethod method, MethodHandle mh, boolean provideAsConstant, Key<?> provideAsKey) {
        super(InternalDependency.fromOperationType(method.operationType()), provideAsConstant, provideAsKey);
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.methodHandle = requireNonNull(mh);
    }

    @Override
    public DependencyProducer[] createProviders() {
        DependencyProducer[] providers = new DependencyProducer[isStatic ? 0 : 1];
        return providers;
    }

    public boolean isStatic() {
        return isStatic;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        return methodHandle;
    }
}
