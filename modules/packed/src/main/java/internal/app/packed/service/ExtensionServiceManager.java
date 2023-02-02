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
package internal.app.packed.service;

import java.util.HashMap;

import app.packed.bindings.Key;
import app.packed.framework.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.operation.OperationSetup;

/**
 * A service manager for extensions.
 *
 * <p>
 * We might embed the functionality directly into ExtensionSetup once the implementation is finalized.
 */
public final class ExtensionServiceManager {

    public final HashMap<Key<?>, ServiceSetup> entries = new HashMap<>();

    /** The (nullable) parent. */
    @Nullable
    final ExtensionServiceManager parent;

    public ExtensionServiceManager(@Nullable ExtensionServiceManager parent) {
        this.parent = parent;
    }

    public void addBean(BeanSetup bean) {
        provide(Key.of(bean.beanClass), bean.instanceAccessOperation(), bean.beanInstanceBindingProvider());
    }

    public ServiceBindingSetup bind(Key<?> key, boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        ServiceSetup e = entries.computeIfAbsent(key, ServiceSetup::new);
        return e.bind(isRequired, operation, operationBindingIndex);
    }

    public void provide(Key<?> key, OperationSetup operation, BindingResolution resolution) {
        ServiceSetup entry = entries.computeIfAbsent(key, ServiceSetup::new);

        // TODO Check same lifetime as the container, or own prototype service

        entry.setProvider(operation, resolution);
    }

    /**
     *
     */
    public void verify() {
        for (ServiceSetup ss : entries.values()) {
            if (ss.provider() == null) {
                throw new RuntimeException("OOPS + " + ss.key);
            }
        }
    }

}
