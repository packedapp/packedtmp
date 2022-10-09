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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.RunState;
import internal.app.packed.application.PackedApplicationDriver.MappedApplicationImage;
import internal.app.packed.application.PackedApplicationDriver.ReusableApplicationImage;
import internal.app.packed.application.PackedApplicationDriver.SingleShotApplicationImage;

/**
 * An application image is a pre-built application that can be launched at a later time.
 * <p>
 * By configuring an image ahead of time, the actual time to instantiation the system can be severely decreased often
 * down to a couple of microseconds. In addition to this, images can be reusable, so you can create multiple systems
 * from a single image.
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
 * @see App#newImage(Assembly, Wirelet...)
 */
// Hvorfor skal man egentlig ikke kunne extende den med sine egne ting

// rename to launcher and then image is a special type of launcher that can be used repeatable
@SuppressWarnings("rawtypes")
public sealed interface ApplicationLauncher<A> permits SingleShotApplicationImage, ReusableApplicationImage, MappedApplicationImage {

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
    // Maaske ikke usefull med custom launchers
    default <E> ApplicationLauncher<E> map(Function<? super A, ? extends E> mapper) {
        requireNonNull(mapper, "mapper is null");
        return new MappedApplicationImage<>(this, mapper);
    }
}

interface Zimgbox<A> {

//  /**
//  * Launches an instance of the application that this image represents.
//  * 
//  * @throws ApplicationLaunchException
//  *             if the application failed to launch
//  * @throws IllegalStateException
//  *             if the image has already been used to launch an application and the image is not a reusable image
//  * @return the application interface if available
//  */
// default A checkedLaunch() throws ApplicationLaunchException {
//     return checkedLaunch(new Wirelet[] {});
// }
//
// default A checkedLaunch(Wirelet... wirelets) throws ApplicationLaunchException {
//     throw new UnsupportedOperationException();
// }

    
    default boolean isUseable() {
        // An image returns true always

        // Optional<A> tryLaunch(Wirelet... wirelets)???
        return true;
    }

    Optional<ApplicationMirror> mirror();
    
    /**
     * Returns the launch mode of application(s) created by this image.
     * 
     * @return the launch mode of the application
     * 
     */
    // ApplicationInfo instead???
    RunState launchMode(); // usageMode??

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
    // Eller bare Optional<Mirror>
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