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

import app.packed.base.Completion;
import app.packed.component.Assembly;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.component.drivers.ArtifactDriver;
import app.packed.container.BaseAssembly;
import app.packed.exceptionhandling.BuildException;

/**
 * A specialization of {@link Image} that is targeted use from the main method of a Java program. This is typically used
 * for running GraalVM native image.
 * 
 * @see Main
 */
// Optimized for running once

// Kan have en masse specielle metoder... Som kun er targeted main 

// @UseSiteIgnoreForStackWalks (ClassValue er O(1) i fremtidige versioner)
// Ideen er lidt at kunne annotere allerede klasser som ikke er direkte Assembly bruger klasser...
// IDK

// Typically used for CLI applications
public /* primitive */ final class MainImage {

    /** The image we are wrapping. */
    private final Image<Completion> image;

    /**
     * Creates a new main image.
     * 
     * @param assembly
     *            the assembly
     * @param wirelets
     *            optional wirelets
     */
    private MainImage(Assembly<?> assembly, Wirelet... wirelets) {
        this.image = Main.driver().buildImage(assembly, wirelets);
    }

    /**
     * A helper method that makes it easier to provide command-line arguments to your program. Is typically used from a
     * program's main method:
     * 
     * <pre> {@code
     * private final static Image<Void> MAIN = Main.imageOf(new SomeAssembly());
     *
     * public static void main(String[] args) {
     *   MAIN.use(args, any additional wirelets...);
     * }}</pre>
     * <p>
     * Invoking this method is identical to invoking
     * {@code image.use(assembly, Wirelet.combine(MainArgs.of(args), wirelets))}.
     * 
     * @param args
     *            command line arguments
     * @param wirelets
     *            optional wirelets
     * @return the result of using the image
     * @throws IllegalStateException
     *             if the image has already been used
     */
    public void use(String[] args, Wirelet... wirelets) {
        image.use(Wirelet.combine(MainArgs.of(args), wirelets));
    }

    public void use(Wirelet... wirelets) {
        image.use(wirelets);
    }

    /**
     * Creates a new main image from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to use for building the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @throws BuildException
     *             if the image could not be build
     * @see ArtifactDriver#buildImage(Assembly, Wirelet...)
     */
    public static MainImage of(Assembly<?> assembly, Wirelet... wirelets) {
        return new MainImage(assembly, wirelets);
    }
}

class MyAppMain extends BaseAssembly {
    private static final MainImage MAIN = MainImage.of(new MyAppMain());

    protected void build() {
        installInstance("HelloWorld");
    }

    public static void main(String[] args) {
        MAIN.use(args);
    }
}