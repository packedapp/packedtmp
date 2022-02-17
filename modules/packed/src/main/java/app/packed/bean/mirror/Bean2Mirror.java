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
package app.packed.bean.mirror;

import java.util.List;
import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanFactoryMirror;
import app.packed.bean.operation.BeanOperationMirrorSelection;

/**
 *
 */
public interface Bean2Mirror extends BeanElementMirror {

    /**
     * @return
     */
    ApplicationMirror application();

    // Er ikke sikker paa det skal vaere optional...
    // Vil fx godt vide om vi er blevet givet en instans fra start
    Optional<BeanFactoryMirror> factory();

    List<BeanFieldMirror> fields();
    
//    Optional<BeanConstructorMirror> initializer(); // A bean never has mm

    boolean isSynthetic(); // Maybe it is modifiers like real Member

    /* BeanLifecycleMirror */ Object lifecycle();

    List<BeanMemberMirror> members();

    List<BeanMethodMirror> methods();

    BeanOperationMirrorSelection<BeanOperationMirror> operations();

    <T extends BeanOperationMirror> BeanOperationMirrorSelection<T> operations(Class<T> operationType);
}
