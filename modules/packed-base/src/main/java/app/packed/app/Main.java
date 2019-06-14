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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.ExtensionActivator;
import app.packed.container.ExtensionHookGroup;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.MethodDescriptor;
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
final class MainExtensionHookGroup extends ExtensionHookGroup<ComponentExtension, MainExtensionHookGroup.Builder> {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        // b.forInjection
        onAnnotatedMethodDescription(Main.class, (b, m) -> b.add(m));
    }

    /** {@inheritDoc} */
    @Override
    public Builder newBuilder(Class<?> componentType) {
        return new Builder();
    }

    static class Builder implements Supplier<BiConsumer<ComponentConfiguration, ComponentExtension>> {

        MethodDescriptor method;

        private void add(MethodDescriptor method) {
            if (this.method != null) {
                throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(method.getDeclaringClass())
                        + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                        + StringFormatter.formatShortWithParameters(this.method) + ", " + StringFormatter.formatShortWithParameters(method));
            }
            this.method = method;
        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, ComponentExtension> get() {
            // Use Support class to invoke stuff on ComponentExtension...
            return (c, e) -> {};
        }
    }
}
