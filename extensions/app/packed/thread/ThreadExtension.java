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
package app.packed.thread;

import app.packed.extension.Extension;

/**
 *
 */
// Controls everything about threads...

// We can have a concurrency extension as well that is more.
// What kind of model do you use...

// Vi vil gerne have vores egne executors. Saa de bliver lukket ned
// naar containeren bliver shutdown...

public class ThreadExtension extends Extension<ThreadExtension> {

    // disableThis
    // disableThis+Kids
    // disableThis for everyone except LifecycleExtension
    // disableThis for user

    // Starting Threads
    // Monitoring threads
    // Thread limits

    // Scheduling???
    // As in Thread Scheduling???

    // I think yes
    // IDK maybe a scheduler.
}
