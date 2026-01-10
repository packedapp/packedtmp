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
package app.packed.component;

import app.packed.binding.Key;

/**
 *
 */
// Was SidebeanInstance
public interface Sidehandle {

    // I should probably be able to get the configuration???
    /**
     * @param <T>
     * @param key
     * @param object
     *
     * @see SidehandleBinding
     */
    default <T> void bindConstant(Class<T> key, T object) {
        bindConstant(Key.of(key), object);
    }

    <T> void bindConstant(Key<T> key, T object);
}
