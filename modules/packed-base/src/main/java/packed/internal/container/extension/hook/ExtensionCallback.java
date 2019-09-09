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

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import packed.internal.container.extension.PackedExtensionContext;

/**
 *
 */
// Bruges til at kalde tilbage paa extensions
public final class ExtensionCallback {
    private final MethodHandle mh;
    private final Object hookGroup;

    /**
     * @param mh
     * @param hookGroup
     */
    public ExtensionCallback(MethodHandle mh, Object hookGroup) {
        this.mh = mh;
        this.hookGroup = hookGroup;
    }

    public void invoke(PackedExtensionContext e, ComponentConfiguration component) throws Throwable {
        if (Extension.class.isAssignableFrom(mh.type().parameterType(0))) {
            mh.invoke(e.extension(), component, hookGroup);
        } else if (ExtensionNode.class.isAssignableFrom(mh.type().parameterType(0))) {
            mh.invoke(e.extension(), component, hookGroup);
        }
    }
}
