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

import static internal.app.packed.util.StringFormatter.format;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.bindings.Key;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 * A service manager for extensions.
 *
 * <p>
 * We might embed the functionality directly into ExtensionSetup once the implementation is finalized.
 */
// Ideen er at vi har en manager per extension instance
// Og saa leder man op recursivt
public final class ExtensionServiceManager {

    public final HashMap<Key<?>, ServiceSetup> entries = new HashMap<>();

    public final Map<Key<?>, BeanSetup> extensionBeans = new LinkedHashMap<>();

    public ServiceBindingSetup bind(Key<?> key, boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        ServiceSetup e = entries.computeIfAbsent(key, ServiceSetup::new);
        return e.bind(isRequired, operation, operationBindingIndex);
    }
    /** The (nullable) parent. */
    @Nullable
    final ExtensionServiceManager parent;

    public ExtensionServiceManager(@Nullable ExtensionServiceManager parent) {
        this.parent = parent;
    }

    public void addBean(BeanSetup bean) {
        if (extensionBeans.putIfAbsent(Key.of(bean.beanClass), bean) != null) {
            throw new InternalExtensionException(
                    "A bean of type '" + format(bean.beanClass) + "' has already been installed into the container '" + bean.container.path() + "'");
        }
    }

    @Nullable
    public BeanSetup lookup(Key<?> key) {
        ExtensionServiceManager m = this;
        do {
            BeanSetup b = m.extensionBeans.get(key);
            if (b != null) {
                return b;
            }
            m = m.parent;
        } while (m != null);
        return null;
    }

    /**
     *
     */
    public void resolve(ExtensionSetup e) {
        // Resolve all bindings
        for (ServiceSetup binding : entries.values()) {
            Class<?> ebc = binding.key.rawType();
            while (e != null) {
                Object val = e.beanClassMap.get(ebc);
                if (val instanceof BeanSetup b) {
                    binding.setProvider(b.instanceAccessOperation(), b.beanInstanceBindingProvider());
                    break;
                } else if (val != null) {
                    throw new InternalExtensionException("sd");
                }
                e = e.treeParent;
            }

        }
    }
}
