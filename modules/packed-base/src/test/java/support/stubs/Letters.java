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

    public static final A A0 = new A();
    public static final A A1 = new A();
    public static final A A2 = new A();

    public static final B B0 = new B();
    public static final C C0 = new C();
    public static final D D0 = new D();
    public static final E E0 = new E();
    public static final F F0 = new F();
    public static final G G0 = new G();
    public static final H H0 = new H();
    public static final I I0 = new I();
    public static final J J0 = new J();

    /** A test stub. */
    public static class A {};

    /** A test stub. */
    public static class B {};

    /** A test stub. */
    public static class C {};

    /** A test stub. */
    public static class D {};

    /** A test stub. */
    public static class E {};

    /** A test stub. */
    public static class F {};

    /** A test stub. */
    public static class G {};

    /** A test stub. */
    public static class H {};

    /** A test stub. */
    public static class I {};

    /** A test stub. */
    public static class J {};

    /** A test stub. */
    public static class K {};

    /** A test stub. */
    public static class L {};

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
