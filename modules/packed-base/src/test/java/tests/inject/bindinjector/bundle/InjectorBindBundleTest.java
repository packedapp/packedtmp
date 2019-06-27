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
package tests.inject.bindinjector.bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static support.assertj.Assertions.npe;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import app.packed.inject.Factory0;
import app.packed.inject.Injector;

/**
 *
 */
public class InjectorBindBundleTest {

    /** Tests various null arguments. */
    @Test
    public void nullArguments() {
        Bundle b = new Bundle() {
            @Override
            protected void configure() {}
        };

        npe(() -> Injector.configure(c -> c.link((Bundle) null)), "bundle");
        npe(() -> Injector.configure(c -> c.link(b, (Wirelet[]) null)), "wirelets");
    }

    /** Tests that we can import no services. */
    @Test
    @Disabled
    public void cannotImportNonExposed() {
        Bundle b = new Bundle() {
            @Override
            protected void configure() {
                provide("X");
            }
        };

        Injector i = Injector.configure(c -> {
            c.link(b);
        });
        assertThat(i.services().count()).isEqualTo(0L);
    }

    /** Tests that we can import no services. */
    @Test
    @Disabled // because of refactoring
    public void OneImport() {
        Bundle b = new Bundle() {
            @Override
            protected void configure() {
                provide("X");
                export(String.class);
            }
        };

        Injector i = Injector.configure(c -> {
            c.link(b);
        });
        assertThat(i.use(String.class)).isEqualTo("X");
    }

    /** Tests that we can import no services. */
    @Test
    @Disabled // because of refactoring
    public void protoTypeImport() {
        AtomicLong al = new AtomicLong();
        Bundle b = new Bundle() {
            @Override
            protected void configure() {
                provide(new Factory0<>(al::incrementAndGet) {}).prototype();
                export(Long.class);
            }
        };

        Injector i = Injector.configure(c -> {
            c.link(b);
        });
        assertThat(i.use(Long.class)).isEqualTo(1L);
        assertThat(i.use(Long.class)).isEqualTo(2L);
        assertThat(i.use(Long.class)).isEqualTo(3L);
    }
}
