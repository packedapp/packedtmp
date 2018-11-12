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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import app.packed.inject.Provides;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 *
 */
public class ComponentClassDescriptor<T> extends ServiceClassDescriptor<T> {

    /** A cached map of all fields for a particular annotation. */
    final ConcurrentHashMap<Class<? extends Annotation>, List<InternalFieldDescriptor>> annotatedFields = new ConcurrentHashMap<>();

    /** All fields that have at least 1 annotation. */
    List<InternalFieldDescriptor> fieldsAllAnnotated;

    /** All methods annotated with {@link Provides}. */
    final Collection<MethodInvokerAtProvides> methodsAtProvides;

    /**
     * @param clazz
     * @param lookup
     */
    ComponentClassDescriptor(Class<T> clazz, Lookup lookup, MemberScanner scanner) {
        super(clazz, lookup, scanner);
        this.methodsAtProvides = scanner.methodsAtProvides == null ? List.of() : List.copyOf(scanner.methodsAtProvides);
    }
}
