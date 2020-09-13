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
package packed.internal.inject.various;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import packed.internal.util.LookupUtil;

/**
 *
 */
public class MHTest {

    static final MethodHandle MH = LookupUtil.mhStaticSelf(MethodHandles.lookup(), "hello", void.class, String.class);

    static final MethodHandle MHI = LookupUtil.mhStaticSelf(MethodHandles.lookup(), "aint", int.class, String.class);
    static final MethodHandle MHS = LookupUtil.mhStaticSelf(MethodHandles.lookup(), "astr", String.class, String.class);
    static final MethodHandle MHC = mhConstructorPrivate(MethodHandles.lookup(), Foo.class, int.class, String.class);
    // .mhStaticSelf(MethodHandles.lookup(), "aint", int.class, String.class);

    public static void main(String[] args) throws Throwable {

        System.out.println(MHI.type());

        MethodHandle mh = MethodHandles.collectArguments(MHI, 0, MHS);

        System.out.println(mh.type());

        System.out.println(MethodHandles.dropArguments(MHI, 1, Long.class, Double.class));

    }

    public static int aint(String str) {
        return 123;
    }

    public static String astr(String str) {
        return str;
    }

    public static void hello(String str) {
        System.out.println(str);
    }

    public static class Foo {
        Foo(int i, String s) {
            System.out.println("Success");
        }
    }

    public static MethodHandle mhConstructorPrivate(MethodHandles.Lookup caller, Class<?> constructorType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(void.class, parameterTypes);
        try {
            return caller.findConstructor(constructorType, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
