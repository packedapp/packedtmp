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
package packed.internal.container.extension.hook;

import java.lang.invoke.MethodHandle;

/**
 *
 */
// Bruges til at kalde tilbage paa extensions
public final class ExtensionCallback {
    public final MethodHandle mh;
    public final Object o;

    /**
     * @param mh
     * @param o
     */
    public ExtensionCallback(MethodHandle mh, Object o) {
        this.mh = mh;
        this.o = o;
    }
}
