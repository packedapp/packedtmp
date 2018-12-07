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

import org.junit.jupiter.api.Test;

import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorImportStage;
import app.packed.inject.Factory0;
import app.packed.inject.Injector;

/**
 *
 */
public class InjectorBindBundleTest {

    /** Tests various null arguments. */
    @Test
    public void nullArguments() {
        InjectorBundle b = new InjectorBundle() {
            @Override
            protected void configure() {}
        };

        npe(() -> Injector.of(c -> c.injectorBind((InjectorBundle) null)), "bundle");
        npe(() -> Injector.of(c -> c.injectorBind(b, (InjectorImportStage[]) null)), "stages");
    }

    /** Tests that we can import no services. */
    @Test
    public void cannotImportNonExposed() {
        InjectorBundle b = new InjectorBundle() {
            @Override
            protected void configure() {
                bind("X");
            }
        };

        Injector i = Injector.of(c -> {
            c.injectorBind(b);
        });
        assertThat(i.services().count()).isEqualTo(0L);
    }

    /** Tests that we can import no services. */
    @Test
    public void OneImport() {
        InjectorBundle b = new InjectorBundle() {
            @Override
            protected void configure() {
                bind("X");
                expose(String.class);
            }
        };

        Injector i = Injector.of(c -> {
            c.injectorBind(b);
        });
        assertThat(i.with(String.class)).isEqualTo("X");
    }

    /** Tests that we can import no services. */
    @Test
    public void protoTypeImport() {
        AtomicLong al = new AtomicLong();
        InjectorBundle b = new InjectorBundle() {
            @Override
            protected void configure() {
                bindPrototype(new Factory0<>(al::incrementAndGet) {});
                expose(Long.class);
            }
        };

        Injector i = Injector.of(c -> {
            c.injectorBind(b);
        });
        assertThat(i.with(Long.class)).isEqualTo(1L);
        assertThat(i.with(Long.class)).isEqualTo(2L);
        assertThat(i.with(Long.class)).isEqualTo(3L);
    }
}
