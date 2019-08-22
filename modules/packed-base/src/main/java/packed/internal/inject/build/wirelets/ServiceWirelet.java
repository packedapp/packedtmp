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
package packed.internal.inject.build.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.container.Wirelet;
import app.packed.util.Key;
import packed.internal.inject.build.ImportedInjector;

/**
 *
 */
public abstract class ServiceWirelet extends Wirelet {

    public void apply(ImportedInjector ii) {
        throw new UnsupportedOperationException();
    }

    public static class FilterOnKey extends ServiceWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        public void apply(ImportedInjector ii) {
            for (Key<?> key : set) {
                ii.entries.remove(key);
            }
        }
    }
}
// final HashMap<Key<?>, BSEImported<?>> processWirelet(Wirelet wirelet, HashMap<Key<?>, BSEImported<?>> nodes) {
// // if (true) {
// // throw new Error();
// // }
// // ImportExportDescriptor ied = ImportExportDescriptor.from(null /*
// // AppPackedBundleSupport.invoke().lookupFromWireOperation(stage) */, stage.getClass());
// //
// // for (AtProvides m : ied.provides.members.values()) {
// // for (InternalDependencyDescriptor s : m.dependencies) {
// // if (!nodes.containsKey(s.key())) {
// // throw new InjectionException("not good man, " + s.key() + " is not in the set of incoming services");
// // }
// // }
// // }
//
// // Make runtime nodes....
//
// HashMap<Key<?>, BSEImported<?>> newNodes = new HashMap<>();
//
// for (Iterator<BSEImported<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
// BSEImported<?> node = iterator.next();
// Key<?> existing = node.key();
//
// // invoke the import function on the stage
// // if (stage instanceof InternalServiceWirelets) {
// // ((InternalServiceWirelets) stage).onEachService(node);
// // }
//
// if (node.key() == null) {
// iterator.remove();
// } else if (!node.key().equals(existing)) {
// iterator.remove();
// // TODO check if a node is already present
// newNodes.put(node.key(), node); // Should make new, with new configuration site
// }
// }
// // Put all remaining nodes in newNodes;
// newNodes.putAll(nodes);
// return newNodes;
// }