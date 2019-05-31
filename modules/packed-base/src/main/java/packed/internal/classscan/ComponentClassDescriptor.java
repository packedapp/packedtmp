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
package packed.internal.classscan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;

/**
 *
 */
// Includere den lookup??? Ja det taenker jeg
public class ComponentClassDescriptor {

    public static void process(ContainerConfiguration container, ComponentConfiguration component) {

    }

    public static BiConsumer<ContainerConfiguration, ComponentConfiguration>[] scan(Class<?> clazz) {
        HashSet<ExtensionGroup> s = new HashSet<>();
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                Annotation[] annotations = method.getAnnotations();
                for (Annotation a : annotations) {
                    ExtensionGroup ce = ExtensionGroup.FOR_ANNOTATION.get(a.annotationType());
                    if (ce != ExtensionGroup.EMPTY) {
                        s.add(ce);
                    }
                }

            }
        }
        return s.toArray(i -> new ExtensionGroup[i]);
    }
}
