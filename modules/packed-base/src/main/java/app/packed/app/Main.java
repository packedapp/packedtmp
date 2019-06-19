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
import app.packed.container.ExtensionActivator;
import app.packed.container.ExtensionHookGroup;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.util.InvalidDeclarationException;
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
@ExtensionActivator(MainExtensionHookGroup.class)
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
}

/** Takes care of component methods annotated with {@link Main}. */
final class MainExtensionHookGroup extends ExtensionHookGroup<LifecycleExtension, MainExtensionHookGroup.Builder> {

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

    // Vi aktivere lifecycle extensionen her, men det er vel ogsaa fint. Eneste issue er.
    // Hvis en bundle har en Main for en eller andends skyld
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
            MethodHandle mh = hook.create();
            return (c, e) -> {
                System.out.println(mh);
                // e.addMain(mh);
            };
        }
    }
}
