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
package app.packed.container.extension;

/**
 * A composable extension is a specific type of extension that allows extensions to communicate across of containers.
 */
public abstract class ComposableExtension<T extends ExtensionNode<?>> extends Extension {

    /**
     * Returns the extension's extension node. This method will be invoked exactly once by the runtime and must not return
     * null.
     * 
     * @return the extension's extension node
     */
    protected abstract T node();
}
