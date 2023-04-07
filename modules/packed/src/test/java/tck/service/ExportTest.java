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

import static tck.service.ServiceLocatorAsserts.testNotPresent;
import static tck.service.ServiceLocatorAsserts.testPresent;
import static testutil.stubs.Letters.A0;
import static testutil.stubs.Letters.B0;
import static testutil.stubs.Letters.C0;
import static testutil.stubs.Letters.D0;

import org.junit.jupiter.api.Test;

import app.packed.service.ServiceLocator;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import tck.ServiceLocatorAppTest;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.Letters.C;
import testutil.stubs.Letters.D;
import testutil.stubs.Qualifiers.CharQualifier;

/**
 * Tests export of services.
 */
@SuppressWarnings("unused")
public class ExportTest extends ServiceLocatorAppTest {

    @Test
    public void basics() {
        installInstance(A0);
        installInstance(B0).provide();
        installInstance(C0).provide().export();
        installInstance(D0).export();

        testNotPresent(app(), A.class);
        testNotPresent(app(), new Key<A>() {});
        testNotPresent(app(), B.class);
        testNotPresent(app(), new Key<B>() {});

        testPresent(app(), C.class, e -> e == C0);
        testPresent(app(), new Key<C>() {}, e -> e == C0);
        testNotPresent(app(), new Key<@CharQualifier C>() {});

        testPresent(app(), D.class, e -> e == D0);
        testPresent(app(), new Key<D>() {}, e -> e == D0);
        testNotPresent(app(), new Key<@CharQualifier D>() {});
    }

    @Test
    public void export() {
        ServiceableBeanConfiguration<A> sc = installInstance(A0).export();
        testPresent(app(), Key.of(A.class), e -> e == A0);

        installInstance(A0).export();
        install(B.class).export();

        testPresent(app(), Key.of(A.class), e -> e == A0);
        testPresent(app(), Key.of(B.class), e -> e instanceof B);
        testPresent(app(), Key.of(B.class), e -> e == app().use(B.class));

        ServiceLocator old = app();
        install(B.class).export();
        // Different instance
        testPresent(app(), Key.of(B.class), e -> e != old.use(B.class));
    }
}
