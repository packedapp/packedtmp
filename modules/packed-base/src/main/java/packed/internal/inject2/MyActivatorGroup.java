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
package packed.internal.inject2;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerExtensionHookGroup;
import app.packed.inject.Provide2;
import packed.internal.componentcache2.Injector2Extension;

/**
 *
 */
public class MyActivatorGroup extends ContainerExtensionHookGroup<Injector2Extension, MyActivatorGroup.Builder> {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        onAnnotatedMethod(Provide2.class, (b, a) -> b.onMethod(a)); // Alternativ Annoteret metoder paa Builderen...
    }

    /** {@inheritDoc} */
    @Override
    public MyActivatorGroup.Builder newBuilder(Class<?> componentType) {
        return new MyActivatorGroup.Builder();
    }

    // /** {@inheritDoc} */
    // @Override
    // protected void configure() {
    // // validation
    // // callbacks...
    // // setup validation...
    // onAnnotatedMethod(Provide.class, (h, m) -> {
    // // validate the mother fucker....
    // });
    // }
    // /** {@inheritDoc} */
    // @Override
    // protected MyBuilder newBuilder(Class<?> componentType) {
    // return new MyBuilder();
    // }
    // static class MyBuilder implements ExtensionActivator.Builder<InjectorExtension> {
    // /** {@inheritDoc} */
    // @Override
    // public BiConsumer<ComponentConfiguration, InjectorExtension> build() {
    // return (c, e) -> e.addOptional(Key.of(String.class));
    // }
    // }

    static class Builder implements Supplier<BiConsumer<ComponentConfiguration, Injector2Extension>> {

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, Injector2Extension> get() {
            // TODO Auto-generated method stub
            return null;
        }

        public void onMethod(AnnotatedMethodHook<Provide2> a) {

        }

    }
}
