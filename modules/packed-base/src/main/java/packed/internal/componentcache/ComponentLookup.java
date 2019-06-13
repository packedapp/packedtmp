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
package packed.internal.componentcache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class exists because we have to ways to access the members of a component. One with a {@link Lookup} object, and
 * one without.
 */
public interface ComponentLookup {

    ComponentClassDescriptor componentDescriptorOf(Class<?> componentType);

    MethodHandle acquireMethodHandle(Class<?> componentType, Method method);

    default MethodHandle acquireMethodHandle(Class<?> componentType, Constructor<?> constructor) {
        throw new UnsupportedOperationException();
    }

    default VarHandle acquireVarHandle(Class<?> componentType, Field field) {
        throw new UnsupportedOperationException();
    }

    // Maybe method for acquire
}
