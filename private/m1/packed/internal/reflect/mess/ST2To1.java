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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 *
 */
public class ST2To1 {

    static void s0() {
        System.out.println("SO");
    }

    static void s1(String s2) {
        System.out.println("S1 " + s2);
    }

    static void s2(String s, String s2) {
        System.out.println("S2 " + s + " " + s2);
    }

    static void s2(String s, Integer s2) {
        System.out.println("S2 " + s + " " + s2);
    }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mitos = lookup.findVirtual(Integer.class, "toString", MethodType.methodType(String.class));

        Method m0 = ST2To1.class.getDeclaredMethod("s0");
        MethodHandle mh0 = lookup.unreflect(m0);
        Method m1 = ST2To1.class.getDeclaredMethod("s1", String.class);
        MethodHandle mh1 = lookup.unreflect(m1);

        Method m2 = ST2To1.class.getDeclaredMethod("s2", String.class, String.class);
        MethodHandle mh2 = lookup.unreflect(m2);

        MethodHandle a0 = MethodHandles.permuteArguments(mh0, MethodType.methodType(void.class, String.class));

        MethodHandle a1 = MethodHandles.permuteArguments(mh1, MethodType.methodType(void.class, String.class), 0);

        MethodHandle id = MethodHandles.identity(String.class);

        // Arity of varargs must match mh2, all indexes must be in MethodType
        MethodHandle a3 = MethodHandles.permuteArguments(mh2, MethodType.methodType(void.class, String.class), 0, 0);

        System.out.println(a0);
        System.out.println(a1);
        System.out.println(a3);
        System.out.println(id);
        a0.invoke("Hoo");
        a1.invoke("Hoo");
        a3.invoke("Hoo");

        MethodHandle mmm = MethodHandles.collectArguments(mh2, 1, mitos);

        System.out.println(mmm);

        System.out.println("Bye");

        mmm.invoke("foo", 34);

    }

}
