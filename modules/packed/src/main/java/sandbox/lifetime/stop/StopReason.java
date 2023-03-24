/*
z * Copyright (c) 2008 Kasper Nielsen.
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
package sandbox.lifetime.stop;

import static java.util.Objects.requireNonNull;

/**
 *
 * The framework comes with a number of pre-defined reasons. But users and extensions are free to define their own.
 */
//
public final class StopReason {

    final String reason;

    private StopReason(String reason) {
        this.reason = requireNonNull(reason);
    }

    /**
     * Indicates that the JVM has been shutdown. And the lifetime is closing as the result of a shutdown hook being called.
     *
     * @see app.packed.application.ApplicationWirelets#shutdownHook(StopOption...)
     * @see app.packed.application.ApplicationWirelets#shutdownHook(java.util.function.Function, StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     */
    public static final StopReason SHUTDOWN_HOOK = new StopReason("ShutdownHook");

    /** Indicates that lifetime has timed out. */
    // Vi har baade TimeToStart timeout, or entrypoint timeout, tror kun vi har en?
    // Session Time Out (Maa vaere SESSION_TIMEOUT)
    public static final StopReason TIMEOUT = new StopReason("Timeout");

    /** An entry-point completed normally. */
    public static final StopReason ENTRY_POINT_COMPLETED = new StopReason("Normal");

    public static final StopReason NORMAL = new StopReason("Normal");

    public static final StopReason FAILED_INTERNALLY = new StopReason("Failed");

    public static final StopReason RESTARTING = new StopReason("Restarting");

    // ApplicationRedeploy
}
