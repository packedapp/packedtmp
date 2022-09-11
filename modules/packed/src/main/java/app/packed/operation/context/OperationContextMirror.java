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

import app.packed.application.ComponentMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;

/**
 *
 */
public interface OperationContextMirror extends Mirror {

    /**
     * The operation from which the context may be created
     * 
     * @return
     */
    //fx multiple @Get on the same bean (or in the same container)
    Collection<OperationMirror> operations();
    
    Optional<ComponentMirror> createsNew(); // if non-operation

    Class<?> contextClass();
}

// Object scope(); // Operation|Bean|Container(Tree?)
