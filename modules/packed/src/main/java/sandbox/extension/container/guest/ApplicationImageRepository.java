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
package sandbox.extension.container.guest;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationTemplate;

/**
 *
 */
// Tror vi er fuldstaendige ligeglade om der koerer nogle application der bruger imaged.
// Det er kun nye applicationer..
public interface ApplicationImageRepository {

    // Hah vi laver jo ikke en ComponentGuest her...
    // Ellers maaske goer vi... Maaske er det i virkeligheden en ApplicationHandle vi faar???
    // Maaske er det i virkeligheden det vi arbejder med.
    // Altsa vi vil maaske gerne skralde det af saa vi kun har MHs

    // Syntes ikke vi skal returnere en standard ApplicationInstaller...

    ApplicationTemplate.Configurator deploy(ApplicationTemplate template);

    Map<String, ComponentGuest> allGuests(); //idk

    Optional<ComponentGuest> get(String name);

    Stream<ComponentGuest> guests();

    void remove(ComponentGuest guest);
}
