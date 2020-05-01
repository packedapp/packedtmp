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
package packed.internal.reflect.mess;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.inject.InjectionContext;
import packed.internal.reflect.MethodHandleBuilder;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.mhadventures.MH1;

/**
 *
 */
public class TestIt2 {

    TestIt2(InjectionContext icd, Optional<String> s, String s2, Integer l4, Optional<Long> ol, Optional<InjectionContext> ic) {
        System.out.println(s + " - " + s2 + " " + l4);
        System.out.println(ol);
        System.out.println(ic.get().keys());
        System.out.println(icd == ic.get());
    }

    public static void main(String[] args) throws Throwable {
        MethodHandleBuilder aa = MethodHandleBuilder.of(TestIt2.class, String.class);
        // aa.addKey(String.class, 0);
        aa.addKey(String.class, MH1.DUP, 0);
        aa.addKey(Integer.class, MH1.LENGTH, 0);

        // aa.addKey(String.class, 0);

        MethodHandle mh = new OpenClass(MethodHandles.lookup(), TestIt2.class, false).findConstructor(aa);
        System.out.println(mh);

        TestIt2 it = (TestIt2) mh.invoke("asdsd");
        System.out.println(it);
    }

    static class Data {

        String tt() {
            return "foobarwewe";
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    static @interface X {

    }

    public static Object dd(Data dd, Class<?> cl) {
        System.out.println("Du er altid for sej" + dd + " " + cl);
        return "Foo";
    }
}
