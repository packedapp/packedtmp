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
package tck.bundle.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.container.BaseBundle;
import app.packed.container.BaseBundleContract;
import app.packed.inject.InjectionExtension;
import app.packed.util.Key;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.C;
import support.stubs.Letters.NeedsA;
import support.stubs.Letters.NeedsAOptional;
import support.stubs.Letters.NeedsB;

/**
 * Test {@link BaseBundleContract#services()}.
 */
public class ProvisionContractTest {

    @Test
    public void empty() {
        BaseBundleContract c = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                use(InjectionExtension.class);
            }
        });
        assertThat(c.services()).isNotNull();
        assertThat(c.services()).isSameAs(c.services());
        assertThat(c.services().services()).isEmpty();
        assertThat(c.services().optional()).isEmpty();
        assertThat(c.services().requires()).isEmpty();
    }

    @Test
    public void provides() {
        BaseBundleContract d = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(A.class);
                provide(B.class);
                export(A.class);
            }
        });
        assertThat(d.services().services()).containsExactly(Key.of(A.class));
        assertThat(d.services().optional()).isEmpty();
        assertThat(d.services().requires()).isEmpty();
    }

    @Test
    public void requires() {
        BaseBundleContract d = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(B.class);
            }
        });
        assertThat(d.services().requires()).containsExactly(Key.of(A.class));
        assertThat(d.services().optional()).isEmpty();
        assertThat(d.services().services()).isEmpty();
    }

    @Test
    public void optional() {
        BaseBundleContract d = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });
        assertThat(d.services().requires()).isEmpty();
        assertThat(d.services().services()).isEmpty();
        assertThat(d.services().optional()).containsExactly(Key.of(A.class));
    }

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        BaseBundleContract d = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });
        assertThat(d.services().requires()).containsExactly(Key.of(A.class));
        assertThat(d.services().optional()).isEmpty();
        assertThat(d.services().services()).isEmpty();
    }

    @Test
    public void all() {
        BaseBundleContract d = BaseBundleContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(NeedsB.class);
                export(provide(C.class));
            }
        });
        assertThat(d.services().optional()).containsExactly(Key.of(A.class));
        assertThat(d.services().requires()).containsExactly(Key.of(B.class));
        assertThat(d.services().services()).containsExactly(Key.of(C.class));
    }
}
