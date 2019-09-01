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

import java.util.ArrayList;
import java.util.IdentityHashMap;

import app.packed.container.extension.Extension;
import app.packed.util.Nullable;

/**
 *
 */

// Runtime creates a wiring, if there is an @ExtensionProperties(wiring = InjectionWiring)

// Der er jo ikke kun WIRING!!! Ogsaa fucking integration pit....

public abstract class ExtensionWiring<E extends Extension> {

    // if null, you can search for extensions of the
    // Jaa, men maaske er grandparent ikke configureret....

    // Invoked the parent has been properly configured...
    // In order of linking....

    // Problemet er nok den injection pool, vi har mere brug for

    // for each linked bundle -> for each extension or
    // for each extension -> for each linked bundle
    // I think the first makes the most sense...

    // Kunne jo ogsaa vaere paa traet.... Saa har man dem samlet et sted
    public void onParentConfigured(MyExtensionTree tree, @Nullable E parent, E child) {

    }
}

class MyWiring extends ExtensionWiring<MyExtension> {

    @Override
    public void onParentConfigured(MyExtensionTree tree, @Nullable MyExtension parent, MyExtension child) {
        if (parent != null) {
            tree.m.computeIfAbsent(parent, e -> new ArrayList<>()).add(child);
        }
        // Kan smide i et IdentityMap, med
    }
}

class MyExtensionTree extends ExtensionTree<MyExtension> {

    final IdentityHashMap<MyExtension, ArrayList<MyExtension>> m = new IdentityHashMap<>();

    void onConfigured(MyExtension extension) {
        ArrayList<MyExtension> l = m.get(extension);
        if (l != null) {
            // Integration!!!!
            // Naehhh saa let skal det ikke vaere, hvad med images....
        }
    }
}
