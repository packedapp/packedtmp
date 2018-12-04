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
package tests.injector;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.inject.BindingMode;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import support.stubs.annotation.StringQualifier;

/**
 *
 */
public class InjectorConfigurationProvidesTest {

    Injector of(Consumer<? super InjectorConfiguration> consumer) {
        return Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    @Test
    public void tags() {

    }

    @Test
    public void description() {
        class WithDescription {

            @Provides(description = "niceField", bindingMode = BindingMode.PROTOTYPE)
            public final Long F = 0L;

            @Provides(description = "niceMethod", bindingMode = BindingMode.PROTOTYPE)
            public int m() {
                return 0;
            }
        }
        Injector i = of(c -> c.bind(new WithDescription()));
        assertThat(i.getService(Long.class).getDescription()).isEqualTo("niceField");
        assertThat(i.getService(Integer.class).getDescription()).isEqualTo("niceMethod");
    }

    @Test
    public void members() {
        validate(of(c -> c.bind(new VisibilityStatic())));
        validate(of(c -> c.bind(VisibilityStatic.class)));
        validate(of(c -> c.bindLazy(VisibilityStatic.class)));
        validate(of(c -> c.bindPrototype(VisibilityStatic.class)));
    }

    private static void validate(Injector i) {
        assertThat(i.with(new Key<@StringQualifier("f_package") String>() {})).isEqualTo("package_f");
        assertThat(i.with(new Key<@StringQualifier("f_private") String>() {})).isEqualTo("private_f");
        assertThat(i.with(new Key<@StringQualifier("f_protected") String>() {})).isEqualTo("protected_f");
        assertThat(i.with(new Key<@StringQualifier("f_public") String>() {})).isEqualTo("public_f");

        assertThat(i.with(new Key<@StringQualifier("m_package") String>() {})).isEqualTo("package_m");
        assertThat(i.with(new Key<@StringQualifier("m_public") String>() {})).isEqualTo("public_m");
        assertThat(i.with(new Key<@StringQualifier("m_protected") String>() {})).isEqualTo("protected_m");
        assertThat(i.with(new Key<@StringQualifier("m_private") String>() {})).isEqualTo("private_m");
    }

    static class VisibilityStatic {

        @Provides
        @StringQualifier("f_package")
        static final String F_PACKAGE = "package_f";

        @Provides
        @StringQualifier("f_private")
        private static final String F_PRIVATE = "private_f";

        @Provides
        @StringQualifier("f_protected")
        protected static final String F_PROTECTED = "protected_f";

        @Provides
        @StringQualifier("f_public")
        public static final String F_PUBLIC = "public_f";

        @Provides
        @StringQualifier("m_package")
        static String m_package() {
            return "package_m";
        }

        @Provides
        @StringQualifier("m_private")
        private static String m_private() {
            return "private_m";
        }

        @Provides
        @StringQualifier("m_protected")
        protected static String m_protected() {
            return "protected_m";
        }

        @Provides
        @StringQualifier("m_public")
        public static String m_public() {
            return "public_m";
        }
    }

    static class VisibilityNonStatic {

        @Provides
        @StringQualifier("f_package")
        final String F_PACKAGE = "package_f";

        @Provides
        @StringQualifier("f_private")
        private final String F_PRIVATE = "private_f";

        @Provides
        @StringQualifier("f_protected")
        protected final String F_PROTECTED = "protected_f";

        @Provides
        @StringQualifier("f_public")
        public final String F_PUBLIC = "public_f";

        @Provides
        @StringQualifier("m_package")
        String m_package() {
            return "package_m";
        }

        @Provides
        @StringQualifier("m_private")
        private String m_private() {
            return "private_m";
        }

        @Provides
        @StringQualifier("m_protected")
        protected String m_protected() {
            return "protected_m";
        }

        @Provides
        @StringQualifier("m_public")
        public String m_public() {
            return "public_m";
        }
    }
}
