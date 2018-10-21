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
package support.stubs;

import static java.util.Objects.requireNonNull;

/**
 * A test stub class.
 *
 */
public class Letters {

    public static final A A = new A();
    public static final A A1 = new A();
    public static final A A2 = new A();

    public static final B B = new B();

    /** A test stub. */
    public static class A extends Letters {};

    /** A test stub. */
    public static class B extends Letters {};

    /** A test stub. */
    public static class C extends Letters {};

    /** A test stub. */
    public static class D {};

    /** A test stub. */
    public static class NeedsA {
        private final A a;

        public NeedsA(A a) {
            this.a = requireNonNull(a, "a");
        }

        public A getA() {
            return a;
        }
    }

    /** A test stub. */
    public static class NeedsB {
        private final B b;

        public NeedsB(B b) {
            this.b = requireNonNull(b, "b");
        }

        public B getB() {
            return b;
        }
    }

    /** A test stub. */
    public static class NeedCDI {
        private final Letters cdi;

        public NeedCDI(Letters cdi) {
            this.cdi = requireNonNull(cdi, "cdi");
        }

        public Letters getCDI() {
            return cdi;
        }

    }

    /** A test stub. */
    public static class AorB {
        private final A a;
        private final B b;

        public AorB(A a) {
            this.a = requireNonNull(a, "a");
            this.b = null;
        }

        public AorB(B b) {
            this.a = null;
            this.b = requireNonNull(b, "b");
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }
    }

    /** A test stub. */
    public static class ABorAorB {
        private final A a;
        private final B b;

        public ABorAorB(A a) {
            this.a = requireNonNull(a, "a");
            this.b = null;
        }

        public ABorAorB(B b) {
            this.a = null;
            this.b = requireNonNull(b, "b");
        }

        public ABorAorB(A a, B b) {
            this.a = requireNonNull(a, "a");
            this.b = requireNonNull(b, "b");
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }
    }

    /** A test stub. */
    public static class NeedsABConstructor {
        private final A a;
        private final B b;

        public NeedsABConstructor(A a, B b) {
            this.a = requireNonNull(a, "a");
            this.b = requireNonNull(b, "b");
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }
    }

    /** A test stub. */
    public static class NeedsABorCDConstructor {
        private final A a;
        private final B b;
        private final C c;
        private final D d;

        public NeedsABorCDConstructor(A a, B b) {
            this.a = requireNonNull(a, "a");
            this.b = requireNonNull(b, "a");
            this.c = null;
            this.d = null;
        }

        public NeedsABorCDConstructor(C c, D d) {
            this.a = null;
            this.b = null;
            this.c = requireNonNull(c, "c");
            this.d = requireNonNull(d, "d");
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

        public C getC() {
            return c;
        }

        public D getD() {
            return d;
        }
    }

    /** A test stub. */
    public static class ABCD {
        private final A a;
        private final B b;
        private final C c;
        private final D d;

        public ABCD(A a, B b, C c, D d) {
            this.a = requireNonNull(a, "a");
            this.b = requireNonNull(b, "a");
            this.c = requireNonNull(c, "c");
            this.d = requireNonNull(d, "d");
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

        public C getC() {
            return c;
        }

        public D getD() {
            return d;
        }
    }

    /** A test stub. */
    public static class XY {
        public XY(YX yx) {}
    }

    /** A test stub. */
    public static class YX {
        public YX(XY xy) {}
    }

    /** A test stub. */
    public static class XYZ {
        public XYZ(YZX yzx) {}
    }

    /** A test stub. */
    public static class YZX {
        public YZX(ZXY zxy) {}
    }

    /** A test stub. */
    public static class ZXY {
        public ZXY(XYZ xyz) {}
    }
}
