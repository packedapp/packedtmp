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

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 */
// AppManifest
// AppSettings
// Could also be Consumer<? super AppOptions>

public class AppOptions {

    /**
     * Sets a maximum time for the application to run. When the deadline podpodf the app is shutdown.
     * 
     * @param timeout
     * @param unit
     * @return
     */
    public AppOptions setTimeToLive(long timeout, TimeUnit unit) {
        // Container will be shutdown normally after the specified timeout
        return this;
    }

    public AppOptions setTimeToLive(long timeout, TimeUnit unit, Supplier<Throwable> supplier) {
        setTimeToLive(10, TimeUnit.SECONDS, () -> new CancellationException());
        return this; // Will be shutdown using this
    }
}
