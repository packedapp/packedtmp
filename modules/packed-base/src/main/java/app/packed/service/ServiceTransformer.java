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
package app.packed.service;

import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;

/**
 *
 */
// Altsaa størstedelen af wirelets kan jo bare wrappe saadan en....

// Vil sige at hvert skridt i wirelets transfomration.
// Skal resultere i unikke keys

// Det er jo mere eller mindre...
// de her compute ting
// Tror altsaa bedre jeg kan lide den end wirelets...

// ServiceComputer... nah
public interface ServiceTransformer {

    Service map(Class<?> from, Class<?> to);  //Make returned Service Configurable???

    /**
     * Returns all services that can be transformed.
     * 
     * @return all services that can be transformed
     */
    ServiceRegistry services();
}
