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

import java.lang.invoke.MethodHandles.Lookup;

import packed.internal.component.SourceModel;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.invoke.OpenClass;

/**
 * This class exists because we have two ways to access the members of a component instance. One with a {@link Lookup}
 * object, and one using whatever power a module descriptor has given us.
 */
interface ComponentLookup {

    SourceModel componentModelOf(Class<?> componentType);

    OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives);

    // Just return MethodHandle directly???
    <T> FactoryHandle<T> readable(FactoryHandle<T> factory);
}
