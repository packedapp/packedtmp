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
package app.packed.hooks.usage;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;

/**
 *
 */
abstract class InterceptingExtensionMethodSetup {

    public final int inject(Class<?> key) {
        return 0;
    }

    public final int inject(Key<?> key) {
        return 0; // int = parameter
    }

    public final int storeInstance(Class<?> type) {
        return 0;
    }

    public final MethodHandle handle() {
        throw new UnsupportedOperationException();
    }
}
