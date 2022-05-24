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
package app.packed.operation.mirror2.ig;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import app.packed.application.Realm;
import app.packed.base.Key;
import app.packed.inject.Variable;
import app.packed.operation.OperationMirror;

/**
 *
 */
// Ligesom vi har hidden beans, har vi hidden operations!!!!!!!!
public interface InjectionGraphA extends Iterable<OperationMirror> {

    // Hvad med ting der ikke har dependencies???
    // Taenker vi inkludere dem
    List<OperationMirror> consumers();
    

    List<Dependency> findAnnotated(Class<? extends Annotation> annoType);

    // Kind of like Parameter = Dependency, Operation = Executable
    sealed interface Dependency {
        
        int index(); // maybe, maybe not..

        /**
         * {@return whether or not the dependency is satisfiable
         */
        boolean isSatisfied(); // isSatisfied

        OperationMirror operation();
        
        Optional<Realm> resolvedBy();
        
        Variable variable();
    }

    enum DependencyType {
        ANNOTATION,
        AUTO_SERVICE, 
        CONSTANT,
        KEY,
        SERVICE;
    }
    
    non-sealed interface PrimeAnnoNode extends Dependency {
        Class<? extends Annotation> annotationType();
    }

    non-sealed interface ServiceNode extends Dependency {
        Key<?> key();
    }
}
