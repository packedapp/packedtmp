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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.DelayedHookOperator;
import app.packed.hook.MethodOperator;
import app.packed.hook.OnHook;
import app.packed.hook.OnHookAggregateBuilder;
import app.packed.inject.InjectionExtension;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import packed.internal.support.AppPackedLifecycleSupport;
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

public final class LifecycleExtension extends Extension {

    static {
        AppPackedLifecycleSupport.Helper.init(new AppPackedLifecycleSupport.Helper() {});
    }

    /**
     * This method once for each component method that is annotated with {@link Main}.
     * 
     * @param mh
     */
    @OnHook(LifecycleHookAggregator.class)
    void addMain(ComponentConfiguration cc, LifecycleHookAggregator mh) {
        mh.applyDelayed.onReady(cc, LifecycleSidecar.class, (s, r) -> r.run());
        // TODO check that we do not have multiple @Main methods
    }

    public <T> void main(Class<T> serviceKey, Consumer<? super T> consumer) {
        main(Key.of(serviceKey), consumer);
    }

    public <T> void main(Key<T> serviceKey, Consumer<? super T> consumer) {
        // invocation multiple times??? Error?
        // What if we have a @Main method? override. What about the dependencies
        // from the @Main method???
        use(InjectionExtension.class).addRequired(serviceKey);
        // How does this work implementation wise??
        // We call InjectionExtension.require(serviceKey) (Which backtraces stackwalker)
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

    public void main(Runnable r) {

    }

    /** {@inheritDoc} */
    @Override
    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(configuration(), new LifecycleSidecar());
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigured() {
        installInParentIfSameArtifact();
    }
}

class LifecycleSidecar {

}

final class LifecycleHookAggregator implements OnHookAggregateBuilder<LifecycleHookAggregator> {

    private final ArrayList<AnnotatedMethodHook<Main>> hooks = new ArrayList<>(1);
    DelayedHookOperator<Runnable> applyDelayed;

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

        applyDelayed = h.applyDelayed(MethodOperator.runnable());

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
        use(LifecycleExtension.class).main(ConcurrentHashMap.class, c -> System.out.println("size = " + c.size()));
    }
}