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

import app.packed.base.TreePath;
import app.packed.component.Component;
import app.packed.component.ComponentModifierSet;

/**
 * Available to all components within a container.
 */
//Available to all guests... The top level component only though...
// And I think stereotypes can remove it for their surragate objects.   
// Can remove itself...
// shutdown itself
// replace itself / restart itself

// Guest Context er kun noget med start/stop, restart osv at goere...

// Den er svaer at smide paa componenten selv, da vi kan koere container-freee.
// Men maaske smider de saa bare UOE.

// Ideen var at man kunne injecte den i componenter...

// Altsaa eneste problem er. Vil vi virkelig gerne have
// At f.eks. extensions kan stoppe en container de er deployet i??? IDK
public interface ContainerContext {

    TreePath path();

    /**
     * Returns the set of
     * 
     * @return a set of modifiers
     */
    ComponentModifierSet modifiers(); // Or should we just return the guest???

    /**
     * Returns the guest component.
     * 
     * @return the guest component
     */
    Component container();
}
//info

// State, initialization, startup reason,

// We don't point back to assembly context or initialization context
// because then we cannot gc