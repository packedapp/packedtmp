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
package app.packed.application.registry.v2;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;

/**
 *
 */
// Tror ideen er at alt ligger i det vi gemmer i I.
// Og så må I være noget specielt En slags super image...

// Så den kan bruges både af managed or unmanaged

// maybe I extends ApplicationImage (name(), uninstall) ApplicationInterface, ApplicationImage, ApplicationLauncher, ...

// image(sf).

// Så er basically bare en Collection
public interface ApplicationRepository<I extends ApplicationImage, H extends ApplicationHandle<I, ?>> extends Iterable<I> {

    // I::instances()
    <R> Stream<R> flatMap(Function<? super I, ? extends Stream<? extends R>> mapper);

    /**
     * {@return an application with the specified name if present in this repository, otherwise empty}
     *
     * @param name
     *            the name of the application
     */
    I image(String name);

    /**
     * Installs a new application in the repository based on {@link #template()}.
     * <p>
     * Applications can be uninstalled by calling {@link InstalledApplication#uninstall()}.
     * <p>
     * If the application in which the repository is located is shutdown while calling this method. The returned application
     * will report a state of XXXXXXX?
     *
     * @return the installed application
     * @throws RuntimeException
     *             if the application failed to build, or could not be installed
     */
    I install(Consumer<? super ApplicationInstaller<H>> installer);

    /** {@return a stream of all installed applications in this repository} */
    Stream<I> stream();

    /** {@return the underlying application template that every application in this repository must use} */
    ApplicationTemplate<H> template();
}
