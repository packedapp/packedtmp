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
package app.packed.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtensionHookGroup;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.hook.OnHook;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.util.InvalidDeclarationException;
import packed.internal.container.PackedContainer;
import packed.internal.support.AppPackedLifecycleSupport;
import packed.internal.util.StringFormatter;

/**
 * A application can have a single main entry point which is the first instructions in a program that is executed, Must
 * be placed on a method on a bundle. Why not individual components??
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// @InterruptOnStop
// https://en.wikipedia.org/wiki/Entry_point

// Main kan vel ogsaa bruges paa en injector???? Nahhh, hvordan styre vi det???
// Maaske bare at alle annoteringer + extensions, udover Provide+Inject fejler???

// Move main to app.packed.lifecycle ??? I think it has a lot to do with lifecycle....
// Because it is actually important that people understand the model....
// It is really heavily related to App actually because, you cannot have a Main for a Container
// Only a main for an App.
// Furthermore we also want to put cli here...
@ContainerExtensionActivator(MainExtensionHookGroup.class)
public @interface Main {

    /**
     * Whether or not the application should be shutdown when this method completes. The default value is true.
     * 
     * @return or not the application should be shutdown when this method completes. The default value is true
     */
    // Syntes ikke den er god, hvad hvis man kalder run()....
    // Shutdown on Success instead? Always shutdown on Failure I guess
    boolean shutdownOnCompletion() default true; // Taenker hellere det maa vaere noget @OnRunning()...

    boolean overridable() default true;

    //// Nice performance measurement. Keep installing noop
    // ContainerImages, with undeploy
    boolean undeployOnCompletion() default true;
}

/** Takes care of component methods annotated with {@link Main}. */
final class MainExtensionHookGroup extends ContainerExtensionHookGroup<LifecycleExtension, MainExtensionHookGroup.Builder> {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        onAnnotatedMethod(Main.class, (b, m) -> b.add(m));
    }

    /** {@inheritDoc} */
    @Override
    public Builder newBuilder(Class<?> componentType) {
        return new Builder();
    }

    static class Builder implements Supplier<BiConsumer<ComponentConfiguration, LifecycleExtension>> {

        private AnnotatedMethodHook<Main> hook;

        private void add(AnnotatedMethodHook<Main> hook) {
            if (this.hook != null) {
                throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(hook.method().getDeclaringClass())
                        + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                        + StringFormatter.formatShortWithParameters(this.hook.method()) + ", " + StringFormatter.formatShortWithParameters(hook.method()));
            }
            this.hook = hook;
        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, LifecycleExtension> get() {
            MethodHandle mh = hook.newMethodHandle();
            hook.onMethodReady(PackedContainer.class, (a, b) -> {
                b.run();
            });

            // Vi skal bruge denne her fordi, vi bliver noedt til at checke at vi ikke har 2 komponenter med @main
            return (c, e) -> AppPackedLifecycleSupport.invoke().doConfigure(e, mh);
        }
    }
}

class Builder implements ContainerExtensionHookProcessor<LifecycleExtension> {

    private AnnotatedMethodHook<Main> hook;

    @OnHook
    void add(AnnotatedMethodHook<Main> hook) {
        // Replace with a List I think... Add hen just add all. And check in onBuild. We can even initialize it to new
        // ArrayList(1) because
        // If this method is called, it means that at least one main method exists....
        if (this.hook != null) {
            throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(hook.method().getDeclaringClass())
                    + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                    + StringFormatter.formatShortWithParameters(this.hook.method()) + ", " + StringFormatter.formatShortWithParameters(hook.method()));
        }
        this.hook = hook;
    }

    /** {@inheritDoc} */
    @Override
    public BiConsumer<ComponentConfiguration, LifecycleExtension> onBuild() {
        MethodHandle mh = hook.newMethodHandle();
        hook.onMethodReady(PackedContainer.class, (a, b) -> {
            b.run();
        });

        // Vi skal bruge denne her fordi, vi bliver noedt til at checke at vi ikke har 2 komponenter med @main
        return (c, e) -> AppPackedLifecycleSupport.invoke().doConfigure(e, mh);
    }
}