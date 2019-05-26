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
package app.packed.container;

import java.lang.module.Configuration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.inject.ServiceWirelets;
import packed.internal.inject.buildtime.DefaultContainerConfiguration;

/** Various container wiring options. */
public final class ContainerWirelets {

    /** No instantiation. */
    private ContainerWirelets() {}

    /**
     * Sets a maximum time for the container to run. When the deadline podpodf the app is shutdown.
     * 
     * @param timeout
     *            the timeout
     * @param unit
     *            the timeunit
     * @return this option object
     */
    // These can only be used with a TopContainer with lifecycle...
    // Container will be shutdown normally after the specified timeout

    // Vi vil gerne have en version, vi kan refreshe ogsaa???
    // Maaske vi bare ikke skal supportered det direkte.

    // Teknisk set er det vel en app wirelet
    public static Wirelet timeToLive(long timeout, TimeUnit unit) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    public static Wirelet timeToLive(long timeout, TimeUnit unit, Supplier<Throwable> supplier) {
        timeToLive(10, TimeUnit.SECONDS, () -> new CancellationException());
        // Alternativ, kan man wrappe dem i f.eks. WiringOperation.requireExecutionMode();
        return new Wirelet() {

            // @Override
            // protected void process(BundleLink link) {
            // link.mode().checkExecute();
            // }
        };
    }

    /**
     * Returns a wirelet that will set name of a container once wired, overriding any existing name it may have.
     * 
     * @param name
     *            the name of the container
     * @return a wirelet that will set name of a container once wired
     */
    // Move to Wirelet???
    public static Wirelet name(String name) {
        return new DefaultContainerConfiguration.OverrideNameWiringOption(name);
    }

    public static Wirelet main(String... args) {
        // MainArgs.wirelet(String... args)
        return ServiceWirelets.provide(MainArgs.of(args));
    }

    public static Wirelet config(Configuration c) {
        // This is for App, but why not for Injector also...
        // we need config(String) for wire()..... configOptional() also maybe...
        // Would be nice.. if config extends WiringOperations
        // alternative c.wire();
        // c.get("/sdfsdf").wire();

        // Maaske skal nogle klasser bare implementere WiringOperation...
        throw new UnsupportedOperationException();
    }

    // force start, initialize, await start...
}
