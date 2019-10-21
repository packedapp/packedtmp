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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import packed.internal.component.ComponentModel;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.reflect.ClassProcessor;

/**
 * This class exists because we have to ways to access the members of a component. One with a {@link Lookup} object, and
 * one without.
 */
public interface ComponentLookup {

    ClassProcessor newClassProcessor(Class<?> clazz, boolean registerNatives);

    ComponentModel componentModelOf(Class<?> componentType);

    <T> FactoryHandle<T> readable(FactoryHandle<T> factory);

    default MethodHandle toMethodHandle(FactoryHandle<?> factory) {
        return readable(factory).toMethodHandle();
    }
}
