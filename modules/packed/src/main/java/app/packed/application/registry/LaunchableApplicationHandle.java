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
package app.packed.application.registry;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLifetime;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.bean.PackedBeanTemplate;

/** A handle for an installed application. */
final class LaunchableApplicationHandle<T> extends BeanHandle<ProvidableBeanConfiguration<T>> {

    @SuppressWarnings("unused")
    private static final PackedBeanTemplate REPOSITORY_BEAN_TEMPLATE = PackedBeanTemplate.builder(BeanLifetime.SINGLETON).build();

    /**
     * @param installer
     */
    LaunchableApplicationHandle(BeanInstaller installer) {
        super(installer);
    }
}
