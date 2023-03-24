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
package sandbox.extension.beanpouch;

import java.util.Collection;
import java.util.List;

import app.packed.service.ServiceableBeanConfiguration;
import sandbox.extension.bean.BeanHandle;

/**
 *
 */

// Pouch er det om at supportere alle mulige annoteringer paa andet end
// container beans


// For a single bean, or for multiple beans in the same container lifetime
// Always a managed container bean

// A bean can only belong to a single pouch

// bean.installedBy == beanPouch.owner

// accept list, reject list extensions

public class PouchBeanConfiguration<T> extends ServiceableBeanConfiguration<T> {

    /**
     * @param handle
     */
    public PouchBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    public Collection<BeanHandle<?>> manages() {
        return List.of();
    }
}

// beanKind = Pouch
