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
package app.packed.lifecycle;

import java.util.function.Consumer;

/**
 * Actions that can be performed at particular times in the
 */
// Det er noget a.la. naar man har naaet et syncpoint....
// Hoere maaske mere til i Lifecycle...
public interface ContainerActionable { // extends SyncPoint???????
    // Lidt som syncpoint.....MEget som syncpoitn

    // execute/run/shit
    // with Service do x

    // runAlways, runIfSuccesfull, runIfFailed
    // Default run if succesfull????

    // Kan være på baggrund af flere syncpoints. Så vi skal have noget navngivning for det.
    // Og maaden at sige, hvis 3/4 lykkedes saa xyz. ellers P

    // Checks that the service will exist before hand
    default <T> void run(Class<T> serviceType, Consumer<T> consumer) {}

    default void run(Runnable runnable) {}

    // Something takes a container...
}
