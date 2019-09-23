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
package app.packed.lifecycle;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionNode;
import app.packed.hook.OnHookGroup;

/**
 *
 */
public final class LifecycleExtensionNode extends ExtensionNode<LifecycleExtension> {

    /**
     * @param context
     */
    protected LifecycleExtensionNode(ExtensionContext context) {
        super(context);
    }

    /**
     * This method once for each component method that is annotated with {@link Main}.
     * 
     * @param mh
     */
    @OnHookGroup(LifecycleHookAggregator.class)
    void addMain(ComponentConfiguration cc, LifecycleHookAggregator mh) {
        mh.applyDelayed.onReady(cc, LifecycleSidecar.class, (s, r) -> r.run());
        // TODO check that we do not have multiple @Main methods
    }
}
