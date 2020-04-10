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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactSource;
import app.packed.container.Extension;
import app.packed.container.Wirelet;

/**
 * Wirelets that can be used when creating an {@link App} instance. For example, via
 * {@link App#start(ArtifactSource, Wirelet...)} or {@link App#execute(ArtifactSource, Wirelet...)}.
 */
// InvalidWireletApplicationException -> Thrown when trying to apply a wirelet in a situation where it cannot be used

// Do we allow them in Image????
// I don't see why not... So
//// Taenker ikke det er App. maaske Lifecycle wirelets....
// Isaer hvis App er en wrapper over en masse ting...
public final class LifecycleWirelets {

    /** No instantiation. */
    private LifecycleWirelets() {}

    /**
     * Sets a maximum time for the container to run. When the deadline podpodf the app is shutdown.
     * 
     * @param timeout
     *            the timeout
     * @param unit
     *            the time unit
     * @return this option object
     */
    // These can only be used with a TopContainer with lifecycle...
    // Container will be shutdown normally after the specified timeout

    // Vi vil gerne have en version, vi kan refreshe ogsaa???
    // Maaske vi bare ikke skal supportered det direkte.

    // Teknisk set er det vel en artifact wirelet
    // Det er vel en der kan bruges hver gang vi har en dynamisk wiring. Og ikke kun apps...
    // Men, f.eks. ogsaa actors...

    // allowForLink() returns false // check(WireletPosition) <- if (wp == link -> throw new X)

    public static Wirelet timeToLive(long timeout, TimeUnit unit) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    public static Wirelet timeToLive(Duration duration) {
        // Duration is from Running transitioning...
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    public static Wirelet timeToLive(Instant deadline) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    public static Wirelet timeToLive(long timeout, TimeUnit unit, Supplier<Throwable> supplier) {
        timeToLive(10, TimeUnit.SECONDS, () -> new CancellationException());
        // Alternativ, kan man wrappe dem i f.eks. WiringOperation.requireExecutionMode();
        throw new UnsupportedOperationException();
    }

    // Den har et andet navn en CTRL_C...
    // shutdownHook() -> Adds a shutdown directly I think...
    public static Wirelet shutdownOnCTRL_C() {
        throw new UnsupportedOperationException();
    }

    public static Wirelet shutdownOnCTRL_C(Supplier<? extends Extension> withException) {
        throw new UnsupportedOperationException();
    }
}
