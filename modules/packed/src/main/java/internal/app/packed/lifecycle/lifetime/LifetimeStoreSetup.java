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
package internal.app.packed.lifecycle.lifetime;

import java.util.ArrayList;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.ExtensionContext;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
final class LifetimeStoreSetup {

    final ArrayList<Object> entries = new ArrayList<>();

    public int addAttachment(OperationSetup bean) {
        return 0;
    }

    public int addBean(BeanSetup bean) {
        if (bean.beanKind == BeanKind.CONTAINER && bean.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
            entries.add(bean);
            return entries.size() - 1;
        }
        return -1;
    }

    // For example, lifetime store
    public void addInternal(Class<?> other) {
        entries.add(other);
    }

    public ExtensionContext newRuntimePool() {
        return PackedExtensionContext.create(entries.size());
    }
}
