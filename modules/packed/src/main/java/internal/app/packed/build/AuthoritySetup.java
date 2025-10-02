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
package internal.app.packed.build;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.component.ComponentRealm;
import app.packed.util.Nullable;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.util.AbstractTreeNode;

/**
 * The owner of a bean. Either the application (via an assembly) or an extension instance.
 */
public sealed abstract class AuthoritySetup<T extends AbstractTreeNode<T>> extends AbstractTreeNode<T> permits AssemblySetup, ExtensionSetup {

    public final ArrayList<ServiceBindingSetup> servicesToResolve;

    /**
     * @param treeParent
     */
    protected AuthoritySetup(@Nullable T treeParent, ArrayList<ServiceBindingSetup> servicesToResolve) {
        super(treeParent);
        this.servicesToResolve = requireNonNull(servicesToResolve);
    }

    public void resolve() {
        if (this instanceof ExtensionSetup es) {
            if (es.treeParent != null) {
                IO.println(es.treeParent);
                throw new Error();
            }
        }
        ArrayList<ServiceBindingSetup> unresolved = new ArrayList<>();
        for (ServiceBindingSetup sbs : servicesToResolve) {
            ServiceProviderSetup sp = sbs.resolve();
            if (sp instanceof NamespaceServiceProviderHandle n) {
                n.bindings.add(sbs);
            }
            if (sp == null && sbs.isRequired) {
                unresolved.add(sbs);
            }
        }

    }

    /** {@return a realm representing the owner.} */
    public abstract ComponentRealm authority();

    /** {@return whether or not the authority entity is still configurable.} */
    public abstract boolean isConfigurable();
}
