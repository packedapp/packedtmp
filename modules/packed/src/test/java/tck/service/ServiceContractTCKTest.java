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
package tck.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.bundle.BaseAssembly;
import app.packed.bundle.Bundle;
import app.packed.inject.service.ServiceContract;
import app.packed.inject.service.ServiceExtension;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.Letters.C;
import testutil.stubs.Letters.NeedsA;
import testutil.stubs.Letters.NeedsAOptional;
import testutil.stubs.Letters.NeedsB;

/**
 *
 */
public class ServiceContractTCKTest {

    /** Tests that we return an empty contract even if we do not use {@link ServiceExtension}. */
    @Test
    public void empty1() {
        check(ServiceContract.EMPTY, new BaseAssembly() {
            @Override
            protected void build() {}
        });
    }

    /** Tests that we return an empty contract. */
    @Test
    public void empty2() {
        check(ServiceContract.EMPTY, new BaseAssembly() {
            @Override
            protected void build() {
                use(ServiceExtension.class);
            }
        });
    }

    /** Tests that services that are not exported are not included. */
    @Test
    public void empty3() {
        check(ServiceContract.EMPTY, new BaseAssembly() {
            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(A.class);
            }
        });
    }

    /** Tests that exported services are part of the contract. */
    @Test
    public void provides() {
        ServiceContract expected = ServiceContract.build(b -> b.provides(A.class));
        check(expected, new BaseAssembly() {
            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(A.class);
                provide(B.class);
                export(A.class);
            }
        });
    }

    /** Checks that registering a service */
    @Test
    public void requires() {
        ServiceContract expected = ServiceContract.build(b -> b.requires(A.class));
        check(expected, new BaseAssembly() {
            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class); // TODO fix, this should work for install
                provide(B.class);
            }
        });
    }

    @Test
    public void optional() {
        ServiceContract expected = ServiceContract.build(b -> b.optional(A.class));
        check(expected, new BaseAssembly() {
            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });
    }

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        ServiceContract expected = ServiceContract.build(b -> b.requires(A.class));
        check(expected, new BaseAssembly() {
            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(NeedsA.class);
                provide(NeedsAOptional.class);
                provide(B.class);
            }
        });
    }

    @Test
    public void all() {
        ServiceContract expected = ServiceContract.build(b -> b.optional(A.class).requires(B.class).provides(C.class));
        check(expected, new BaseAssembly() {

            @Override
            protected void build() {
                lookup(MethodHandles.lookup());
                provide(NeedsAOptional.class);
                provide(NeedsB.class);
                provide(C.class).export();
            }
        });
    }

    static void check(ServiceContract expected, Bundle<?> s) {
        assertThat(ServiceContract.of(s)).isEqualTo(expected);
        // TODO maybe check that runtime is the same. Except
        // That requires is removed??
    }
}
