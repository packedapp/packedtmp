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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentModifier;
import app.packed.config.ConfigSite;

/**
 * The different types of components that are supported in Packed.
 */

// Til noedt kan vi kalde den BuildinComponentType

// Driver + Bundle + X... Skal gaa igen imellem images...
// Faktisk er navnet vel ogsaa her????? Nah ikke for rod images containere...

// Ved intet om specifikke boern eller parents...
//Det maa ogsaa betyde at den ikke ved noget om pods...

// Naar man instantiere et image, for alt andet end roden (heller ikke roden taenker jeg)
// Skal man kunne smide RCM med over i.
public final class RuntimeComponentModel {

    /** The configuration site of the component. */
    final ConfigSite configSite;

    /** The depth of the component in a tree of components. */
    // Depth kan have 8 bit-> full depth, 8 bit, container depth, 8 bit artifact depth.
    final int depth;

    final int modifiers;

    RuntimeComponentModel(ComponentBuild compConf) {
        this.depth = compConf.treeDepth;
        this.configSite = requireNonNull(compConf.configSite());
        // this.extension = context.extension();
        int p = compConf.modifiers;
        p = PackedComponentModifierSet.removeIf(p, depth == 0, ComponentModifier.IMAGE_ROOT);
        this.modifiers = p;
    }

    public boolean isContainer() {
        return PackedComponentModifierSet.isSet(modifiers, ComponentModifier.CONTAINER);
    }

    static RuntimeComponentModel of(ComponentBuild context) {
        return new RuntimeComponentModel(context);
    }
}
