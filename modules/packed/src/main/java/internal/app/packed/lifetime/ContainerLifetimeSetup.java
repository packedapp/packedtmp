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
package internal.app.packed.lifetime;

import java.util.ArrayList;
import java.util.List;

import app.packed.base.Nullable;
import internal.app.packed.container.ContainerSetup;

/**
 * The lifetime of an application
 */
public class ContainerLifetimeSetup extends LifetimeSetup {

    /** Any child lifetimes. */
    private List<LifetimeSetup> children;

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(ContainerSetup container, @Nullable ContainerLifetimeSetup parent) {
        super(parent);
        this.container = container;
    }

    public LifetimeSetup addChild(ContainerSetup component) {
        LifetimeSetup l = new LifetimeSetup(this);
        if (children == null) {
            children = new ArrayList<>(1);
        }
        children.add(l);
        return l;
    }
}
