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

import app.packed.inject.InjectionContext;
import packed.internal.reflect.MethodHandleBuilder;
import packed.internal.reflect.OpenClass;

/**
 *
 */
public class Stuff {

    Stuff(Number n, InjectionContext ic) {
        System.out.println(ic.keys());
    }

    public static void main(String[] args) throws Throwable {
        MethodHandleBuilder b = MethodHandleBuilder.of(Stuff.class, String.class, Integer.class);
        b.addKey(Number.class, 1);
        OpenClass oc = new OpenClass(MethodHandles.lookup(), Stuff.class, false);
        MethodHandle mh = b.build(oc, Stuff.class.getDeclaredConstructors()[0]);

        System.out.println(mh.invoke("FOO", 123));
    }

}
