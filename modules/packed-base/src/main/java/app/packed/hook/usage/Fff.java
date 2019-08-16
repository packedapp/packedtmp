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
package app.packed.hook.usage;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import app.packed.hook.FieldOperator;

/**
 *
 */
public class Fff {

    public static String foo = "DDD";

    public static void main(String[] args) throws Throwable {

        Field f = Fff.class.getDeclaredField("foo");

        FieldOperator<Object> fo = FieldOperator.getOnce();
        FieldOperator<Supplier<Object>> fos = FieldOperator.supplier();

        System.out.println(fo.accessStatic(MethodHandles.lookup(), f));

        Supplier<Object> s = fos.accessStatic(MethodHandles.lookup(), f);
        System.out.println(s.get());
        foo = "AAA";
        System.out.println(s.get());
        System.out.println(s.get());
        // fo.accessStatic(h)
    }
}
