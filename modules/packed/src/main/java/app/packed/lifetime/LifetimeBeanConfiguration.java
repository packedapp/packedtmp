/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.lifetime;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;

/**
 *
 */
// Ideen er vi laver en af dem. Og saa "attached" til hver lifetime

// Altsaa vi skal jo have et @OnStop callback. Kan ligesaa vaere en bean... Som en runnable

// I only think extensions can install it
public final class LifetimeBeanConfiguration<T> extends BeanConfiguration<T> {

    /**
     * @param handle
     */
    public LifetimeBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

}
// A lifetime bean is a bean whose instances are bound to individual lifetime instances

// Typically an extension will have a single lifetime bean installed.
// But instances of it is used across multiple lifetimes