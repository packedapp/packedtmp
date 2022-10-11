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

import java.util.List;

import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.service.inject.InternalDependency;

/**
 * Represents a single operation.
 */

// Hvad er en operation? En operation er noget der skal have en OperationMirror

public sealed abstract class OperationSetup permits BeanOperationSetup, FusedOperationSetup, CompositeOperationSetup {

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] EMPTY = new BindingSetup[0];

    /** The bean this operation concerns. */
    public final BeanSetup bean;

    /** Bindings for this operation. {@code null} represents an unbound parameter, maybe yes. */
    public final BindingSetup[] bindings;

    /** The type of the operation. */
    public final OperationType type;

    protected OperationSetup(BeanSetup bean, OperationType type) {
        this.bean = bean;
        this.type = type;
        this.bindings = type.parameterCount() == 0 ? EMPTY : new BindingSetup[type.parameterCount()];
    }

    public abstract OperationMirror mirror();

    public void resolve() {
        List<InternalDependency> id = InternalDependency.fromOperationType(type);
        for (int i = 0; i < bindings.length; i++) {
            if (bindings[i] == null) {
                System.out.println("XXX");
                InternalDependency ia = id.get(i);
                bean.container.sm.addBinding(ia.key(), !ia.isOptional(), (BeanOperationSetup) this, i);
            }
        }
    }
}
