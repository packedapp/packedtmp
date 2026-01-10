/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.extension.v2;

import app.packed.extension.Extension;

/**
 *
 */
// RuntimeExtension?
// Ideen er at ditch alt omkring extension classes on runtime
// Saa vi fjerne dem via condensing
// Alternativt, gemme interfaces og saa strip alle metoder fra det
// Og permitted implementation
class ExtensionRef {
    String className;

    public Class<Extension<?>> resolve(ClassLoader classLoader) {
        //classLoader.loadClass(className);
        throw new UnsupportedOperationException();
    }
}
