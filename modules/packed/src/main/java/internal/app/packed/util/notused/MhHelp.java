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
package internal.app.packed.util.notused;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import internal.app.packed.util.LookupUtil;

/**
 *
 */
public class MhHelp {

    static final MethodHandle INC = LookupUtil.findStaticOwn(MethodHandles.lookup(), "increment", int.class, int.class);

    static final MethodHandle INCL = LookupUtil.findStaticOwn(MethodHandles.lookup(), "incrementL", int.class, long.class);

    static final MethodHandle ADD = LookupUtil.findStaticOwn(MethodHandles.lookup(), "add", int.class, int.class, int.class);

    static int incrementL(long a) {
        return (int) a + 1;
    }

    static int increment(int a) {
        return a + 1;
    }

    static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) throws Throwable {
        System.out.println((int) ADD.invokeExact(1, 5));

        MethodHandle fa = MethodHandles.filterArguments(ADD, 0, ADD, INC);

        System.out.println(fa.type());
        System.out.println(fa.invokeExact(1, 2));
        System.out.println(fa.invokeExact(123, 2));
    }

    public static void mainz(String[] args) {
        MethodHandles.collectArguments(null, 0, null);
        MethodHandles.filterArguments(null, 0);
        MethodHandles.foldArguments(null, 0, null);
        MethodHandles.permuteArguments(null, null);
    }
}
