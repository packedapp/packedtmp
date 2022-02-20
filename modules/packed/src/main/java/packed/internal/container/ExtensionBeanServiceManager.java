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
package packed.internal.container;

import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.bean.BeanSetup;

/**
 * Manages all beans for a single container.
 * 
 * <p>
 * We might embed the functionality directly into ExtensionSetup once the implementation is finalized.
 */
// Ideen er at vi har en manager per extension instance
// Og saa leder man op recursivt
public final class ExtensionBeanServiceManager {

    public final Map<Key<?>, BeanSetup> beans = new LinkedHashMap<>();

    /** The (nullable) parent. */
    @Nullable
    final ExtensionBeanServiceManager parent;

    ExtensionBeanServiceManager(@Nullable ExtensionBeanServiceManager parent) {
        this.parent = parent;
    }

    @Nullable
    public BeanSetup lookup(Key<?> key) {
        ExtensionBeanServiceManager m = this;
        do {
            BeanSetup b = m.beans.get(key);
            if (b != null) {
                return b;
            }
            m = m.parent;
        } while (m != null);
        return null;
    }
}
