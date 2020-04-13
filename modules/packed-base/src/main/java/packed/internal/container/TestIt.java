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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Set;

import app.packed.base.Key;
import app.packed.inject.InjectionContext;

/**
 *
 */
public class TestIt {

    TestIt(String s, InjectionContext ic, String s2) {
        System.out.println("IF " + ic.keys());
        System.out.println(s + "   " + s2);
    }

    public static void main(String[] args) throws Throwable {
        Constructor<?> con = TestIt.class.getDeclaredConstructors()[0];
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle mh = lookup.unreflectConstructor(con);

        mh = MethodHandles.insertArguments(mh, 1, new Inj());

        mh = MethodHandles.dropArguments(mh, 2, String.class);
        mh.invoke("ffff", "ddd", "sdsdsd");
        System.out.println("Bye");
    }

    static class Inj implements InjectionContext {

        /** {@inheritDoc} */
        @Override
        public Set<Key<?>> keys() {
            return Set.of(Key.of(String.class));
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> target() {
            return String.class;
        }
    }
}

// En extension instans tager bare en PEC?
//