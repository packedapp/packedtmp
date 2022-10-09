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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHook;
import app.packed.container.ContainerMirror;
import app.packed.container.UserOrExtension;

/**
 *
 */
// Maybe have

// ApplicationAssemblySetup
// NonRootedAssemblySetup
// ComposerAssemblySetup

// Lige nu sker der dog for meget i ActualAssemblySetup

public abstract sealed class AssemblySetup extends RealmSetup permits ActualAssemblySetup, ComposerAssemblySetup {

    final Class<? extends Assembly> assemblyClass;

    final AssemblyModel assemblyModel;

    /**
     * All extensions that are used in the installer (if non embedded) An order set of extension according to the natural
     * extension dependency order.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    protected AssemblySetup(Class<? extends Assembly> assemblyClass) {
        this.assemblyClass = assemblyClass;
        this.assemblyModel = AssemblyModel.of(assemblyClass);
    }

    /** {@inheritDoc} */
    public final Class<? extends Assembly> assemblyClass() {
        return assemblyClass;
    }

    final void close() {
        super.close();

        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while being closed
        // In which case an Iterator might throw ConcurrentModificationException

        // Test and see if we are closing the root container
        ContainerSetup container = container();
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

    public void postBuild(ContainerConfiguration configuration) {
        assemblyModel.postBuild(configuration);
    }

    public void preBuild(ContainerConfiguration configuration) {
        assemblyModel.preBuild(configuration);
    }

    /** {@inheritDoc} */
    @Override
    public final UserOrExtension realm() {
        return UserOrExtension.application();
    }

    /** Implementation of {@link AssemblyMirror}. */
    public record BuildtimeAssemblyMirror(AssemblySetup assembly) implements AssemblyMirror {

        /** {@inheritDoc} */
        @Override
        public List<Class<? extends ContainerHook>> containerHooks() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container() {
            return assembly.container().mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends Assembly> assemblyClass() {
            return assembly.assemblyClass();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<AssemblyMirror> parent() {
            ContainerSetup org = assembly.container();
            for (ContainerSetup p = org.parent; p != null; p = p.parent) {
                if (org.userRealm != p.userRealm) {
                    return Optional.of(p.userRealm.mirror());
                }
            }
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<AssemblyMirror> children() {
            return children(assembly, assembly.container(), new ArrayList<>()).stream();
        }

        private ArrayList<AssemblyMirror> children(AssemblySetup assembly, ContainerSetup cs, ArrayList<AssemblyMirror> list) {
            if (assembly == cs.userRealm) {
                for (ContainerSetup c : cs.containerChildren) {
                    children(assembly, c, list);
                }
            } else {
                list.add(cs.userRealm.mirror());
            }
            return list;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return assembly.container().parent == null;
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return assembly.container().application.mirror();
        }
    }
}
