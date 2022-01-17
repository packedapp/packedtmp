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
package packed.internal.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 */
public class Tester {

    public static void main(String[] args) throws Throwable {
        MethodHandle mh = MethodHandles.lookup().findStatic(Tester.class, "bla", MethodType.methodType(void.class, String.class));

        MethodHandle m1 = MethodHandles.collectArguments(mh, 0, MethodHandles.constant(String.class, "foobar"));

        MethodHandle m2 = mh.bindTo("foobar");

        m1.invoke();
        m2.invoke();
        
        System.out.println(m1.getClass());
        System.out.println(m2.getClass());
    }

    public static void bla(String s) {
        System.out.println(s);
    }
}
