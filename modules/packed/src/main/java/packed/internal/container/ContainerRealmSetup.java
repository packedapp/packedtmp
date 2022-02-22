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

import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 */
public abstract sealed class ContainerRealmSetup extends RealmSetup permits AssemblyRealmSetup,ComposerRealmSetup {

    /** An order set of extension according to the natural extension dependency order. */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    void closeRealm() {
        ContainerSetup container = container();
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
        isClosed = true;

        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while closing
        // In which we need to process them as well in the right order as determined
        // by the order in the Treeset.
        
        if (container.parent != null) {
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                e.onUserClose();
                e = extensions.pollFirst();
            }
        } else {
            // If we are the root assembly we also need to call extension.onClose()
            
            ArrayList<ExtensionSetup> list = new ArrayList<>(extensions.size());
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e);
                e.onUserClose();
                e = extensions.pollFirst();
            }
            for (ExtensionSetup extension : list) {
                extension.onClose();
                extension.realm().isClosed = true;
            }
        }
    }

    /** {@return the setup of the root container in the realm.} */
    public abstract ContainerSetup container();
}
