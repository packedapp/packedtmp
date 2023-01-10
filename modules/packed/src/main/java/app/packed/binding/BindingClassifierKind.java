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
package app.packed.binding;

import app.packed.bean.BeanHook.AnnotatedVariableHook;
import app.packed.bean.BeanHook.VariableTypeHook;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;

/**
 *
 */
public enum BindingClassifierKind {

    /**
     * The binding has been created manually.
     * 
     * @see OperationHandle#bindable(int)
     * @see Op#bind(Object)
     * @see Op#bind(int, Object, Object...)
     */
    MANUAL,

    /**
     * The binding has been created because of a Hook. Either the variable is annotated with a binding hook. Or the variable
     * class is annotated with BindingHook
     * 
     * 
     * 
     * @see AnnotatedVariableHook
     **/
    BINDING_ANNOTATION,

    /**
     * The binding has been created because of a Hook. Either the variable is annotated with a binding hook. Or the variable
     * class is annotated with BindingHook
     * 
     * 
     * 
     * @see VariableTypeHook
     **/
    BINDING_TYPE, // or BINDING_TYPE
    
    KEY, // There is a key
}
