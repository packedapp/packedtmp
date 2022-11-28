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
package app.packed.operation.context;

import java.util.Collection;
import java.util.Optional;

import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;

/**
 *
 */

// En operation kan vaere i en context
// En operation kan starte en eller flere contexts
// En operation kan lave en Bean/Container/Application der kun eksistere saa laenge operationen koere...
// En Bean, Container kan vaere i en context. (Hvad med en application?)


// Fx BeanInitializationContext er aabenlyst OperationSpan (per bean). Fordi vi kan injecte forskellige ting...

// Scheduling
// @Schedule foo() on ContainerBean -> OperationSpan
// SExt.schedule(Op<?>) 
// @Schedule foo() on SExt.registerScheduler(Bean) ->OperationSpan
// @Schedule
public interface OperationContextMirror extends Mirror {

    // ContextSpan -> Operation, Bean, Container, (RestOfTree), Application
    
    Class<?> contextClass();
    
    Optional<LifetimeMirror> createsNew(); // if non-operation

    /**
     * The operation from which the context may be created
     * 
     * @return
     */
    //fx multiple @Get on the same bean (or in the same container)
    Collection<OperationMirror> operations();
    
    ContextSpan span();
    // span
    
    // Container or Container_Lifetime???
    enum ContextSpan {
        APPLICATION, BEAN, CONTAINER_LIFETIME, OPERATION;
    }
}

// Object scope(); // Operation|Bean|Container(Tree?)
