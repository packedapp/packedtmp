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
package app.packed.entrypoint;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import app.packed.component.SingletonConfiguration;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.HookApplicator;
import app.packed.hook.OnHook;
import app.packed.lang.InvalidDeclarationException;
import app.packed.lang.Key;
import app.packed.lang.reflect.MethodOperator;
import app.packed.lifecycle.OnStart;
import app.packed.lifecycle.OnStop;
import app.packed.service.ServiceExtension;
import packed.internal.util.StringFormatter;

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

public final class EntryPointExtension extends Extension {

    public <T> void main(Class<T> serviceKey, Consumer<? super T> consumer) {
        main(Key.of(serviceKey), consumer);
    }

    // Vi vil sgu gerne bare smide any exception we will...
    public <T> void main(Key<T> serviceKey, Consumer<? super T> consumer) {
        // invocation multiple times??? Error?
        // What if we have a @Main method? override. What about the dependencies
        // from the @Main method???
        use(ServiceExtension.class).require(serviceKey);
        // How does this work implementation wise??
        // We call InjectionExtension.require(serviceKey) (Which backtraces stackwalker)
    }

    // protected void supporting

    // void addAlias(Class<?>

    // Perhaps adding some custom annotations

    // ClassValue<Feature> -> IdentityHashMap<Feature, CachedConfiguration>>

    public void main(Runnable r) {

    }

    /**
     * This method once for each component method that is annotated with {@link Main}.
     * 
     * @param mh
     */
    void addMain(SingletonConfiguration<?> cc, LifecycleHookAggregator mh) {
        mh.applyDelayed.onReady(cc, LifecycleSidecar.class, (s, r) -> r.run());
        // TODO check that we do not have multiple @Main methods
    }

    static class Composer extends ExtensionComposer<EntryPointExtension> {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            // onInstantiation((e, c) -> c.put(new LifecycleSidecar()));
        }
    }
}

class LifecycleSidecar {

}

final class LifecycleHookAggregator implements Hook.Builder<LifecycleHookAggregator>, Hook {

    private final ArrayList<AnnotatedMethodHook<Main>> hooks = new ArrayList<>(1);
    HookApplicator<Runnable> applyDelayed;

    @OnHook
    void add(AnnotatedMethodHook<Main> hook) {
        hooks.add(hook);
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleHookAggregator build() {
        if (hooks.size() > 1) {
            throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(hooks.get(0).method().getDeclaringClass())
                    + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                    + StringFormatter.formatShortWithParameters(hooks.get(0).method()) + ", "
                    + StringFormatter.formatShortWithParameters(hooks.get(1).method()));
        }
        AnnotatedMethodHook<Main> h = hooks.get(0);

        applyDelayed = h.applicator(MethodOperator.runnable());

        // Vi skal bruge denne her fordi, vi bliver noedt til at checke at vi ikke har 2 komponenter med @main
        return this;
    }
}

// @Map(PicoClicc. EntryPointExtension)
// ....
@interface UsesPicoCli {}

class X extends BaseBundle {

    @Override
    public void configure() {
        lifecycle().main(ConcurrentHashMap.class, c -> System.out.println("size = " + c.size()));
    }
}