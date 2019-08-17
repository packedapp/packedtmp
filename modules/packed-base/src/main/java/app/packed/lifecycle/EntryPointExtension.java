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
package app.packed.lifecycle;

import app.packed.extension.Extension;

/**
 * 
 * If an artifact does not contain any entry points. The artifact will, once started, run until it shutdown by the user.
 * 
 * <p>
 * Containers that link other containers with this extension will automatically have the extension installed.
 * <p>
 * This extension or {@link Main} is not supported at runtime.
 */

final class EntryPointExtension extends Extension {

    /** Creates a new entry point extension. */
    EntryPointExtension() {}

}
