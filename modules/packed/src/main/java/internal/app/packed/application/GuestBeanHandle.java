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
package internal.app.packed.application;

import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanTemplate.Installer;
import app.packed.binding.Key;
import app.packed.component.guest.ComponentHostConfiguration;
import app.packed.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;
import internal.app.packed.context.PackedComponentHostContext;

/**
 *
 */
public class GuestBeanHandle extends BeanHandle<ComponentHostConfiguration<?>> {

    /**
     * @param installer
     */
    public GuestBeanHandle(Installer installer) {
        super(installer);
    }
    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedLifecycle.class), Key.of(ServiceLocator.class));

    public static final PackedComponentHostContext DEFAULT = new PackedComponentHostContext(KEYS);
    public PackedComponentHostContext toContext() {
        return DEFAULT;
    }
}
