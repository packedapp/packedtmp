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
package app.packed.component.sandbox;

import app.packed.extension.Extension;

/**
 * Internal class, visible to the framwwork
 */
// Ideen er lidt at beskrive componenten typen her. Der vil vaere praecis en instant af den
// Maaske har den nogle callback metoder on build-time

// ComputedConstant in the stomich..
// Lots of final getters.
// All of them call constant.get();


// It is like ComponentMetaTemplate
// <Extension, Handle> -> comput

public class ComponentDefinition<E extends Extension<E>> {

    // ComponentPath how does it look
    // ComponentKind
    // BaseMirrorType

    // Relationships
    // How is the component attached to application/container/bean/operation/how do we stream mirrors
    // I think there

    // There are some components that need multi representation-> Namespace (Actually namespace, and a Container
    // Representation)
    // Let us treat it as the only one for now

    interface Builder {
        default <E extends Extension<?>> E build(Class<? extends Extension<?>> extensionClass) {
            throw new UnsupportedOperationException();
        }
    }
}
// Template, Handle, Configuration, Mirror