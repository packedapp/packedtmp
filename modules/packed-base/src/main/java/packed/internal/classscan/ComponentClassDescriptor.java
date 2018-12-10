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
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 *
 */
public class ComponentClassDescriptor extends ServiceClassDescriptor {

    // Need the lookup....
    // Also with regards to ComponentMethod....
    // Access might fail at runtime.... For example, if we stream all component methods....
    // And one is private, the lookup didn't not allow it. I think its find just to throw a runtime exception

    /** A cached map of all fields for a particular annotation. */
    final ConcurrentHashMap<Class<? extends Annotation>, List<InternalFieldDescriptor>> annotatedFields = new ConcurrentHashMap<>();

    /** All fields that have at least 1 annotation. */
    List<InternalFieldDescriptor> fieldsAllAnnotated;

    /**
     * @param clazz
     * @param lookup
     */
    ComponentClassDescriptor(Class<?> clazz, Lookup lookup, MemberScanner scanner) {
        super(clazz, lookup, scanner);
    }
}
