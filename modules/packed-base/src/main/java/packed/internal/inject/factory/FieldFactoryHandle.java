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
package packed.internal.inject.factory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.TypeLiteral;
import app.packed.introspection.FieldDescriptor;
import packed.internal.inject.dependency.DependencyDescriptor;

/** An invoker that can read and write fields. */
final class FieldFactoryHandle<T> extends FactoryHandle<T> {

    /** The field we invoke. */
    final FieldDescriptor field;

    private final List<DependencyDescriptor> dependencies;

    private final Object instance = null;

    @SuppressWarnings("unchecked")
    FieldFactoryHandle(FieldDescriptor field, List<DependencyDescriptor> dependencies) {
        super((TypeLiteral<T>) field.getTypeLiteral());
        this.field = field;
        this.dependencies = dependencies;
    }

    /**
     * Compiles the code to a single method handle.
     * 
     * @return the compiled method handle
     */
    @Override
    public MethodHandle toMethodHandle(Lookup lookup) {
        MethodHandle handle;
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                // vs MethodHandles.private???
                lookup = lookup.in(field.getDeclaringClass());
            }
            handle = field.unreflectGetter(lookup);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        if (instance != null) {
            handle = handle.bindTo(instance);
        }
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public List<DependencyDescriptor> dependencies() {
        return dependencies;
    }
}
