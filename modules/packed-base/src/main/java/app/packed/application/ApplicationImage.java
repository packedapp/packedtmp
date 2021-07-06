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

import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.state.sandbox.InstanceState;
import packed.internal.application.PackedApplicationDriver.PackedApplicationImage;

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
 * An image can be used to create new instances of {@link app.packed.application.Program} or other applications.
 * Artifact images can not be used as a part of other containers, for example, via
 * 
 * @see Program#newImage(Assembly, Wirelet...)
 */

// Det er som default mange gange...
//// Og saa har vi single shot!!!

/// ApplicationImage<String> i; String numberOfFoos = i.launch();

@SuppressWarnings("rawtypes")
public sealed interface ApplicationImage<A> permits PackedApplicationImage {

    default boolean isAvailable() {
        // An image returns true always

        // Optional<A> tryLaunch(Wirelet... wirelets)???
        return true;
    }

    /**
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
     * Returns the launch mode of this image. The launch mode is the runmode target
     * <p>
     * The launch mode can be overridden by specifying a launch mode wirelet using
     * {@link ApplicationRuntimeWirelets#launchMode(InstanceState)}.
     * 
     * @return the launch mode of the application
     * 
     * @see ApplicationDriver#launchMode()
     */
    InstanceState launchMode();

    // fra launchable...
    /**
     * Returns a mirror for the application.
     * 
     * @return a mirror for the application
     * @throws IllegalStateException
     *             if the image is not {@link #isAvailable() available}.
     * @throws UnsupportedOperationException
     *             if mirroring is disabled
     */
    default ApplicationMirror mirror() {
        throw new UnsupportedOperationException();
    }
}

interface ZImage<A> {
    // Hmmmmmmm IDK
    // Could do sneaky throws instead
    A throwingUse(Wirelet... wirelets) throws Throwable;
}