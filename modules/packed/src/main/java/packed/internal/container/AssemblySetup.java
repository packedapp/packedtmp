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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.TreeSet;

import app.packed.base.Nullable;
import app.packed.component.Realm;
import app.packed.container.Assembly;
import app.packed.container.Composer;

/**
 *
 */
public abstract sealed class AssemblySetup extends RealmSetup permits AssemblyAssemblyInstaller, ComposerAssemblyInstaller {

    /**
     * All extensions that are used in the installer (if non embedded) An order set of extension according to the natural
     * extension dependency order.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    @Nullable
    final ExtensionModel model = null;

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
            ArrayList<ExtensionTreeSetup> list = new ArrayList<>(extensions.size());

            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e.extensionTree);
                e.onUserClose();
                e = extensions.pollFirst();
            }

            container.application.injectionManager.finish(container.lifetime.pool, container);

            // Close all extensions application wide
            for (ExtensionTreeSetup extension : list) {
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

    /** {@return the setup of the root container in the realm.} */
    public abstract ContainerSetup container();

    /**
     * @param lookup
     *            the lookup to use
     * @see Assembly#lookup(Lookup)
     * @see Composer#lookup(Lookup)
     */
    public void lookup(@Nullable Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        this.accessor = accessor().withLookup(lookup);
    }

    @Override
    public final Realm owner() {
        if (model == null) {
            return Realm.application();
        } else {
            return Realm.extension(model.type());
        }
    }
}
