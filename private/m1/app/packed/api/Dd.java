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
package app.packed.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 */
public class Dd {

    public static void main(String[] args) throws Throwable {
        MethodHandle mh = MethodHandles.lookup().findStatic(Dd.class, "doo", MethodType.methodType(void.class));
        for (int i = 0; i < 4; i++) {
            mh = MethodHandles.foldArguments(mh, mh);
        }
        mh.invoke();
    }

    static void doo() {
        System.out.println("FFFF");
    }
}
