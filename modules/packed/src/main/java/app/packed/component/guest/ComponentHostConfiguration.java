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
package app.packed.component.guest;

import java.util.HashMap;
import java.util.Map;

import app.packed.application.ApplicationTemplate;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.binding.Key;
import app.packed.extension.BaseExtensionPoint;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.container.PackedContainerLink;

/**
 *
 */
// Den er ikke provideable, fordi den bliver lavet sammen med sub-applikationern

// Vi har brug for den her fordi vi Templaten bliver lavet af packed. Vi skal specificere contexts osv.
// Hvor de kommer fra, ect

// De enkelt required service links maa addes her
public class ComponentHostConfiguration<T> extends InstanceBeanConfiguration<T> {

    Map<Key<?>, PackedContainerLink> links = new HashMap<>();

    /**
     * @param handle
     */
    ComponentHostConfiguration(GuestBeanHandle handle) {
        super(handle);
    }

    // Fungere ikke... Hvad goer vi med det vi bygger nu
    public static <T> ComponentHostConfiguration<T> installApplicationHost(ApplicationTemplate<?> template, BaseExtensionPoint extensionPoint,
            Class<T> beanClass) {
        throw new UnsupportedOperationException();
    }

    public static <T> ComponentHostConfiguration<T> installBeanHost(BaseExtensionPoint extensionPoint, Class<T> beanClass) {
        throw new UnsupportedOperationException();
    }

    public static <T> ComponentHostConfiguration<T> installContainerHost(BaseExtensionPoint extensionPoint, Class<T> beanClass) {
        throw new UnsupportedOperationException();
    }
}
