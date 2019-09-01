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
package a;

import java.util.List;

import app.packed.container.extension.Extension;

/**
 *
 */
// Dette kan foerst laves naar artifacten er faerdig konfigureret

public class ExtensionRealTree<E extends Extension> {

    boolean hasCommonRoot() {
        throw new UnsupportedOperationException();
    }

    List<E> roots() {
        throw new UnsupportedOperationException();
    }
}
// The 3 Levels of communication
// Extensions communicate in the same artifact
// Extension communicate with host via sidecars
// Extensions communicate with each other in a sidemesh.. Either via extensions or sidecars