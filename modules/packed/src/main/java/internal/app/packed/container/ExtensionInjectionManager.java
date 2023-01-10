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
package internal.app.packed.container;

import static internal.app.packed.util.StringFormatter.format;

import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.binding.Key;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import internal.app.packed.bean.BeanSetup;

/**
 * Manages all beans for a single container.
 * 
 * <p>
 * We might embed the functionality directly into ExtensionSetup once the implementation is finalized.
 */
// Ideen er at vi har en manager per extension instance
// Og saa leder man op recursivt
public final class ExtensionInjectionManager {

    public final Map<Key<?>, BeanSetup> extensionBeans = new LinkedHashMap<>();

    /** The (nullable) parent. */
    @Nullable
    final ExtensionInjectionManager parent;

    public ExtensionInjectionManager(@Nullable ExtensionInjectionManager parent) {
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
        ExtensionInjectionManager m = this;
        do {
            BeanSetup b = m.extensionBeans.get(key);
            if (b != null) {
                return b;
            }
            m = m.parent;
        } while (m != null);
        return null;
    }
}
