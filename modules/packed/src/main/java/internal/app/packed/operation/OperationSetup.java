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
package internal.app.packed.operation;

import app.packed.operation.OperationMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.binding.BindingSetup;

/**
 *
 */
public sealed abstract class OperationSetup permits BeanOperationSetup, DynamicBindingOperationSetup {

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] EMPTY = new BindingSetup[0];

    /** The bean that defines the operation. */
    public final BeanSetup bean;
    
    /** Any binding for the operation. {@code null} represents an unbound parameter. */
    public final BindingSetup[] bindings;

    protected OperationSetup(BeanSetup bean, int count) {
        this.bean = bean;
        this.bindings = count == 0 ? EMPTY : new BindingSetup[count];
    }
    
    public abstract OperationMirror mirror();
}
