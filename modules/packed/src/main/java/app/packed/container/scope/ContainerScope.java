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
package app.packed.container.scope;

import app.packed.component.ComponentMirrorTree;
import app.packed.container.ContainerConfiguration;

/**
 *
 */

// Key X Scope
// Config X ApplicationScope
// CliArgs X ApplicationScope
// Alle Container X ContainerScope+ Immediately Children

// En anden loesning er at lave en
// ContainerScope.custom();
// Saa kan alle der har instancen dele det

public final class ContainerScope {

    /** {@return a scope that include all containers in an application.} */
    static ContainerScope applicationScope() {
        throw new UnsupportedOperationException();
    }

    static ContainerScope singleContainerScope(ContainerConfiguration m) {
        throw new UnsupportedOperationException();
    }
    
    // Spoergmaalet er om det ikke er en build ting??? Saa vi ikke skal arbejde med mirrors
    static ContainerScope containerScope(ContainerConfiguration m) {
        throw new UnsupportedOperationException();
    }

    static ContainerScope treeScope(ComponentMirrorTree t) {
        throw new UnsupportedOperationException();
    }
}
