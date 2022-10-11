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
import internal.app.packed.operation.BeanOperationSetup;
import internal.app.packed.operation.binding.BindingSetup;

/**
 *
 */
public final class ServiceBindingSetup extends BindingSetup {

    // Noget omkring required
    final ServiceManager.Entry entry;

    // A binding in the same container for the same key
    @Nullable
    ServiceBindingSetup nextFriend;

    final boolean required;

    /**
     * @param beanOperation
     * @param index
     */
    ServiceBindingSetup(BeanOperationSetup operation, int index, ServiceManager.Entry entry, boolean required) {
        super(operation, index);
        this.entry = entry;
        this.required = required;
    }
}
