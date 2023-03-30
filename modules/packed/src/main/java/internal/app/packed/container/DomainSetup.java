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

import java.util.HashMap;
import java.util.Map;

import app.packed.util.Nullable;
import internal.app.packed.bean.BeanOwner;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.MagicInitializer;

/**
 *
 */
public final class DomainSetup {

    public static final MagicInitializer<DomainSetup> MI = MagicInitializer.of();

    public final BeanOwner owner;

    public final ExtensionSetup root;

    public final PackedDomainTemplate<?> template;

    public final String name = "main";

    public DomainSetup(PackedDomainTemplate<?> template, ExtensionSetup root, BeanOwner owner) {
        this.template = template;
        this.root = root;
        this.owner = owner;
    }

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    // Tror maaske vi har nogle strategies
    // AllApplication-> No nodes

    // Vi vil helst avoid node
    static class DomainNode extends AbstractTreeNode<DomainNode> {

        /**
         * @param treeParent
         */
        protected DomainNode(@Nullable DomainNode treeParent) {
            super(treeParent);
        }

    }
}
