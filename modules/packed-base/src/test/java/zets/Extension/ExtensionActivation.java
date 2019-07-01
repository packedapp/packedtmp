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
package zets.Extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ArtifactImage;
import app.packed.container.Bundle;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.hook.OnHook;

/** Tests that we can automatically activate an extension using a annotated component. */
public class ExtensionActivation {

    @Test
    public void test() {
        Bundle b = new Bundle() {
            @Override
            public void configure() {
                assertThat(extensions()).isEmpty();
                install(new MyStuff());
                assertThat(extensions()).containsExactlyInAnyOrder(ComponentExtension.class, MyExtension.class);
            }
        };
        ArtifactImage.of(b);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ContainerExtensionActivator(Builder.class)
    public @interface ActivateMyExtension {
        String value();
    }

    static class Builder extends ContainerExtensionHookProcessor<MyExtension> {

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, MyExtension> onBuild() {
            return (a, b) -> {
                b.set(a);
            };
        }

        @OnHook
        public void onMethod(AnnotatedMethodHook<ActivateMyExtension> h) throws Throwable {
            assertThat(h.annotation().value()).isEqualTo("Foo");
            assertThat(h.method().getDeclaringClass()).isSameAs(MyStuff.class);
            assertThat(h.method().getName()).isEqualTo("foo");

            assertThat(h.newMethodHandle()).isNotNull();
            if (h.method().isStatic()) {
                assertThat(h.newMethodHandle().invoke()).isEqualTo("ABC");
            } else {
                assertThat(h.newMethodHandle().invoke(new MyStuff())).isEqualTo("ABC");
            }

            // TODO test rest...
            // static vs instance method...
        }
    }

    public static class MyExtension extends ContainerExtension<MyExtension> {
        protected void set(ComponentConfiguration a) {}
    }

    static class MyStuff {

        @ActivateMyExtension("Foo")
        public String foo() {
            return "ABC";
        }
    }
}
