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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import app.packed.inject.InjectionContext;
import packed.internal.reflect.InjectableFunction;
import packed.internal.reflect.OpenClass;

/**
 *
 */
public class TestIt {

    TestIt(Data data, InjectionContext ic, String sd, Data d2, InjectionContext i3, String s) {
        System.out.println(ic);
        System.out.println(sd);
    }

    public static void main(String[] args) throws Throwable {
        Lookup ll = MethodHandles.privateLookupIn(Data.class, MethodHandles.lookup());

        MethodHandle mhhhh = ll.findSpecial(Data.class, "tt", MethodType.methodType(String.class), Data.class);

        InjectableFunction aa = InjectableFunction.of(TestIt.class, Data.class);
        aa.addKey(Data.class, 0);
        aa.addKey(String.class, 0, mhhhh);

        MethodHandle mh = new OpenClass(MethodHandles.lookup(), TestIt.class, false).findConstructor(aa);
        System.out.println(mh);

        TestIt it = (TestIt) mh.invoke(new Data());
        System.out.println(it);
    }

    static class Data {

        String tt() {
            return "foobarwewe";
        }
    }
}
