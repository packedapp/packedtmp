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
package internal.app.packed.util;

import java.util.HashMap;

import app.packed.util.Nullable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.util.TreeNode.ActualNode;

/**
 *
 */
public class NamedTreeNode<T extends ActualNode<T>> extends TreeNode<T> {

    public String name;

    /** Maintains unique names for child containers. */
    public final HashMap<String, ContainerSetup> children = new HashMap<>();

    /**
     * @param treeParent
     * @param treeThis
     */
    public NamedTreeNode(@Nullable T treeParent, T treeThis) {
        super(treeParent, treeThis);
    }

}
