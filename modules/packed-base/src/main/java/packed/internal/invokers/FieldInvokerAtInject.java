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
package packed.internal.invokers;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.ArrayList;

import app.packed.inject.Dependency;
import app.packed.inject.Inject;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** This class represents a field annotated with the {@link Inject} annotation. */
public final class FieldInvokerAtInject extends FieldInvoker {

    /** The field represented as a dependency */
    private final InternalDependency dependency;

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
        this.dependency = InternalDependency.of(descriptor);
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
        setValue(instance, dependency.wrapIfOptional(value));
    }

    static void checkIfInjectable(FieldBuilder builder, Field f, Annotation[] annotations) {
        if (JavaXInjectSupport.isInjectAnnotationPresent(f)) {
            InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(f);
            checkAnnotatedFieldIsNotStatic(descriptor, Inject.class);
            checkAnnotatedFieldIsNotFinal(descriptor, Inject.class);
            if (builder.injectableFields == null) {
                builder.injectableFields = new ArrayList<>(2);
            }
            FieldInvokerAtInject fi = new FieldInvokerAtInject(descriptor, builder.lookup);
            builder.injectableFields.add(fi);
        }
    }
}
