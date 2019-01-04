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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */

// Can schedule thing, .. query state, ...
// Could also be a cluster....
// Startup stuff....
public interface StartupContext<T> {

    CompletableFuture<T> now();

    CompletableFuture<T> in(long timeout, TimeUnit unit);
}

// Startup, shutdown

// StartupContext<Container> startup();
// StartupContext<Container> shutdownNow();

// Query State (Alle)
// Notify On State [OnXXX] <-Listener???
// Control start/stop (Component only)
// OnXX (Component only)
// Control lifecycle (start/stop) Container only
// Run OnState (Container.....<----)

// lifecycle.container = lifecycle.root
// Syntes sgu det giver mening.eller...

// Nu er vi ihvertfald enige om @OnInitialize, saa kan vi implementere den....
/// No Shutdown, because shutdown can be multi threaded.

// components that have transistioned to the starting phase will have its shutdown methods invoked...

// Method @OnStart and @OnStop might be invoked concurrently...
// if methods are synchronized....