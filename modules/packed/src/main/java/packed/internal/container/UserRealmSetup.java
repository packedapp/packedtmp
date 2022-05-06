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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import app.packed.component.ComponentMirror;
import app.packed.component.Realm;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerHook;
import app.packed.container.ContainerMirror;
import packed.internal.component.ComponentSetup;

/**
 *
 */
public abstract sealed class UserRealmSetup extends RealmSetup permits AssemblyUserRealmSetup, ComposerUserRealmSetup {

    /**
     * All extensions that are used in the installer (if non embedded) An order set of extension according to the natural
     * extension dependency order.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));
    
    void initializeUserRealm(ContainerSetup container) {
        currentComponent = requireNonNull(container);
    }
    
    final void closeRealm() {
        ContainerSetup container = container();
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
        isClosed = true;

        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while being closed
        // In which case an Iterator might throw ConcurrentModificationException

        // Test and see if we are closing the root container
        if (container.parent == null) {
            // Root container
            // We must also close all extensions application-wide.
            ArrayList<ExtensionRealmSetup> list = new ArrayList<>(extensions.size());

            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e.extensionRealm);
                e.onUserClose();
                e = extensions.pollFirst();
            }

            container.application.injectionManager.finish(container.lifetime.pool, container);

            // Close all extensions application wide
            for (ExtensionRealmSetup extension : list) {
                extension.close();
            }

        } else {
            // Similar to above, except we do not close extensions application-wide
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                e.onUserClose();
                e = extensions.pollFirst();
            }
        }
    }

    /** {@return the setup of the root container of the realm.} */
    public abstract ContainerSetup container();

    /** {@return a mirror for this assembly.} */
    public AssemblyMirror mirror() {
        return new BuildtimeAssemblyMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public final Realm realm() {
        return Realm.application();
    }

    /** Implementation of {@link AssemblyMirror}. */
    public record BuildtimeAssemblyMirror(UserRealmSetup assembly) implements AssemblyMirror {

        /** {@inheritDoc} */
        @Override
        public List<Class<? extends ContainerHook>> containerHooks() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror root() {
            return assembly.container().mirror();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Assembly> assemblyType() {
            // Probably does not work for composer
            // Needs to check isAssignable
            return (Class<? extends Assembly>) assembly.realmType();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<ComponentMirror> components() {
            return assembly.container().stream().filter(c -> c.assembly == assembly).map(ComponentSetup::mirror);
        }

        /** {@inheritDoc} */
        @Override
        public Optional<AssemblyMirror> parent() {
            ContainerSetup org = assembly.container();
            for (ContainerSetup p = org.parent; p != null; p = p.parent) {
                if (org.assembly != p.assembly) {
                    return Optional.of(p.assembly.mirror());
                }
            }
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<AssemblyMirror> children() {
            return children(assembly, assembly.container(), new ArrayList<>()).stream();
        }

        private ArrayList<AssemblyMirror> children(UserRealmSetup assembly, ContainerSetup cs, ArrayList<AssemblyMirror> list) {
            if (assembly == cs.assembly) {
                for (ContainerSetup c : cs.containerChildren) {
                    children(assembly, c, list);
                }
            } else {
                list.add(cs.assembly.mirror());
            }
            return list;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return assembly.container().parent == null;
        }
    }
}
