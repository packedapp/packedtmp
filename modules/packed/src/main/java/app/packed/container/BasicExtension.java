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

import app.packed.service.ServiceExtension;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;

/**
 * Basic extensions are extensions that make use of internal APIs in the framework. In order words they cannot be
 * implemented by other users. A
 */
// Det der taeller imod den er at vi dokumentere protected members
// Og ogsaa grunden til den blev droppet i foerste omgang

// Det kan ogsaa vaere vi ender med en stor gang internal fifilifoo ogsaa for FileExtension, TemplateExtension, ect..
// Fx TestExtension kunne sagtens bruge interne APIs
public abstract sealed class BasicExtension<E extends BasicExtension<E>> extends Extension<E>permits ServiceExtension {

    protected final ContainerSetup container() {
        return extension.container;
    }

    protected final ExtensionSetup extension() {
        return extension;
    }
}
