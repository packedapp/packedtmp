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
package app.packed.inject;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Set;

import app.packed.util.Key;

/**
 *
 */
class InjectorFactory {

}

class BundleX {

    // Can we create a AppFactory as well???
    // Maaske skal man lave et bundle, som har required services...

    // Man burde ogsaa kunne lave nogle assisted inject factories....
    // Hvor man kan lave en instance af X

    // Istedet for settet skal vi have saadan en modifiere som vi bruger andre steder
    protected void addNewInstanceLookup(MethodHandles.Lookup lookup) {

    }

    // ConfigSource is disabled per default
    protected <T> ServiceConfiguration<T> registerInjectorFactory(Class<T> injectorFactory, Set<Key<?>> keysToInheritFromParent) {
        // key = class, description = Interface factory for xxxxx
        throw new UnsupportedOperationException();
    }

    protected void registerInjectorFactory(Class<?> injectorFactory) {
        registerInjectorFactory(IFactory.class); // <--- IFactory can be injected like a normal service...
    }

    // the factory is spawned from internal services
    // Problemer er maaske at ikke kan definere endnu et injector factory level uden at angive en bundle....

    interface IFactory {
        Injector a(Request request, Response response);

        Injector b(Request request, Response response);

        // Parameters can override existing services
        Injector b(Request request, Response response, String overridesExistingStringService);

        Somebody c(Request request, Response response);

        // Fails if Request is not available for each method
        @Provides
        default LocalDate now(Request request) {
            throw new UnsupportedOperationException();
        }
    }

    // Alternativ have et interface eller lignende for f.eks. AbstractHost, InjectorFactory
    // Men okay, saa kan vi ikke angive parametere...

    interface Request {}

    interface Response {}

    interface Somebody {}

    // Maaske kan vi registerere det her med en host ogsaa...
    // F.eks. i henhold til Sessions. Hvor vi registererer dem som apps.

}