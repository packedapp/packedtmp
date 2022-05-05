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

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifecycle.RunState;
import packed.internal.application.PackedApplicationDriver.PackedApplicationLauncher;

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
 * @see App#newLauncher(Assembly, Wirelet...)
 * @see App#newReusableLauncher(Assembly, Wirelet...)
 */

// Det er som default mange gange...
//// Og saa har vi single shot!!!

/// ApplicationImage<String> i; String numberOfFoos = i.launch();

//// Jeg tror ikke man kan mirror et application image...
//// Med mindre man bruger en speciel wirelet

@SuppressWarnings("rawtypes")
public sealed interface ApplicationLauncher<A> permits PackedApplicationLauncher {

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
     * The behavior of this method is identical to {@link ApplicationDriver#launch(Assembly, Wirelet...)}.
     * 
     * @param wirelets
     *            optional wirelets
     * @return an application instance
     * @see {@link ApplicationDriver#launch(Assembly, Wirelet...)}
     */
    A launch(Wirelet... wirelets);

    /**
     * Returns the launch mode of application(s) created by this image.
     * 
     * @return the launch mode of the application
     * 
     * @see ApplicationDriver#launchMode()
     */
    RunState launchMode(); // usageMode??
}

// Man maa lave sit eget image saa
// Og saa i driveren sige at man skal pakke launch 
interface ZImage<A> {
    // Hmmmmmmm IDK
    // Could do sneaky throws instead
    A throwingUse(Wirelet... wirelets) throws Throwable;
}

interface Zimgbox<A> {

    default boolean isUseable() {
        // An image returns true always

        // Optional<A> tryLaunch(Wirelet... wirelets)???
        return true;
    }

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
}