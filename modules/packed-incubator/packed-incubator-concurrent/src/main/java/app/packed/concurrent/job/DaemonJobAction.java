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
package app.packed.concurrent.job;

import java.util.concurrent.TimeUnit;

/**
 *
 */
//Could return, RESTART, EXIT, SLEEP, CONTINUE

// Tror det er noget den annoteret metode kan returnere

interface DaemonJobAction {

    // The one problem here is cleanup...
    // Do we want a stop method? Probably not
    static DaemonJobAction sleep(long duration, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }
}
