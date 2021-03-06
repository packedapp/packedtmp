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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.bean.BeanSetup;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.service.ServiceManagerSetup;
import packed.internal.service.runtime.PrototypeRuntimeService;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.service.runtime.ServiceInstantiationContext;

/** A entry wrapping a component source. */
public final class SourceInstanceServiceSetup extends ServiceSetup {

    /** The singleton source we are wrapping */
    private final BeanSetup source;

    /**
     * Creates a new node from an instance.
     * 
     * @param component
     *            the component we provide for
     */
    public SourceInstanceServiceSetup(ServiceManagerSetup im, BeanSetup component, Key<?> key) {
        super(key);
        this.source = requireNonNull(component);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return source.dependant();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return source.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return source.singletonAccessor != null;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return RuntimeService.constant(key(), source.singletonAccessor.read(context.pool));
        } else {
            return new PrototypeRuntimeService(this, context.pool, dependencyAccessor());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Singleton " + source;
    }
}
