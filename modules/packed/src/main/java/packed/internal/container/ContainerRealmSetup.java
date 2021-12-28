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
import java.util.Collections;
import java.util.TreeSet;

/**
 *
 */
public abstract sealed class ContainerRealmSetup extends RealmSetup permits AssemblyRealmSetup,ComposerRealmSetup {

    // Bliver draenet hver gang vi lukker en realm
    // Til allersidst kopiere vi
    //
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    protected void close() {
        if (current != null) {
            current.onWired();
            current = null;
        }
        isClosed = true;
        for (ContainerSetup c : rootContainers) {
            c.onRealmClose();
        }
        // assert container.name != null;
    }

    protected void closeNew(ContainerSetup container) {
        if (current != null) {
            current.onWired();
            current = null;
        }
        isClosed = true;

        int extensionCount = container.extensions.size();
        ArrayList<ExtensionSetup> extensionsOrdered = new ArrayList<>(container.extensions.values());
        Collections.sort(extensionsOrdered, (c1, c2) -> -c1.model.compareTo(c2.model));

        // Close every extension
        // Okay vi haandtere ikke extensions der bliver tilfoejet mens vi lukker ordentligt
        // Tror vi behoever en live heap i ContainerRealmSetup
        for (ExtensionSetup extension : extensionsOrdered) {
            extension.onUserClose();
        }

        for (ContainerSetup c : rootContainers) {
            c.onRealmClose();
        }

        ExtensionSetup e = extensions.pollFirst();
        while (e != null) {
            e = extensions.pollFirst();
        }

        // Check if any extensions have been added while close the last realm
        if (extensionCount != container.extensions.size()) {
            extensionsOrdered = new ArrayList<>(container.extensions.values());
            Collections.sort(extensionsOrdered, (c1, c2) -> -c1.model.compareTo(c2.model));
        }

        // If root container. We close every extension
        if (container.depth == 0) {
            // Close every extension
            for (ExtensionSetup extension : extensionsOrdered) {
                extension.onClose();
                extension.realm().isClosed = true;
            }
        }

        // assert container.name != null;

    }

}
