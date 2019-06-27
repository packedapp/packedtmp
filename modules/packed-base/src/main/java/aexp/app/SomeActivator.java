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
package aexp.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ExtensionHookGroup;
import app.packed.util.MethodDescriptor;

/**
 *
 */

// Kunne vaere fedt at paa en eller anden maade.. At kunne koble den samme med et context object....
public class SomeActivator extends ExtensionHookGroup<SomeExtension, SomeActivator.Builder> {

    SomeActivator() {
        System.out.println("Created");
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        super.onAnnotatedMethodDescription(SomeAnnotation.class, (b, a) -> {
            b.list.add(a);
        });
    }

    /** {@inheritDoc} */
    @Override
    public Builder newBuilder(Class<?> componentType) {
        return new Builder();
    }

    static class Builder implements Supplier<BiConsumer<ComponentConfiguration, SomeExtension>> {

        List<MethodDescriptor> list = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, SomeExtension> get() {
            List<MethodDescriptor> l = List.copyOf(list);
            return (c, e) -> e.methods(c, l);
        }

    }
}
