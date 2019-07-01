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
package app.packed.container;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;

/**
 *
 */
// Hook annotations must be annotated with @ActivateHook()
// Maybe something with Cache, ExtensionComponentCache

// This should be an interface... However, We need to fix the code that can extract E.
// Right now it only deals with abstract classes. (TypeVariable, Key, ...) because thats what we needed at the time
public abstract class ContainerExtensionHookProcessor<E extends ContainerExtension<E>> {
    public abstract BiConsumer<ComponentConfiguration, E> onBuild();
}
