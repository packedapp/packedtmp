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
package app.packed.app;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import app.packed.container.Bundle;

/**
 *
 */
public class Dooo {
    String ss;

    public Dooo() {

    }

    public static void init(Class<?> cl) throws Exception {
        Constructor<?> c = cl.getDeclaredConstructor();
        Object newInstance = c.newInstance();
        System.out.println(newInstance);

        Lookup lookup = MethodHandles.lookup();

        lookup = MethodHandles.privateLookupIn(cl, lookup);
        lookup.unreflectConstructor(c);
    }

    public static MethodHandle initNoPrivate(Class<?> cl) throws Exception {
        Bundle.class.getModule().addReads(cl.getModule());

        // Constructor<?> c = cl.getDeclaredConstructor();

        Lookup lookup = MethodHandles.lookup();

        lookup = MethodHandles.privateLookupIn(cl, lookup);

        return lookup.findConstructor(cl, MethodType.methodType(void.class));

        // MethodHandle mh = lookup.unreflectConstructor(c);
        //
        // System.out.println(mh.type());
        // return mh;
    }

    public static MethodHandle initNoPrivate(Class<?> cl, MethodHandles.Lookup lookup) throws Exception {

        Constructor<?> c = cl.getDeclaredConstructor();

        // Lookup lookup = MethodHandles.lookup();

        // lookup.findConstructor(cl, MethodType.methodType(cl));

        return lookup.unreflectConstructor(c);
    }
}
