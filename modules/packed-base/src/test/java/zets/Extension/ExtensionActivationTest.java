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

import app.packed.app.App;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionActivator;
import app.packed.container.ExtensionHookProcessor;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import zets.name.spi.AbstractArtifactTest;

/** Tests that we can automatically activate an extension using a annotated field or method. */
public class ExtensionActivationTest extends AbstractArtifactTest {

    @Test
    public void instanceMethod() {
        App.of(new Bundle() {
            @Override
            public void configure() {
                assertThat(extensions()).isEmpty();
                WithMethodInstance.invoked = false;
                install(new WithMethodInstance());
                assertThat(WithMethodInstance.invoked).isTrue();
                assertThat(extensions()).containsExactlyInAnyOrder(ComponentExtension.class, MyExtension.class);
            }
        });
    }

    @Test
    public void staticField() {
        App.of(new Bundle() {
            @Override
            public void configure() {
                assertThat(extensions()).isEmpty();
                install(new WithFieldStatic());
                assertThat(extensions()).containsExactlyInAnyOrder(ComponentExtension.class, MyExtension.class);
            }
        });
    }

    @Test
    public void instanceField() {
        App.of(new Bundle() {
            @Override
            public void configure() {
                assertThat(extensions()).isEmpty();
                install(new WithFieldInstance());
                assertThat(extensions()).containsExactlyInAnyOrder(ComponentExtension.class, MyExtension.class);
            }
        });
    }

    @Test
    public void staticMethod() {
        App.of(new Bundle() {
            @Override
            public void configure() {
                assertThat(extensions()).isEmpty();
                WithMethodStatic.invoked = false;
                install(new WithMethodStatic());
                assertThat(WithMethodStatic.invoked).isTrue();
                assertThat(extensions()).containsExactlyInAnyOrder(ComponentExtension.class, MyExtension.class);
            }
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.FIELD })
    @ExtensionActivator(Builder.class)
    public @interface ActivateMyExtension {
        String value();
    }

    static class Builder extends ExtensionHookProcessor<MyExtension> {

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, MyExtension> onBuild() {
            return (a, b) -> {
                b.set(a);
            };
        }

        @OnHook
        public void onField(AnnotatedFieldHook<ActivateMyExtension> h) throws Throwable {
            assertThat(h.annotation().value()).isEqualTo("Foo");
            assertThat(h.field().getName()).isEqualTo("foo");
            assertThat(h.newVarHandle()).isNotNull();
            if (h.field().isStatic()) {
                assertThat(h.field().getDeclaringClass()).isSameAs(WithFieldStatic.class);
                assertThat(h.newVarHandle().get()).isEqualTo("ABC");
            } else {
                assertThat(h.field().getDeclaringClass()).isSameAs(WithFieldInstance.class);
                assertThat(h.newVarHandle().get(new WithFieldInstance())).isEqualTo("ABC");
            }
        }

        @OnHook
        public void onMethod(AnnotatedMethodHook<ActivateMyExtension> h) throws Throwable {
            assertThat(h.annotation().value()).isEqualTo("Foo");
            assertThat(h.method().getName()).isEqualTo("foo");
            assertThat(h.newMethodHandle()).isNotNull();
            if (h.method().isStatic()) {
                assertThat(h.method().getDeclaringClass()).isSameAs(WithMethodStatic.class);
                assertThat(h.newMethodHandle().invoke()).isEqualTo("ABC");
            } else {
                assertThat(h.method().getDeclaringClass()).isSameAs(WithMethodInstance.class);
                assertThat(h.newMethodHandle().invoke(new WithMethodInstance())).isEqualTo("ABC");
            }
        }
    }

    public static class MyExtension extends Extension {
        protected void set(ComponentConfiguration a) {}
    }

    public static class WithFieldInstance {

        @ActivateMyExtension("Foo")
        public String foo = "ABC";

    }

    public static class WithFieldStatic {

        @ActivateMyExtension("Foo")
        public static String foo = "ABC";
    }

    private static class WithMethodInstance {
        static boolean invoked;

        @ActivateMyExtension("Foo")
        public String foo() {
            invoked = true;
            return "ABC";
        }
    }

    private static class WithMethodStatic {
        static boolean invoked;

        @ActivateMyExtension("Foo")
        public static String foo() {
            invoked = true;
            return "ABC";
        }
    }
}
