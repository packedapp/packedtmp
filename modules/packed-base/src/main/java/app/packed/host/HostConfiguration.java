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
package app.packed.host;

import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.Extension;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;

/**
 *
 */
interface HostConfiguration<T> extends ComponentConfiguration<T> {

    // Ideen er at man kan sige hvem skal ligesom skinne igennem

    // Skal sgu maaske vaere specifikt...
    HostConfiguration<T> add(Extension e);

    <S extends HostConfigurator<?>> HostConfiguration<T> add(Class<S> cl, Consumer<S> c);

    HostConfiguration<T> add(Extension e, Object hostConfigurator);

    HostConfiguration<T> as(Key<? extends Host> key);
}

// Component
// -> Container
// -> Noneton
// -> Singleton
// -> Manyton
// -> Host
// -> Placeholder <- A component that just organizes other components

// Ville vaere fint hvis de havde hver deres forbogstav... kunne vaere let at vise dem
// grafisk saa. Maaske 2 bogstaver AS-> ActorSystem, AC -> Actor, AS -> AgentSystem??

// Singleton -> Listener, Service, 1 instance component

// ComponentInstanceMultiplicity
/// NONE, SINGLE, MANY

class Usage {
    void foo(HostConfiguration<?> hc) {
        hc.add(ServiceHostConfigurator.class, e -> e.export(Key.of(String.class)));
    }
}

class ServiceHostConfigurator implements HostConfigurator<ServiceExtension> {

    // Maybe just specify a contract

    public void export(Key<?> key) {
        throw new UnsupportedOperationException();
    }
}

interface HostConfigurator<E extends Extension> {

}