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
package packed.internal.inject2;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerExtension;

/**
 *
 */
// Skal vi have
public abstract class HookProcessor<B extends HookProcessor<?, ?, ?>, E extends ContainerExtension<E>, R> {

    protected abstract BiConsumer<ComponentConfiguration, E> onBuild();

    protected BiConsumer<ComponentConfiguration, R> onRuntime() {
        throw new UnsupportedOperationException("Not supported on runtime");//

    }
}
