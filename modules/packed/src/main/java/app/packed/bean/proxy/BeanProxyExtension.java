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
package app.packed.bean.proxy;

import app.packed.bean.ContainerBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.Factory;

/**
 *
 */
// Depends on BeanExtension, ClassGenExtension
// Igen er problemet lidt her sikkerhed...
public class BeanProxyExtension extends Extension<BeanProxyExtension> {
    BeanProxyExtension() {}

    // Problemet er her provide()
    // Vi skal maaske have en provide(ApplicationBeanConfiguration)

    // Installs what looks like an ApplicationBean. But is lazily initialized and started
    public <T> ContainerBeanConfiguration<T> installLazy(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public <T> ContainerBeanConfiguration<T> installLazy(Factory<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public void interceptAll() {}
}
