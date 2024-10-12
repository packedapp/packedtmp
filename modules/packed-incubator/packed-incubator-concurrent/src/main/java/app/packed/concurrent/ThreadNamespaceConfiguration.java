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

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.build.BuildActor;
import app.packed.namespace.NamespaceConfiguration;
import internal.app.packed.concurrent.ExecutorConfiguration;

/**
 * A thread namespace defines policies that used for all daemons and scheduled jobs defined within the same namespace.
 */
// Har vi saadan en per ContainerLifecycle
// Namespace???
// Fyldt med Thread factories/pools
// Hvad med extensions??? Vi vil gerne kunne configure deres.
// Maaske deler vi en enkelt. Maaske har vi noget hierrakisk hvor vi nedarver settings
public final class ThreadNamespaceConfiguration extends NamespaceConfiguration<ThreadExtension> {

    final ThreadNamespaceHandle handle;

    /**
     * @param handle
     */
    ThreadNamespaceConfiguration(ThreadNamespaceHandle handle, ThreadExtension extension, BuildActor actor) {
        super(handle, extension, actor);
        this.handle = handle;
    }

    public DaemonOperationConfiguration addDaemon(Consumer<DaemonContext> action) {
        throw new UnsupportedOperationException();
    }

    public ScheduledOperationConfiguration addScheduledJob(Consumer<SchedulingContext> action) {
        throw new UnsupportedOperationException();
    }

    // Ideen er lidt at man registrer Executor Services, og saa mapper man dem bagefter
    // Function<OperationInfo -> ExecutorService>
    public void register(ExecutorService scheduler, boolean shutdownOnExit) {
        handle.scheduler = new ExecutorConfiguration(scheduler, shutdownOnExit);
    }

    Stream<ScheduledOperationConfiguration> scheduledOperations() {
        // All In Application -> No, that would break cross assembly conf
        // All In Assembly
        // All In Container
        // All in Container + Childs in same Assembly
        throw new UnsupportedOperationException();
    }

    // Function<OperationInfo -> ExecutorService>

    // Maps Operations -> Threadpools/virtual/thread
}
//

//// Hvad hvis vi har flere annoteringer??? Vi kan vel kun have en?
//// Den giver ikke super mening..
//public DaemonOperationConfiguration addDaemonApplication(Assembly assembly, Wirelet... wirelets) {
//  throw new UnsupportedOperationException();
//}
