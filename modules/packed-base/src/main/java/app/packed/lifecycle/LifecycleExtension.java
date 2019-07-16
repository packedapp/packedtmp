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
package app.packed.lifecycle;

import java.lang.invoke.MethodHandle;
import java.util.HashSet;
import java.util.Set;

import app.packed.container.Extension;
import app.packed.entrypoint.Main;
import packed.internal.support.AppPackedLifecycleSupport;

/**
 * An extension
 */

// Configuring lifecycle for the container,
//// Component lifecycle is hmmmmm
public final class LifecycleExtension extends Extension {

    static {
        AppPackedLifecycleSupport.Helper.init(new AppPackedLifecycleSupport.Helper() {

            @Override
            public void doConfigure(LifecycleExtension extension, MethodHandle mh) {
                extension.addMain(mh);
            }
        });
    }

    // set
    // @Override
    // protected void onWireChild(@Nullable LifecycleExtension child, O link) {
    //
    // }
    //
    // @Override
    // protected void onWireParent(@Nullable LifecycleExtension parent, BundleLink link) {
    // if (parent == null) {
    // throw new /* WiringException */ RuntimeException("Cannot wiring to a bundle that does not support lifecycle...");
    // }
    // }

    // protected void supporting

    // void addAlias(Class<?>

    // Perhaps adding some custom annotations

    // ClassValue<Feature> -> IdentityHashMap<Feature, CachedConfiguration>>

    private Set<MethodHandle> s = new HashSet<>();

    /**
     * This method once for each component method that is annotated with {@link Main}.
     * 
     * @param mh
     */
    private void addMain(MethodHandle mh) {
        // TODO check that we do not have multiple @Main methods
        System.out.println(mh);
        s.add(mh);
    }

}
