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
package app.packed.context;

import java.util.Collection;

import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;

/**
 *
 */

// En operation context er kun til raadighed for ejeren af operationen
// En bean context er kun raadighed for ejeren af beanen
// Container context er kun til raadighed for applications ejeren

// En context laver ikke en ny bean. Men en ny bean lifetime

// En operation kan vaere i en context
// En operation kan starte en eller flere contexts
// En operation kan lave en Bean/Container/Application lifetime der kun eksistere saa laenge operationen koere...
// En Bean, Container kan vaere i en context. (Hvad med en application?)

// Extendable?

// Fx BeanInitializationContext er aabenlyst OperationSpan (per bean). Fordi vi kan injecte forskellige ting...

// Scheduling
// @Schedule foo() on ContainerBean -> OperationSpan
// SExt.schedule(Op<?>)
// @Schedule foo() on SExt.registerScheduler(Bean) ->OperationSpan
// @Schedule
public interface ContextMirror extends Mirror {

//    default Author author() {
//        ContextScopeMirror m = scope();
//        if (m instanceof OperationMirror om) {
//            return om.bean().owner();
//        } else if (m instanceof BeanMirror bm) {
//            return bm.owner();
//        } else {
//            return Author.application();
//        }
//    }

    /** {@return the context.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension that defines the context.} */
    Class<? extends Extension<?>> extensionClass();

    /**
     * All the operations that may create the context.
     *
     * @return
     */

    // Er context lavet i forbindelse med en LifetimeOperation
    // Kan kun vaere bean eller container

    //// Hmm, er det entry points, and if yes is an operation its own entry point
    // Lad os sige det er sessions context...
    // Lad os sige det er en
    Collection<OperationMirror> initiatingOperations();

    ContextScopeMirror scope();
}
