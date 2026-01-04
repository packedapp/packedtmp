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
package app.packed.application.containerdynamic;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.application.registry.ApplicationRegistryBeanConfiguration;
import app.packed.application.registry.LaunchableApplication.Launcher;
import app.packed.extension.BaseExtension;
import app.packed.service.ProvidableBeanConfiguration;

// Den er jo super general...
// Instance Manager

// Vi har kun et handle. Kan ikke se hvor

// Lad os sige vi har 23 session beans spredt ud og et container tree.

// Hvor er rooten? Giver det meningen med wirelets???


// Manages one or more instances, of a single
// Maaske kan den endda bruges som en simple ApplicationReposity.
// Hvis vi kun har et handle som vi kender paa build time...
public interface ContainerRegistry<I> {

    Optional<ManagedInstance<I>> managedInstance(String name);

    Stream<ManagedInstance<I>> managedInstances();

    Launcher<I> launcher();

    // Problemet her kan jo saa vaere noget namespace fis...
    // Naar vi skal launches shittet
    static <A, H extends ApplicationHandle<A, ?>> ProvidableBeanConfiguration<A> install(ApplicationTemplate<H> template, BaseExtension extension,
            Consumer<? super ApplicationInstaller<H>> installer) {
        throw new UnsupportedOperationException();
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRegistryBeanConfiguration<A, H> install(Class<H> handleKind, ApplicationTemplate<H> template,
            BaseExtension extension) {
        throw new UnsupportedOperationException();
    }
}
