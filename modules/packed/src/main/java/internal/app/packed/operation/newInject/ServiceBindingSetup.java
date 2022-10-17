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
package internal.app.packed.operation.newInject;

import app.packed.base.Nullable;
import app.packed.operation.BindingMirror;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.BindingSetup;

/**
 * A binding to a service.
 */
public final class ServiceBindingSetup extends BindingSetup {

    /** An entry corresponding to the key. */
    public final ServiceManager.Entry entry;

    /** A binding in the same container for the same key */
    @Nullable
    ServiceBindingSetup nextFriend;

    /** Whether or not the binding is required. */
    public final boolean required;

    /**
     * @param beanOperation
     * @param index
     */
    ServiceBindingSetup(OperationSetup operation, int index, ServiceManager.Entry entry, boolean required) {
        super(operation, index);
        this.entry = entry;
        this.required = required;
    }

    /** {@return whether or not the service could be resolved.} */
    public boolean isResolved() {
        return entry.provider != null;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMirror mirror0() {
        return null;
    }
}
