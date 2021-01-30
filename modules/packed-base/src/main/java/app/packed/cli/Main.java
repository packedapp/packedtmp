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
package app.packed.cli;

import app.packed.component.App;
import app.packed.component.ArtifactDriver;
import app.packed.component.Assembly;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.state.RunState;
import app.packed.state.StateWirelets;

/**
 *
 */
public final class Main {

    /** The artifact driver used by this class. */
    private static final ArtifactDriver<Void> DRIVER = ArtifactDriver.daemon().with(StateWirelets.shutdownHook());

    /** Not today Satan, not today. */
    private Main() {}

    public static Image<Void> buildImage(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().buildImage(assembly, wirelets);
    }

    public static ArtifactDriver<Void> driver() {
        return DRIVER;
    }

    // rename to execute?
    public static void run(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        driver().use(assembly, MainArgs.of(args).andThen(wirelets));
    }

    /**
     * This method will create and start an {@link App application} from the specified source. Blocking until the run state
     * of the application is {@link RunState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * <p>
     * This method will automatically install a shutdown hook wirelet using
     * {@link StateWirelets#shutdownHook(app.packed.state.Host.StopOption...)}.
     * 
     * @param assembly
     *            the assembly to execute
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     * @see StateWirelets#shutdownHook(app.packed.state.Host.StopOption...)
     */
    public static void run(Assembly<?> assembly, Wirelet... wirelets) {
        driver().use(assembly, wirelets);
    }
}
