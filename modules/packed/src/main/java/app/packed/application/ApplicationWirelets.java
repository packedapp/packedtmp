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

import app.packed.container.Wirelet;
import internal.app.packed.container.InternalWirelet;
import internal.app.packed.container.PackedContainerBuilder;

/**
 * Wirelets that can be used when building an application.
 * <p>
 * Attempting to use any of these wirelets on anything else then when building an exception throws WireletNotApplicable
 */
// Or maybe ApplicationWirelets and then
// reusableImage();
// delayedImage();
public final class ApplicationWirelets {
    private ApplicationWirelets() {}

    /**
     * Does not build the application until it is needed.
     *
     * @return a wirelet
     */
    // Fungere kun for image, eller non-root applications

    // Hah, hvad med application mirror...
    public static Wirelet lazyBuild() {
        class LazyApplicationBuild extends InternalWirelet {
            static final LazyApplicationBuild INSTANCE = new LazyApplicationBuild();
            /** {@inheritDoc} */
            @Override
            public void onInstall(PackedContainerBuilder builder) {
                checkIsApplication(builder, this); // maybe explicit error msg
                builder.optionBuildApplicationLazy = true;
            }
        }
        return LazyApplicationBuild.INSTANCE;
    }

    // Start codegen in background threads

    // spawn 10 threads, that creates method handles...
    // There are a couple of strategies
    // Normally we eagerly generate all code
    /// lazyCodegen().guardBy(System.getProperty("sdfsdf")=="noLazy");
    // will build the application, but not generate the code until last minute
    static Wirelet lazyCodegen() {
        // lazyBuild will always triump
        throw new UnsupportedOperationException();
    }

    // A build exception is never retryable
    static Wirelet alwaysThrowBuildException() {
        throw new UnsupportedOperationException();
    }

    /**
     * By default images created using {@link App#imageOf(app.packed.container.Assembly, Wirelet...)} and similar methods
     * can only be used a single time. Once launched, the
     * <p>
     * By specifying this wirelet when creating an application image. The image can be used any number of times.
     * <p>
     * Specifying this wirelet when launching an application immediately create application mirrors
     *
     * @return
     * @see App#imageOf(app.packed.container.Assembly, Wirelet...)
     */
    // I actually think this is only useful for root images
    public static Wirelet resuableImage() {
        // Vi har droppet at lave flere metoder imageOf, imageResuableOf
        // Vi laver et image eagerly, og det kan launches 1 gang.
        // Saa faar vi fail-faster vi hvis vi proever fx at launche twice
        class ReusableApplicationImage extends InternalWirelet {
            static final ReusableApplicationImage INSTANCE = new ReusableApplicationImage();

            /** {@inheritDoc} */
            @Override
            public void onInstall(PackedContainerBuilder builder) {
                checkIsApplication(builder, this); // maybe explicit error msg
                builder.optionBuildReusableImage = true;
            }
        }
        return ReusableApplicationImage.INSTANCE;
    }
}

//// Can only be used together with image
//public static Wirelet delayedBuild() {
//  // delayed vs lazyBuild?
//  throw new UnsupportedOperationException();
//}