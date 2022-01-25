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
package app.packed.bean.instance;

import java.util.Collection;

import app.packed.base.NamespacePath;
import app.packed.lifecycle.RunState;

/**
 *
 */
// Taekner ikke noget control...
// Det er alt sammmen ikke styre paa component

// RunState
// Monitoring
// Custom attributes (add/remove?? Fixed on component af build, fixed here efter instantiation)
// Adding listeners?? (when state=...->)

// Persistance... (state = Hibernating)
// Still here if persistet I would think... 

/// MHT til restart.. Taenker jeg lidt det betyder
/// forskellige navne
public interface Instance {

    /**
     * Returns an unmodifiable view of all of this component's children.
     *
     * @return an unmodifiable view of all of this component's children
     */
    Collection<Instance> children();

//    /**
//     * Returns the definition of this component instance.
//     * 
//     * @return the definition of this component instance
//     */
//    ComponentModel component(); /// nah.. vi retain

    /**
     * Returns the distance to the root component. The root component having depth 0.
     * 
     * @return the distance to the root component
     */
    // Maybe just a method on path().depth();
    int depth();

    /**
     * Returns the name of this component instance.
     *
     * @return the name of this component instance
     */
    String name();

    /**
     * Returns the path of this component instance.
     *
     * @return the path of this component instance
     */
    NamespacePath path();

    boolean isStateless(); // en component metode???
    // What if stateless? empty string

    String state();
}

/**
*
*/
interface InstanceStream {

}

//Tror bedre man kan snakke om component instances

//Hvorfor runtime component

//Vi har et kaempe trae. Men vi instantiater kun noget af det

//Syntes instance er et daarligt signal at sende... naar man f.eks.
//kun har nogle lambdas... eller statiske componneter

//Maybe just instance...
interface ZInstance {

    // Restart, reInitialize...
    // Not sure we should capture state like that
    // Also, fx syntes maaske ikke det giver mening at en
    // function har en state...
    RunState runState();

    Object sourceInstances();
}

// Runlet