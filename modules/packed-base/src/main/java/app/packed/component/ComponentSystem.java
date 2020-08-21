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
package app.packed.component;

/**
 *
 */

// Hvorfor er en component ikke bare et system...
// Taenker ideen er lidt at et ComponentSystem har alle mulige maader at navigere paa
// Og hvorfor kan Component ikke have det???

// Forskellen er lidt at 
public interface ComponentSystem {

    // origin/base/source
    Component origin();

    default ComponentPath path() {
        return origin().path();
    }
}
// 1. Set of Components they don't need to wired together

// 2. Wired Components - All wired

//// Forest of these, for example, give me all containers...

// Tree Components - have a shared root, som ikke behoever at vaere system root

// Den sidste er vel Component

// containerView -> Just linked components that are in the same

// ComponentViews

// component.podView();
// component.containerView();
// component.

// podView 

// AssemblyView = System view... Assembly = The System
// System View = Root component.view

// Så det der er interessant er lad os lave et
// GuestView??? ContainerView

// GuestContext.....
// GuestDriver???
