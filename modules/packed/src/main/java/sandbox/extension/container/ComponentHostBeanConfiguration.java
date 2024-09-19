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
package sandbox.extension.container;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;

/**
 *
 */
// ContainerMultiHostBeanConfiguration holds, for example, one app

// This bean can start and stop multiple.
// Must be strongly connected to ContainerHostBeanConfiguration
public class ComponentHostBeanConfiguration extends BeanConfiguration {

    // MultiHostManager<T> <--- Where <T>

    /**
     * @param handle
     */
    public ComponentHostBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    final HashMap<String, ComponentGuestAdaptorBeanConfiguration<?>> holders = new LinkedHashMap<>();

    public Class<?> holderClass() {
        throw new UnsupportedOperationException();
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(holders.keySet());
    }

    public void add(ComponentGuestAdaptorBeanConfiguration<?> conf) {
        if (conf.holderClass() != holderClass()) {
            throw new IllegalArgumentException();
        }
        // Must be same container
        // Must not have been added before

        String beanName = conf.onBeanRename((oldName, newName) -> {
            // if (putIf)
            // try and move in map
        });

        System.out.println(beanName);
        // MotherFucker bean name can change!!!!
//        holders.put(null, conf)
    }

    public enum Kind {
        SAME_APP_KIND_PREBUILT;
    }

//    public @interface OnComponentGuestRunstateChange {
//
//    }

    // Ideen er her vi kan lave en masse konfiguration foerend vi installere
    // Isaer med hensyn til Host Collections
    // Enable
    public interface Props {

        Props addApplicationRegistry();

        ComponentHostBeanConfiguration install();
    }
}
