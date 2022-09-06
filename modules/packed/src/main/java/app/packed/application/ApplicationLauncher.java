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
package app.packed.application;

import java.util.function.Function;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.managed.RunState;
import internal.app.packed.application.PackedApplicationDriver.PackedApplicationLauncher;

/**
 * An application image is a pre-built application that can be instantiated at a later time. By configuring an system
 * ahead of time, the actual time to instantiation the system can be severely decreased often down to a couple of
 * microseconds. In addition to this, images can be reusable, so you can create multiple systems from a single image.
 * <p>
 * Application images typically have two main use cases:
 * 
 * GraalVM Native Image
 * 
 * Recurrent instantiation of the same application.
 * 
 * Creating artifacts in Packed is already really fast, and you can easily create one 10 or hundres of microseconds. But
 * by using artifact images you can into hundres or thousounds of nanoseconds.
 * <p>
 * Use cases: Extremely fast startup.. graal
 * 
 * Instantiate the same container many times
 * <p>
 * Limitations:
 * 
 * No structural changes... Only whole artifacts
 * 
 * <p>
 * An image can be used to create new instances of {@link app.packed.application.App} or other applications. Artifact
 * images can not be used as a part of other containers, for example, via
 * 
 * @see App#build(Assembly, Wirelet...)
 * @see App#buildReusable(Assembly, Wirelet...)
 */

// Det er som default mange gange...
//// Og saa har vi single shot!!!

/// ApplicationImage<String> i; String numberOfFoos = i.launch();

//// Jeg tror ikke man kan mirror et application image...
//// Med mindre man bruger en speciel wirelet
// I virkeligheden er det jo bare det samme som at launch en Bean...
// Saa det er vel snare en tynd wrapper over en MH som tager en single parameter or type
// Wirelet[] wirelets

// Jeg er ikke vild med navnet launch
@SuppressWarnings("rawtypes")
public sealed interface ApplicationLauncher<A> permits PackedApplicationLauncher {

    default A checkedLaunch() throws ApplicationLaunchException {
        return checkedLaunch(new Wirelet[] {});
    }
    
    // Altsaa skal vi have en Bestemt Exception. Som har mere info??
    // ApplicationLaunchException
    A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException;

    
    /**
     * Launches an instance of the application that this image represents.
     * 
     * @throws RuntimeException
     *             if the application failed to launch
     * @throws IllegalStateException
     *             if the image has already been used to launch an application and the image is not a reusable image
     * @return the application interface if available
     */
    default A launch() {
        return launch(new Wirelet[] {});
    }

    default A launch(String[] args) {
        return launch(/* CliWirelets.args(args).andThen( */);
    }

    /**
     * @param args
     * @param wirelets
     * @return
     */
    default A launch(String[] args, Wirelet... wirelets) {
        return launch(/* CliWirelets.args(args).andThen( */wirelets);
    }

    /**
     * Launches an instance of the application that this image represents.
     * <p>
     * Launches an instance of the application. What happens here is dependent on application driver that created the image.
     * The behaviour of this method is identical to {@link ApplicationDriver#launch(Assembly, Wirelet...)}.
     * 
     * @param wirelets
     *            optional wirelets
     * @return an application instance
     */
    A launch(Wirelet... wirelets);

    /**
     * Returns a new application image that maps the result of the launch.
     * 
     * @param <E>
     *            the type to map the launch result to
     * @param mapper
     *            the mapper
     * @return a new application image that maps the result of the launch
     */
     <E> ApplicationLauncher<E> map(Function<A, E> mapper);
}

interface Zimgbox<A> {
    

    /**
     * Returns the launch mode of application(s) created by this image.
     * 
     * @return the launch mode of the application
     * 
     */
    // ApplicationInfo instead???
    RunState launchMode(); // usageMode??

    default boolean isUseable() {
        // An image returns true always

        // Optional<A> tryLaunch(Wirelet... wirelets)???
        return true;
    }

    // Hmmmmmmm IDK
    // Could do sneaky throws instead
    A throwingUse(Wirelet... wirelets) throws Throwable;

    default ApplicationLauncher<A> with(Wirelet... wirelets) {
        // Egentlig er den kun her pga Launcher
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a mirror for the application if available.
     * 
     * @param image
     *            the image to extract the application mirror from
     * @return a mirror for the application
     * @throws UnsupportedOperationException
     *             if the specified image was not build with BuildWirelets.retainApplicationMirror()
     */
    static ApplicationMirror extractMirror(ApplicationLauncher<?> image) {
        throw new UnsupportedOperationException();
    }

    // ALWAYS HAS A CAUSE
    // Problemet jeg ser er, hvad skal launch smide? UndeclaredThrowableException

    // App.execute
    // App.checkedExecute <---

    // Maaske er det LifetimeLaunchException
    public static class ApplicationLaunchException extends Exception {

        private static final long serialVersionUID = 1L;

        RunState state() {
            return RunState.INITIALIZED;
        }
    }
}