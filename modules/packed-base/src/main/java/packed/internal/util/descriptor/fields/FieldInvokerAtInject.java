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
package packed.internal.util.descriptor.fields;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Inject;
import app.packed.util.Nullable;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** This class represents a field annotated with the Inject annotation. */
public final class FieldInvokerAtInject extends FieldInvoker {

    /** The field represented as a dependency */
    private final Dependency dependency;

    /**
     * Creates a new invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object
     */
    private FieldInvokerAtInject(InternalFieldDescriptor descriptor, Lookup lookup) {
        super(descriptor, lookup);
        this.dependency = Dependency.of(descriptor);
    }

    /**
     * Returns the dependency representing the field.
     *
     * @return the dependency representing the field
     */
    public Dependency dependency() {
        return dependency;
    }

    /**
     * Injects the specified value into the specified instance. Wrapping any value in optional if needed.
     * 
     * @param instance
     *            the instance for which to inject
     * @param value
     *            the value to inject
     */
    public void injectInstance(Object instance, Object value) {
        setField(instance, dependency.wrapIfOptional(value));
    }

    public static Collection<FieldInvokerAtInject> findInjectableFields(Class<?> clazz, Lookup lookup) {
        ArrayList<FieldInvokerAtInject> result = null;
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                // Extract valid annotations
                // First check for @Inject
                FieldInvokerAtInject i = tryCreate(field, lookup);
                if (i != null) {
                    if (result == null) {
                        result = new ArrayList<>(2);
                    }
                    result.add(i);
                }
            }
        }
        return result == null ? List.of() : List.copyOf(result);
    }

    // For now this is a separate method, when we also want to support components fields..
    @Nullable
    private static FieldInvokerAtInject tryCreate(Field field, Lookup lookup) {
        // First check for @Inject
        if (JavaXInjectSupport.isInjectAnnotationPresent(field)) {
            InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
            checkAnnotatedFieldIsNotStatic(descriptor, Inject.class);
            checkAnnotatedFieldIsNotFinal(descriptor, Inject.class);
            return new FieldInvokerAtInject(descriptor, lookup);
        }
        return null;
    }
}
