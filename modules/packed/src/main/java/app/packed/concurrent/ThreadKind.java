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
package app.packed.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 *
 * <p>
 * Typically, operations allows for more specific thread configuration by accepting a
 * {@link java.util.concurrent.ThreadFactory}. For example
 * {@link DaemonOperationConfiguration#threadFactory(java.util.concurrent.ThreadFactory)}
 */
public enum ThreadKind {

    /** A non-daemon platform thread */
    PLATFORM_THREAD,

    /** A daemon platform thread */
    DAEMON_THREAD,

    /** A virtual thread */
    VIRTUAL_THREAD;

    public ThreadFactory threadFactory() {
        return switch (this) {
        case DAEMON_THREAD -> Thread.ofPlatform().daemon().factory();
        case PLATFORM_THREAD -> Thread.ofPlatform().factory();
        case VIRTUAL_THREAD -> Thread.ofVirtual().factory();
        };
    }
}
