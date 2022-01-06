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

    // Bliver draenet hver gang vi lukker en realm
    // Til allersidst kopiere vi
    //

    /** An order set of extension according to the natural extension dependency order. */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    public abstract ContainerSetup container();

    void closeRealm() {
        ContainerSetup container = container();
        if (current != null) {
            current.onWired();
            current = null;
        }
        isClosed = true;

        if (container.parent != null) {
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                e.onUserClose();
                e = extensions.pollFirst();
            }
        } else {
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

        onRealmClose(container);
    }

    private void onRealmClose(ContainerSetup cs) {
        // We recursively close all children in the same realm first
        // We do not close individual components
        if (cs.containerChildren != null) {
            for (ContainerSetup c : cs.containerChildren) {
                if (c.realm == cs.realm) {
                    onRealmClose(c);
                }
            }
        }
        // Complete all extensions in order
        // Vil faktisk mene det skal vaere den modsatte order...
        // Tror vi skal have vendt comparatoren

        // Close every extension
//        for (ExtensionSetup extension : extensionsOrdered) {
//     //       extension.onComplete();
//        }

        cs.beans.resolve();
    }

}
