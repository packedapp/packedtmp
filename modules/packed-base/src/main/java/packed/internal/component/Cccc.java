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
package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

import app.packed.base.TypeLiteral;

/**
 *
 */
public class Cccc {

    static final MethodHandle MH;

    static {
        try {
            MH = MethodHandles.lookup().findVirtual(Consumer.class, "accept", MethodType.methodType(void.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        TypeLiteral<Consumer<? extends Number>> tl = new TypeLiteral<>() {};
        Consumer<Long> c = (cc) -> System.out.println("ff " + cc);
        addSingletonFunction(tl, c);
    }

    static void foo(Consumer<?> c) {
        for (Method m : c.getClass().getDeclaredMethods()) {
            System.out.println(m);
        }
    }

    // Det er ogsaa med functions at vi faktisk er anderledes...
    // Taenk jeg

    // Mapper til en @Get
    static <T> void addSingletonFunction(TypeLiteral<T> tl, T tl1) throws Throwable {
        System.out.println(tl1 instanceof Consumer);
        System.out.println(Arrays.asList(tl1.getClass().getInterfaces()));
        MH.invoke(tl1, 23L);
    }

    // Er det @Get der definere det???
    // Hvor mange midter lag har vi?

    // Tjen
    static class FunctionDefinition<T> {
        // Has A @Get Inside...
        // Or do we map to a sidecar??

        // Yeah a function sidecar...

        MethodHandle mh;

        void foo() {}
    }

}
