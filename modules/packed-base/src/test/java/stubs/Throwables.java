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

/**
 * Various throwable classes.
 * 
 */
public class Throwables {

    public static class Error1 extends Error {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static final Error1 INSTANCE = new Error1();

        private Error1() {}
    }

    public static class Error2 extends Error {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Error2 INSTANCE = new Error2();
    }

    public static class Error3 extends Error {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Error3 INSTANCE = new Error3();
    }

    public static class Exception1 extends Exception {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static final Exception1 INSTANCE = new Exception1();
    }

    public static class Exception2 extends Exception {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Exception2 INSTANCE = new Exception2();
    }

    public static class Exception3 extends Exception {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Exception3 INSTANCE = new Exception3();
    }

    public static class RuntimeException1 extends RuntimeException {

        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static final RuntimeException1 INSTANCE = new RuntimeException1();
    }

    public static class RuntimeException2 extends RuntimeException {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static RuntimeException2 INSTANCE = new RuntimeException2();
    }

    public static class RuntimeException3 extends RuntimeException {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static RuntimeException3 INSTANCE = new RuntimeException3();
    }

    public static class Throwable1 extends Throwable {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static final Throwable1 INSTANCE = new Throwable1();
    }

    public static class Throwable2 extends Throwable {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Throwable2 INSTANCE = new Throwable2();
    }

    public static class Throwable3 extends Throwable {
        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        /** The default instance. */
        public static Throwable3 INSTANCE = new Throwable3();
    }

    public static class ThrowsError1InConstructor {

        public ThrowsError1InConstructor() {
            throw Error1.INSTANCE;
        }
    }

    public static class ThrowsException1InConstructor {

        public ThrowsException1InConstructor() throws Exception {
            throw Exception1.INSTANCE;
        }
    }

    public static class ThrowsRuntimeException1InConstructor {

        public ThrowsRuntimeException1InConstructor() {
            throw RuntimeException1.INSTANCE;
        }
    }

    public static class ThrowsThrowable1InConstructor {
        public ThrowsThrowable1InConstructor() throws Throwable1 {
            throw Throwable1.INSTANCE;
        }
    }

}
