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
package internal.app.packed.container;

import java.util.function.Consumer;

import internal.app.packed.lifetime.PackedContainerLifetimeChannel;

/**
 * The basic idea is that we can configure some stuff that must be done on first usage of the extension
 */
public final class ExtensionPreLoad {

    Consumer<? super ExtensionSetup> onUse;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void add(PackedContainerLifetimeChannel<?> bridge) {
        Consumer<? super ExtensionSetup> c = bridge.onUse();
        if (c != null) {
            onUse = onUse == null ? c : onUse.andThen((Consumer) c);
        }
    }
}