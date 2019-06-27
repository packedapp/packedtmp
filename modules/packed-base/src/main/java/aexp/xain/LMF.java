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
package aexp.xain;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import app.packed.container.Extension;

/**
 *
 */
public class LMF {

    public static void main2(String[] args) throws Throwable {
        MethodHandles.Lookup caller = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(Object.class);
        MethodType actualMethodType = MethodType.methodType(String.class);
        MethodType invokedType = MethodType.methodType(Supplier.class);
        CallSite site = LambdaMetafactory.metafactory(caller, "get", invokedType, methodType, caller.findStatic(LMF.class, "print", actualMethodType),
                methodType);
        MethodHandle factory = site.getTarget();
        Supplier<String> r = (Supplier<String>) factory.invoke();
        System.out.println(r.get());
    }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup caller = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(Object.class);
        MethodType invokedType = MethodType.methodType(Supplier.class);
        CallSite site = LambdaMetafactory.metafactory(caller, "get", invokedType, methodType,
                caller.findConstructor(MyExtension.class, MethodType.methodType(void.class)), methodType);
        MethodHandle factory = site.getTarget();
        Supplier<MyExtension> r = (Supplier<MyExtension>) factory.invoke();

        System.out.println(r.get());
        System.out.println(r.get());
    }

    @SuppressWarnings("unused")
    private static String print() {
        return "hello world";
    }

    public static class MyExtension extends Extension<MyExtension> {}
}
