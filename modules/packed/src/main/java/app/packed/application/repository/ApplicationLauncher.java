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
package app.packed.application.repository;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 * Could be a inner interface on the repository. Although som
 */
// Handlers can be used across multiple application instances.
// So we need to be able to uninstall them  per application instance

// Add H?

// Hvor meget er applications specific og hvor meget kunne genbruges for en Container???
// Problemet her kan jo saa vaere noget namespace fis...
// Naar vi skal launches shittet

public interface ApplicationLauncher<I> {

    /**
     * Disables all future launch from this launcher.
     * <p>
     * Trying to lunch a new application, will fail with {@link IllegalStateException}.
     */
    // Hvad saa med alle dem der er i gang??? Fx er de i initialized
    // Det kraever ihvertfald at alle instancer selv dem der ikke er running, er med i instances.
    // Saa vi kan lukke dem ned
    void disableLaunch();

    /** {@return the launchers underlying (unconfigurable) handle} */
    ApplicationHandle<?, ?> handle();

    /** {@return an instance with the specified name if it exists} */
    Optional<ManagedInstance<I>> instance(String name);

    /** {@return a stream of all managed instances for this application} */
    Stream<ManagedInstance<I>> instances();

    boolean isAvailable();

    GuestLauncher<I> launcher();

    I startGuest(Wirelet... wirelets);

    /**
     *
     * @throws UnsupportedOperationException
     *             if used stand-alone outside of an {@link app.packed.application.ApplicationRepository}.
     */
    default void uninstall() {

    }

    // Simple version of ApplicationRepository. Only thing is mirrors where we may now have multiple versions...
    static <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<ApplicationLauncher<I>> install(ApplicationTemplate<H> template,
            Consumer<? super ApplicationInstaller<H>> installer, BaseExtension extension) {

        throw new UnsupportedOperationException();
    }

    static <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<ApplicationLauncher<I>> provide(ApplicationTemplate<H> template,
            Consumer<? super ApplicationInstaller<H>> installer, BaseExtension extension) {
        return install(template, installer, extension);
    }
}
