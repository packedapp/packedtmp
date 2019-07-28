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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.inject.InjectionExtension;
import app.packed.util.Key;
import packed.internal.support.AppPackedLifecycleSupport;

/**
 * An extension that enables lifecycle management of components via annotations such as {@link OnStart} and
 * {@link OnStop}.
 * 
 */

// Configuring lifecycle for the container,
//// Component lifecycle is hmmmmm

// Possibilities
// No Main/Cli -> Daemon, will run until shutdown/terminated
// Main -> Will run until main method exists in some way...
// Cli -> Will run one of the cli endpoints, which can either be daemons, or run til end.

// Do we require lifecycle extension???? Nahhh

// Implementation.. look and see if EntryPointExtension exists... and run it...
// Otherwise you are a daemon

// Other Stuff
//// Handling of Main exceptions??? Naah ErrorHandlingExtension
//// What if result?????? have some of Container methods return Object???

// Smide denne ind under LifecycleExtension

public final class LifecycleExtension extends Extension {

    public void main(Runnable r) {

    }

    public <T> void main(Key<T> serviceKey, Consumer<? super T> consumer) {
        // invocation multiple times??? Error?
        // What if we have a @Main method? override. What about the dependencies
        // from the @Main method???
        use(InjectionExtension.class).addRequired(serviceKey);
        // How does this work implementation wise??
        // We call InjectionExtension.require(serviceKey) (Which backtraces stackwalker)
    }

    public <T> void main(Class<T> serviceKey, Consumer<? super T> consumer) {
        main(Key.of(serviceKey), consumer);
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigured() {
        installInParentIfSameArtifact();
    }

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
        System.out.println(s);
    }

}

class X extends Bundle {

    @Override
    public void configure() {
        use(LifecycleExtension.class).main(ConcurrentHashMap.class, c -> System.out.println("size = " + c.size()));
    }
}

// @Map(PicoClicc. EntryPointExtension)
// ....
@interface UsesPicoCli {}