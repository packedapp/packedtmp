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
package app.packed.service.mirror.oldMaybe;

import app.packed.binding.BindingHandle;
import app.packed.binding.Key;

/**
 *
 * @see app.packed.bean.BeanConfiguration#overrideService(Class, Object)
 * @see app.packed.bean.BeanConfiguration#overrideService(app.packed.util.Key, Object)
 */
// Alternativt peger den bare paa en ConstantMirror i beanen's namespace??
public class BeanServiceBindingMirror extends ServiceBindingMirror {

    /**
     * @param handle
     */
    public BeanServiceBindingMirror(BindingHandle handle) {
        super(handle);
    }

    @Override
    public Key<?> key() {
        throw new UnsupportedOperationException();
    }
}
