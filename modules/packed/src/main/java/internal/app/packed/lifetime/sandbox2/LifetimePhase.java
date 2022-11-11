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
package internal.app.packed.lifetime.sandbox2;

/**
 *
 */
// LifecyclePhase was LifetimeTransition
public enum LifetimePhase {
    INITIALIZATION, STARTUP, SHUTDOWN;
}
// Initialization - Single threaded - On Any failure
// Startup - Potential Multi threaded - On Any failure
// Shutdown - Potential Multi threaded - always continues (reverse order)


// initialize(Lifesegment pre, Lifesegment segment, Lifesegment post)
// startup(Lifesegment pre, Lifesegment segment, Lifesegment post);
// stop(Lifesegment pre, Lifesegment segment, Lifesegment post);