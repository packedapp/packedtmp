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
package packed.internal.container.model;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.Extension;
import app.packed.container.extension.graph.HookGroupProcessor;
import packed.internal.hook.HGBModel;

/**
 *
 */
// Bruges til at kalde tilbage paa extensions
final class HookCallback {

    private final Object hookGroup;

    HGBModel m;

    private final MethodHandle mh;

    public HookCallback(HGBModel m, Object hookGroup) {
        this.mh = null;
        this.m = requireNonNull(m);
        this.hookGroup = hookGroup;
    }

    /**
     * @param mh
     * @param hookGroup
     */
    public HookCallback(MethodHandle mh, Object hookGroup) {
        this.mh = mh;
        this.hookGroup = hookGroup;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void invoke(Extension e, ComponentConfiguration component) throws Throwable {
        if (m != null) {
            ((HookGroupProcessor) m.groupProcessor).process(e, component, hookGroup);
            return;
        }
        if (Extension.class.isAssignableFrom(mh.type().parameterType(0))) {
            mh.invoke(e, component, hookGroup);
        }
    }
}
