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
package internal.app.packed.entrypoint;

import java.util.ArrayList;

import app.packed.extension.Extension;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// Error messages comes later
public final class EntryPointManager {

    final Class<? extends Extension<?>> dispatcher;

    final ArrayList<OperationSetup> entryPoints = new ArrayList<>();

    final ContainerLifetimeSetup lifetime;

    public EntryPointManager(ContainerLifetimeSetup lifetime, Class<? extends Extension<?>> controlledBy) {
        this.lifetime = lifetime;
        this.dispatcher = controlledBy;
    }
}
