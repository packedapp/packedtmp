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
package packed.internal.hook;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Qualifier;
import packed.internal.container.extension.test.WwTest.Left;

/**
 *
 */
public class HookTest {

    public static void main(String[] args) throws Throwable {
        Foo<Left, Left> f = new Foo<>() {};
        Hook.Builder.test(MethodHandles.lookup(), f, Foox.class);

    }

    public static class Foo<T extends Annotation, S> {

        @OnHook
        public void foo(AnnotatedFieldHook<Left> h) throws Throwable {
            System.out.println(h.getter().invoke());
            System.err.println("CXXX");
        }

        @OnHook
        public void foodx(AnnotatedFieldHook<T> h) {
            System.err.println("CXXX");
        }

        @OnHook
        public void foox(AnnotatedMethodHook<T> h) {
            System.err.println("CXXX - Method " + h.method());
        }

        @OnHook
        public void foox(AnnotatedTypeHook<T> h) {
            System.err.println("CXXX Tyoe");
        }
    }

    @Left
    public static class Foox {

        @Left
        public static String ss = "dddd";

        @Left
        public static String sss = "ssss";

        @Left
        public static String sss() {
            return "Ssqw";
        }
    }

    @Qualifier
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public @interface Sxx {

    }

}
