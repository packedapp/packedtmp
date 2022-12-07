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
import java.util.Set;

import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import app.packed.service.Key;
import internal.app.packed.container.Mirror;

/**
 *
 */

// En context laver ikke en ny bean. Men en ny bean lifetime

// En operation kan vaere i en context
// En operation kan starte en eller flere contexts
// En operation kan lave en Bean/Container/Application lifetime der kun eksistere saa laenge operationen koere...
// En Bean, Container kan vaere i en context. (Hvad med en application?)

// Fx BeanInitializationContext er aabenlyst OperationSpan (per bean). Fordi vi kan injecte forskellige ting...

// Scheduling
// @Schedule foo() on ContainerBean -> OperationSpan
// SExt.schedule(Op<?>) 
// @Schedule foo() on SExt.registerScheduler(Bean) ->OperationSpan
// @Schedule
public interface ContextSpanMirror extends Mirror {

    /** {@return the context.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension that defines the context.} */
    Class<? extends Extension<?>> extensionType();

    /**
     * All the operations that may create the context.
     * 
     * @return
     */
    Collection<OperationMirror> initiatedFrom();
    
    /** {@return services that are made specially available because of the context.} */
    Set<Key<?>> keys();

    ContextualizedElement element();

    ContextSpan span();

    enum ContextSpan {
        APPLICATION, BEAN, CONTAINER, OPERATION;
    }
}
// Tror ikke vi supporter Tree downward
// ContextSpan -> Operation, Bean, Container, (RestOfTree), Application
