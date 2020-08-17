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

import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;

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

    /** The description of this component (optional). */
    @Nullable
    final String description;

    /** Any extension the component belongs to. */ // Generic Extension Table?
    final Optional<Class<? extends Extension>> extension;

    final PackedComponentDriver<?> driver; // tmp

    RuntimeComponentModel(ComponentNodeConfiguration context) {
        this.depth = context.depth;
        this.configSite = requireNonNull(context.configSite());
        this.description = context.getDescription();
        this.extension = context.extension();
        this.driver = context.driver();
    }

    public boolean isContainer() {
        return driver.isContainer();
    }

    public Optional<Class<? extends Extension>> extension() {
        return extension;
    }

    static RuntimeComponentModel of(ComponentNodeConfiguration context) {
        return new RuntimeComponentModel(context);
    }

}

//Sealed type....

//SingleLetter
//H <- Host
//S <- ActorSystem
//A <- Actor
//I <- Instance
//M <- Many
//C <- Container
//V <- VirtualContainer

//Task --> ComponentContext.addTask(Class).
//Task --> ComponentContext.addTask(Class, Composer<? super TaskConfiguration>)).

//Like ElementType

// configuration tyoe
// runtime tyoe
// context type

// Task <------