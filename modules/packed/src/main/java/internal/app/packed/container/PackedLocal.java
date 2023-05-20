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
package internal.app.packed.container;

import java.util.function.Supplier;

import app.packed.bean.BeanLocal;
import app.packed.container.ContainerLocal;
import app.packed.util.Nullable;

/**
 * The base class for container and bean locals.
 */
@SuppressWarnings("rawtypes")
public abstract sealed class PackedLocal<T> permits BeanLocal, ContainerLocal {

    /** An optional supplier that can provide initial values for a bean local. */
    final @Nullable Supplier<? extends T> initialValueSupplier;

    protected PackedLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }
}
