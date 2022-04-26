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
package app.packed.operation.mh;

import static app.packed.operation.mh.MethodHandleUtil.afterInterceptor;
import static app.packed.operation.mh.MethodHandleUtil.aroundInterceptor;
import static app.packed.operation.mh.MethodHandleUtil.beforeInterceptor;
import static app.packed.operation.mh.MethodHandleUtil.invokeAndReturnArg;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
/**
 *
 */
public class OpTest {
    static final MethodHandle PRINT;
    static final MethodHandle TIMES_TWO;
    static final MethodHandle before, after, hi;
    
    static {
        Lookup l = MethodHandles.lookup();
        try {
            PRINT = l.findStatic(OpTest.class, "print", MethodType.methodType(void.class, String.class));
            TIMES_TWO = l.findStatic(OpTest.class, "timesTwo", MethodType.methodType(int.class, int.class));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        before = PRINT.bindTo("Before");
        after = PRINT.bindTo("After");
        hi = PRINT.bindTo("Hi");
    }

    public static void mainx(String[] args) throws Throwable {

        before.invoke();
        hi.invoke();
        after.invoke();

        System.out.println("------");
        // MH - (String)
        // Prints Before - Hej
        MethodHandle beforeHi = MethodHandles.foldArguments(hi, before);
        beforeHi.invokeExact();

        System.out.println("------");
        // MH - (String)
        // Prints Hej - After
        MethodHandle hiAfter = MethodHandles.foldArguments(after, hi);
        hiAfter.invokeExact();

        System.out.println("------");
        // MH - (String)
        // Prints Before - Hej - After
        MethodHandle beforeHiAfter = MethodHandles.foldArguments(after, hi);
        beforeHiAfter = MethodHandles.foldArguments(beforeHiAfter, before);
        beforeHiAfter.invokeExact();

        // MH - (String)
        // Prints Before - 246
        MethodHandle m1 = MethodHandles.foldArguments(TIMES_TWO, before);
        System.out.println(m1.type());
        System.out.println("CONVERTED TO " + m1.invoke(123));

        MethodHandle mh = invokeAndReturnArg(before, int.class);

        System.out.println(mh.type());
        System.out.println(mh.invoke("sad"));

    }

    public static void main(String[] args) throws Throwable {

        MethodHandle andThenAfter = afterInterceptor(TIMES_TWO, after);

        System.out.println(andThenAfter.invoke(123));
        System.out.println();

        MethodHandle andThenBefore = beforeInterceptor(TIMES_TWO, before);
        System.out.println(andThenBefore.invoke(333));

        MethodHandle around = aroundInterceptor(TIMES_TWO, before, after);
        System.out.println();
        System.out.println("Result = " + around.invoke(222));
    }


    public static void print(String s) {
        System.out.println(s);
    }

    public static int timesTwo(int i) {
        System.out.println("Times two " + i);
        return i * 2;
    }

    public static String toUppercase(String s) {
        System.out.println("Converting to uppercase " + s);
        return s.toUpperCase();
    }
}
