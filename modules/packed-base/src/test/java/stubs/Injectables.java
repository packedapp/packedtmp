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
package stubs;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import app.packed.inject.Inject;
import stubs.Letters.A;

/**
 *
 */

public class Injectables {

    /** Various fields that should be injected. */
    public static class InjectA {

        public final A expected;

        @Inject
        A fieldAccessPackagePrivate;

        @Inject
        private A fieldAccessPrivate;

        @Inject
        protected A fieldAccessProtected;

        @Inject
        public A fieldAccessPublic;

        public A fieldNotAnnotated;

        @Inject
        public A fieldOverridden = new A();

        private boolean methodAccessPackagePrivate;

        private boolean methodAccessPrivate;

        private boolean methodAccessProtected;

        private boolean methodAccessPublic;

        private boolean methodTwoParameters;

        private boolean methodWithReturnType;

        private boolean methodInjectNothing;

        public InjectA(A expected) {
            this.expected = requireNonNull(expected);
        }

        @Inject
        void accessPackagePrivate(A value) {
            assertSame(expected, value);
            this.methodAccessPackagePrivate = true;
        }

        @Inject
        private void accessPrivate(A value) {
            assertSame(expected, value);
            methodAccessPrivate = true;
        }

        @Inject
        protected void accessProtected(A value) {
            assertSame(expected, value);
            this.methodAccessProtected = true;
        }

        @Inject
        public void accessPublic(A value) {
            assertSame(expected, value);
            this.methodAccessPublic = true;
        }

        public final void notAnnotated(A value) {
            fail("This method should not be injected");
        }

        @Inject
        public final void twoParameters(A value1, A value2) {
            assertSame(expected, value1);
            assertSame(expected, value2);
            this.methodTwoParameters = true;
        }

        @Inject
        public void injectNothing() {
            methodInjectNothing = true;
        }

        public void verify() {
            // Verify fields
            assertNull(fieldNotAnnotated, "field should not have been injected");
            assertEquals(expected, this.fieldAccessPublic, "public field was not injected");
            assertEquals(expected, this.fieldOverridden, "public field was not injected");
            assertEquals(expected, this.fieldAccessProtected, "protected field was not injected");
            assertEquals(expected, this.fieldAccessPackagePrivate, "package private field was not injected");
            assertEquals(expected, this.fieldAccessPrivate, "private field was not injected");

            // Verify methods

            // Private methods are never overridden
            assertTrue(methodAccessPrivate, "method with private access was not injected");

            // Public and protected methods are always overridden
            if (getClass() == InjectA.class) {
                assertTrue(methodAccessPublic, "method with public access was not injected");
                assertTrue(methodAccessProtected, "method with protected access was not injected");
            } else {
                assertFalse(methodAccessPublic, "method with public access is overridden and should not have been injected");
                assertFalse(methodAccessProtected, "method with public access is overridden and should not have been injected");
            }

            // Package private methods is only overridden in the same package
            if (getClass() == InjectA.class || getClass().getPackage() != InjectA.class.getPackage()) {
                assertTrue(methodAccessPackagePrivate, "method with package private access was not injected");
            } else {
                assertFalse(methodAccessPackagePrivate, "method with package private access is overridden and should not have been injected");
            }
            // Other methods (Not overridden in subclasses)
            assertTrue(methodTwoParameters, "method with 2 identical parameters not injected");
            assertTrue(methodWithReturnType, "method with return type was not injected");
            assertTrue(methodInjectNothing, "method with no parameters was not injected");

        }

        @Inject
        public String withReturnType(A value) {
            assertSame(expected, value);
            this.methodWithReturnType = true;
            return "FooBar";
        }
    }

    /** Various fields that should be injected. */
    public static class InjectAExtends extends InjectA {

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

        public InjectAExtends(A expected) {
            super(expected);
        }

        @Override
        @Inject
        void accessPackagePrivate(A value) {
            assertSame(expected, value);
            this.methodAccessPackagePrivate = true;
        }

        @Inject
        private void accessPrivate(A value) {
            assertSame(expected, value);
            methodAccessPrivate = true;
        }

        @Override
        @Inject
        protected void accessProtected(A value) {
            assertSame(expected, value);
            this.methodAccessProtected = true;
        }

        @Override
        @Inject
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

    /** Various fields that should be injected. */
    public static class InjectAOptional {
        @Inject
        public Optional<A> field;

        public Optional<A> method;

        @Inject
        public void injectPublic(Optional<A> value) {
            this.method = value;
        }
    }

}
