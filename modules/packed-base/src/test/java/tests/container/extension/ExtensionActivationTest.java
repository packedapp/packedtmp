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
package tests.container.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.component.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import packed.internal.bundle.Packlet;
import testutil.util.AbstractArtifactTest;

/** Tests that we can automatically activate an extension using a annotated field or method. */
@Disabled // uses Packlets
public class ExtensionActivationTest extends AbstractArtifactTest {

    @Test
    public void instanceMethod() {
        App.of(new BaseAssembly() {
            @Override
            public void build() {
                assertThat(extensions()).isEmpty();
                WithMethodInstance.invoked = false;
                installInstance(new WithMethodInstance());
                assertThat(WithMethodInstance.invoked).isTrue();
                assertThat(extensions()).containsExactlyInAnyOrder(MyExtension.class);
            }
        });
    }

    @Test
    public void staticField() {
        App.of(new BaseAssembly() {
            @Override
            public void build() {
                assertThat(extensions()).isEmpty();
                installInstance(new WithFieldStatic());
                assertThat(extensions()).containsExactlyInAnyOrder(MyExtension.class);
            }
        });
    }

    @Test
    public void instanceField() {
        App.of(new BaseAssembly() {
            @Override
            public void build() {
                assertThat(extensions()).isEmpty();
                installInstance(new WithFieldInstance());
                assertThat(extensions()).containsExactlyInAnyOrder(MyExtension.class);
            }
        });
    }

    @Test
    public void staticMethod() {
        App.of(new BaseAssembly() {
            @Override
            public void build() {
                assertThat(extensions()).isEmpty();
                WithMethodStatic.invoked = false;
                installInstance(new WithMethodStatic());
                assertThat(WithMethodStatic.invoked).isTrue();
                assertThat(extensions()).containsExactlyInAnyOrder(MyExtension.class);
            }
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.FIELD })
    @Packlet(extension = MyExtension.class)
    public @interface ActivateMyExtension {
        String value();
    }

//    static class Foo implements Hook {
//        final String s;
//
//        Foo(String s) {
//            this.s = s;
//        }
//
//        static class Builder implements Hook.Builder<Foo> {
//
//            /** {@inheritDoc} */
//            @Override
//            public Foo build() {
//                return new Foo("ffooo");
//            }
//
//            @OnHook
//            public void onField(AnnotatedFieldHook<ActivateMyExtension> h) throws Throwable {
//                assertThat(h.annotation().value()).isEqualTo("Foo");
//                assertThat(h.field().getName()).isEqualTo("foo");
//                assertThat(h.varHandle()).isNotNull();
//                if (h.field().isStatic()) {
//                    assertThat(h.field().getDeclaringClass()).isSameAs(WithFieldStatic.class);
//                    assertThat(h.varHandle().get()).isEqualTo("ABC");
//                } else {
//                    assertThat(h.field().getDeclaringClass()).isSameAs(WithFieldInstance.class);
//                    assertThat(h.varHandle().get(new WithFieldInstance())).isEqualTo("ABC");
//                }
//            }
//
//            @OnHook
//            public void onMethod(AnnotatedMethodHook<ActivateMyExtension> h) throws Throwable {
//                assertThat(h.annotation().value()).isEqualTo("Foo");
//                assertThat(h.method().getName()).isEqualTo("foo");
//                assertThat(h.methodHandle()).isNotNull();
//                if (h.method().isStatic()) {
//                    assertThat(h.method().getDeclaringClass()).isSameAs(WithMethodStatic.class);
//                    assertThat(h.methodHandle().invoke()).isEqualTo("ABC");
//                } else {
//                    assertThat(h.method().getDeclaringClass()).isSameAs(WithMethodInstance.class);
//                    assertThat(h.methodHandle().invoke(new WithMethodInstance())).isEqualTo("ABC");
//                }
//            }
//        }
//    }

    public static final class MyExtension extends Extension {

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
