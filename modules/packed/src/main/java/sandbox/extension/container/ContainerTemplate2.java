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
package sandbox.extension.container;

import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.extension.Extension;
import app.packed.operation.OperationType;

/**
 *
 */
public interface ContainerTemplate2 {

    Optional<OperationType> adaptorFactory(); // Behoever faktisk ikke guestAdaptor() saa. Return type er jo det

    Optional<Class<?>> guestAdaptor(); // What if Op? Maybe just target class

    Map<Key<?>, Link> links();

    boolean isManaged();

    // Har vi en special MaybeLoader? Altsaa hvis vi har banned extensions.. Er der ingen grund til at loaded den
    SequencedSet<Class<? extends Extension<?>>> requiredExtensions();

    // Nydus Canal
    // Extension
    interface Link {

        static Link of(Consumer<? super Configurator> configure) {
            throw new UnsupportedOperationException();
        }

        // Or maybe Just LinkConfigurator on the class
        interface Configurator {

        }
    }
}
