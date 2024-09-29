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
package testutil.stubs.otherpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.packed.bean.Inject;
import app.packed.lifecycle.OnInitialize;
import testutil.stubs.Injectables.InjectA;
import testutil.stubs.Letters.A;

/**
 * Tests that package private methods are not overridden for classes in other packages
 *
 */
public class InjectAExtendsOtherPackage extends InjectA {

    @Inject
    A fieldAccessPackagePrivate;

    @Inject
    private A fieldAccessPrivate;

    @Inject
    protected A fieldAccessProtected;

    @Inject
    public A fieldAccessPublic;

    private boolean methodAccessPackagePrivate;

    private boolean methodAccessPrivate;

    private boolean methodAccessProtected;

    private boolean methodAccessPublic;

    public InjectAExtendsOtherPackage(A expected) {
        super(expected);
    }

    @SuppressWarnings("all")
    @OnInitialize
    void accessPackagePrivate(A value) {
        assertSame(expected, value);
        this.methodAccessPackagePrivate = true;
    }

    @OnInitialize
    private void accessPrivate(A value) {
        assertSame(expected, value);
        methodAccessPrivate = true;
    }

    @Override
    @OnInitialize
    protected void accessProtected(A value) {
        assertSame(expected, value);
        this.methodAccessProtected = true;
    }

    @Override
    @OnInitialize
    public void accessPublic(A value) {
        assertSame(expected, value);
        this.methodAccessPublic = true;
    }

    @Override
    public void verify() {
        super.verify();
        // Verify fields
        assertNull(fieldNotAnnotated, "field should not have been injected");
        assertEquals(expected, this.fieldAccessPublic, "public field was not injected");
        assertEquals(expected, this.fieldOverridden, "public field was not injected");
        assertEquals(expected, this.fieldAccessProtected, "protected field was not injected");
        assertEquals(expected, this.fieldAccessPackagePrivate, "package private field was not injected");
        assertEquals(expected, this.fieldAccessPrivate, "private field was not injected");

        // Verify methods
        assertTrue(methodAccessPrivate, "method with private access was not injected");
        assertTrue(methodAccessPublic, "method with public access was not injected");
        assertTrue(methodAccessProtected, "method with protected access was not injected");
        assertTrue(methodAccessPackagePrivate, "method with package private access was not injected");
    }

}
