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
package app.packed.extension;

import app.packed.framework.Framework;

/**
 * A framework extension indicates that an extension is part of the framework (Packed).
 * <p>
 * Framework extensions receive no special treatment. This class merely server as a marker to easily see whether or not
 * a particular extension is part of the framework.
 * <p>
 * A framework extension must be places in a {@link Module module} whose {@link Module#getName() name} is contained in
 * {@link Framework#moduleNames()} or the unnamed module.
 * 
 * @implNote This class is not a sealed class as the framework may consists of multiple modules in the future.
 */
// I think test extensions are part of the framework?? Or maybe have a FrameworkTestExtension?
public abstract class FrameworkExtension<E extends FrameworkExtension<E>> extends Extension<E> {}
