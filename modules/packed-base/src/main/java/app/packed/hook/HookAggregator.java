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
package app.packed.hook;

import java.lang.invoke.MethodHandle;

/**
 *
 */
// HookCollector
// Tror godt vi vil have en specific alligevel....
// Bliver hurtig lidt rodet nu med en
public interface HookAggregator<T> {

    public static void foo(MethodHandle mh) {
        mh.bindTo("ddd");
    }

}

// Vi kan godt supportere consumation af nogle instantiated method handles....
