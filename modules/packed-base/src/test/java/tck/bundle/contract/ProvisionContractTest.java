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

import app.packed.base.ContractSet;
import app.packed.base.Key;
import app.packed.container.DefaultBundle;
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
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                use(ServiceExtension.class);
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);
        assertThat(ic).isNotNull();
        assertThat(ic).isSameAs(ServiceContract.EMPTY);
        assertThat(ic.services()).isEmpty();
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.requires()).isEmpty();
    }

    @Test
    public void provides() {
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(A.class);
                provide(B.class);
                export(A.class);
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);
        assertThat(ic.services()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.requires()).isEmpty();
    }

    @Test
    public void requires() {
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(B.class);
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);
        assertThat(ic.requires()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.services()).isEmpty();
    }

    @Test
    public void optional() {
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);
        assertThat(ic.requires()).isEmpty();
        assertThat(ic.services()).isEmpty();
        assertThat(ic.optional()).containsExactly(Key.of(A.class));
    }

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);
        assertThat(ic.requires()).containsExactly(Key.of(A.class));
        assertThat(ic.optional()).isEmpty();
        assertThat(ic.services()).isEmpty();
    }

    @Test
    public void all() {
        ContractSet c = ContractSet.contractsOf(new DefaultBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(NeedsB.class);
                export(provide(C.class));
            }
        });

        ServiceContract ic = c.use(ServiceContract.class);

        assertThat(ic.optional()).containsExactly(Key.of(A.class));
        assertThat(ic.requires()).containsExactly(Key.of(B.class));
        assertThat(ic.services()).containsExactly(Key.of(C.class));
    }
}
