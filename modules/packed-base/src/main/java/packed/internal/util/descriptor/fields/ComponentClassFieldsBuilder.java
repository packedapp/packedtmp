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

import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 *
 */
public class ComponentClassFieldsBuilder {

    ArrayList<FieldInvokerAtInject> result = null;

    public Collection<FieldInvokerAtInject> injectableFields() {
        throw new UnsupportedOperationException();
    }

    public static ComponentClassFieldsBuilder create(Class<?> clazz, Lookup lookup) {
        ArrayList<InternalFieldDescriptor> result = null;

        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {

            }
        }
        return null;
    }

    public static Collection<FieldInvokerAtInject> findInjectableFields(Class<?> clazz, Lookup lookup) {
        ArrayList<FieldInvokerAtInject> result = null;

        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                // Extract valid annotations
                // First check for @Inject
                FieldInvokerAtInject i = null;// tryCreate(field, lookup);
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
}
