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

import app.packed.base.Key;
import app.packed.container.BaseBundle;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.Letters.C;
import testutil.stubs.Letters.NeedsA;
import testutil.stubs.Letters.NeedsAOptional;
import testutil.stubs.Letters.NeedsB;

/**
 * 
 */
public class ProvisionContractTest {

    @Test
    public void empty() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                use(ServiceExtension.class);
            }
        });

        assertThat(ic).isNotNull();
        assertThat(ic).isSameAs(ServiceContract.EMPTY);
        assertThat(ic.provides()).isEmpty();
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.requires()).isEmpty();
    }

    @Test
    public void provides() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(A.class);
                provide(B.class);
                export(A.class);
            }
        });

        assertThat(ic.provides()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.requires()).isEmpty();
    }

    @Test
    public void requires() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(B.class);
            }
        });

        assertThat(ic.requires()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.provides()).isEmpty();
    }

    @Test
    public void optional() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });

        assertThat(ic.requires()).isEmpty();
        assertThat(ic.provides()).isEmpty();
        assertThat(ic.optional()).containsExactly(Key.of(A.class));
    }

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });

        assertThat(ic.requires()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.provides()).isEmpty();
    }

    @Test
    public void all() {
        ServiceContract ic = ServiceContract.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(NeedsB.class);
                provide(C.class).export();
            }
        });

        assertThat(ic.optional()).containsExactly(Key.of(A.class));
        assertThat(ic.requires()).containsExactly(Key.of(B.class));
        assertThat(ic.provides()).containsExactly(Key.of(C.class));
    }
}
