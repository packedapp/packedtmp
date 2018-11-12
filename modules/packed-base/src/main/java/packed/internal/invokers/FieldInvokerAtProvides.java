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

import app.packed.inject.BindingMode;
import app.packed.inject.Inject;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.util.Nullable;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** This class represents a field annotated with the {@link Inject} annotation. */
public final class FieldInvokerAtProvides extends FieldInvoker {

    /** The binding mode from {@link Provides#bindingMode()}. */
    private final BindingMode bindingMode;

    /** An (optional) description from {@link Provides#description()}. */
    @Nullable
    private final String description;

    /** The key under which this field will deliver services. */
    private final Key<?> key;

    /**
     * Creates a new invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object
     */
    private FieldInvokerAtProvides(InternalFieldDescriptor descriptor, Lookup lookup, Provides provides) {
        super(descriptor, lookup);
        this.key = null;// Key.fromMethodReturnType(method) .from MethodReturnType();
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.bindingMode = provides.bindingMode();
    }

    /**
     * Returns the binding mode as defined by {@link Provides#bindingMode()}.
     *
     * @return the binding mode
     */
    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /**
     * Returns the (optional) description.
     * 
     * @return the (optional) description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Returns the key under which the provided service will be made available.
     * 
     * @return the key under which the provided service will be made available
     */
    public Key<?> getKey() {
        return key;
    }

    /**
     * Injects the specified value into the specified instance. Wrapping any value in optional if needed.
     * 
     * @param instance
     *            the instance for which to inject
     * @param value
     *            the value to inject
     */
    public Object getInstance(Object instance) {
        return getValue(instance);
    }

    static FieldInvokerAtProvides createIfPresent(MemberScanner builder, Field field, Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a.annotationType() == Provides.class) {
                InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
                if (builder.fieldsAtProvides == null) {
                    builder.fieldsAtProvides = new ArrayList<>(2);
                }
                FieldInvokerAtProvides fi = new FieldInvokerAtProvides(descriptor, builder.lookup, (Provides) a);
                builder.fieldsAtProvides.add(fi);
                return fi;
            }
        }
        return null;
    }
}
