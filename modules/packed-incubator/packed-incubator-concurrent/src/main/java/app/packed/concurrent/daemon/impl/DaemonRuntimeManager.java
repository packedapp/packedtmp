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
package app.packed.concurrent.daemon.impl;

import java.util.concurrent.ConcurrentHashMap;

import app.packed.bean.lifecycle.Start;

// Vi har jo strengt taget ikke brug for denne laengere
// Med mindre vi vil se en liste af alle daemons
// Taenker vi maaske ogsaa kan dele en ConcurrentFaetter klasse nu..
public final class DaemonRuntimeManager {

    // ConcurrentSet
    final ConcurrentHashMap<Thread, DaemonSideBean> deamons = new ConcurrentHashMap<>();

    @Start
    public void onStart() {
        IO.println("On Start");
    }

    @Start
    public void onStop() {
        IO.println("Bye");
    }
}