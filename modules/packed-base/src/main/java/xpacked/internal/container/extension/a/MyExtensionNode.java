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
package xpacked.internal.container.extension.a;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionNode;
import app.packed.hook.OnHookGroup;

/**
 *
 */
public final class MyExtensionNode extends ExtensionNode<MyExtension> {

    /**
     * @param context
     */
    protected MyExtensionNode(ExtensionContext context) {
        super(context);
    }

    @OnHookGroup(Agg.class)
    public void foo(ComponentConfiguration cc, Integer val) {
        System.out.println("Saa godt da");
    }

}
