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
package tests.inject.bindinjector.assembly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.inject.Factory0;
import app.packed.inject.service.ServiceLocator;
import packed.internal.inject.service.InjectorComposer;
import testutil.assertj.Assertions;

/**
 *
 */
public class InjectorBindAssemblyTest {

    /** Tests various null arguments. */
    @Test
    public void nullArguments() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {}
        };

        Assertions.npe(() -> InjectorComposer.configure2(c -> c.link((BaseAssembly) null)), "assembly");
        Assertions.npe(() -> InjectorComposer.configure2(c -> c.link(b, (Wirelet[]) null)), "wirelets");
    }

    /** Tests that we can import no services. */
    @Test
    public void cannotImportNonExposed() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {
                provideInstance("X");
            }
        };

        ServiceLocator i = InjectorComposer.configure2(c -> {
            c.link(b);
        });
        assertThat(i.keys().size()).isEqualTo(0L);
    }

    /** Tests that we can import no services. */
    @Test
    @Disabled // Link
    public void OneImport() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {
                provideInstance("X").export();
            }
        };

        ServiceLocator i = InjectorComposer.configure2(c -> {
            c.link(b);
        });
        assertThat(i.use(String.class)).isEqualTo("X");
    }

    /** Tests that we can import no services. */
    @Test
    @Disabled
    public void protoTypeImport() {
        AtomicLong al = new AtomicLong();
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {
                providePrototype(new Factory0<Long>(al::incrementAndGet) {}).export();
            }
        };

        ServiceLocator i = InjectorComposer.configure2(c -> {
            c.link(b);
        });
        assertThat(i.use(Long.class)).isEqualTo(1L);
        assertThat(i.use(Long.class)).isEqualTo(2L);
        assertThat(i.use(Long.class)).isEqualTo(3L);
    }
}
