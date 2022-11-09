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
package app.packed.framework;

import app.packed.container.Extension;

/**
 * A framework extension is an extension that is implemented as part of the framework.
 * <p>
 * 
 * <p>
 * A framework extension must be places in a module with a name in contained in {@link Framework#moduleNames()} or the
 * unnamed module.
 * 
 * @implNote we do not make use of sealed classes because in the future the framework may consists of multiple modules.
 */
public abstract class FrameworkExtension<E extends FrameworkExtension<E>> extends Extension<E> {}
