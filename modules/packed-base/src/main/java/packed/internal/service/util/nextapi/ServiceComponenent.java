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
package packed.internal.service.util.nextapi;

import java.util.Collection;

import app.packed.config.ConfigSite;
import app.packed.service.ServiceDescriptor;

/**
 *
 */
// Det er ikke meningen folk selv skal lave descriptor imodsaetning til contracts.
// De skal heller ikke extendes....

// De er primaert taenkt til at vaere taet knyttet til f.eks. en Injector

// injector.descriptor();

// Den var tiltaenkt til at vise alle services paa en component...
// Alternativt, ala features
public interface ServiceComponenent {

    ConfigSite configSite();

    Collection<ServiceDescriptor> services();

    // Map<Key, ServiceDescriptor>

    // InjectorContract contract();

}
