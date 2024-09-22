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
package micro.hook;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class HookOnExtensionActivationMicro {

    // Den er lige droppet indtil vi kan ditche det stack trace gathering

    /* --------------------------------------- */

    public static class Comp1Field extends Extension<Comp1Field> {
        /**
         * @param handle
         */
        protected Comp1Field(ExtensionHandle<Comp1Field> handle) {
            super(handle);
        }

        @HookActivateAnnotation
        public static final String foo = "";
    }

    public static class Comp1Method extends Extension<Comp1Method> {
        /**
         * @param handle
         */
         Comp1Method(ExtensionHandle<Comp1Method> handle) {
            super(handle);
        }

        @HookActivateAnnotation
        public static final void foo() {}
    }

    public static class Comp1Field1Method extends Extension<Comp1Field1Method> {
        /**
         * @param handle
         */
         Comp1Field1Method(ExtensionHandle<Comp1Field1Method> handle) {
            super(handle);
        }

        @HookActivateAnnotation
        public static final String foo = "";

        @HookActivateAnnotation
        public static final void foo() {}
    }

    public static class Comp3Fields3Methods extends Extension<Comp3Fields3Methods> {
        /**
         * @param handle
         */
        protected Comp3Fields3Methods(ExtensionHandle<Comp3Fields3Methods> handle) {
            super(handle);
        }

        @HookActivateAnnotation
        public static final String foo1 = "";
        @HookActivateAnnotation
        public static final String foo2 = "";
        @HookActivateAnnotation
        public static final String foo3 = "";

        @HookActivateAnnotation
        public static final void foo1() {}

        @HookActivateAnnotation
        public static final void foo2() {}

        @HookActivateAnnotation
        public static final void foo3() {}
    }

    public static class Comp5Fields5Methods extends Extension<Comp5Fields5Methods> {
        /**
         * @param handle
         */
        protected Comp5Fields5Methods(ExtensionHandle<Comp5Fields5Methods> handle) {
            super(handle);
        }

        @HookActivateAnnotation
        public static final String foo1 = "";
        @HookActivateAnnotation
        public static final String foo2 = "";
        @HookActivateAnnotation
        public static final String foo3 = "";
        @HookActivateAnnotation
        public static final String foo4 = "";
        @HookActivateAnnotation
        public static final String foo5 = "";

        @HookActivateAnnotation
        public static final void foo1() {}

        @HookActivateAnnotation
        public static final void foo2() {}

        @HookActivateAnnotation
        public static final void foo3() {}

        @HookActivateAnnotation
        public static final void foo4() {}

        @HookActivateAnnotation
        public static final void foo5() {}
    }

    @Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface HookActivateAnnotation {}

}
