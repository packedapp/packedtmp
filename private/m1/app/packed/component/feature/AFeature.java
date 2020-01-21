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
package app.packed.component.feature;

import java.util.List;
import java.util.Map;
import java.util.Set;

import app.packed.base.reflect.ConstructorDescriptor;
import app.packed.base.reflect.FieldDescriptor;
import app.packed.base.reflect.MethodDescriptor;
import app.packed.component.Component;
import app.packed.service.ServiceDescriptor;

// RuntimeEnviroment != BuildEnvironment
/**
 *
 */
// A= fra component, B = fra ComponentContext

// Do we want to use it as a key... Or a class
//// I think it depends
/// Can
// Maybe we can opti
// <ContainerConfiguration, Component, ComponentContext>
public abstract class AFeature<A, B> {

    protected A fromComponent(Component c) {
        // return from AttributeMap....
        // Det hjaelper os dog ikke med at iterere...
        throw new UnsupportedOperationException();
    }

    protected abstract A empty();
}

class Inj {

    // component.get(ComponentDependencyFeature.class)

    static final AFeature<Set<ServiceDescriptor>, Set<ServiceDescriptor>> SERVICES = new AFeature<>() {

        @Override
        protected Set<ServiceDescriptor> empty() {
            return Set.of();
        }
    };
}

// En BundleDependencyDescriptor er jo bare Map<Component, ComponentDependencyDescriptor>
/// Dependency kan jo altsaa vaere andre ting en Services......
interface ComponentDependencyDescriptor {

    // Fungere ikke rigtig

    // Vi vil gerne have en list over alle mandatory dependencies
    // Alle optional dependencies...

    //// Her traekker vi jo automatisk Context objekter

    Map<FieldDescriptor, Dependency> fields();

    Map<ConstructorDescriptor<?>, List<Dependency>> constructor();

    Map<MethodDescriptor, List<Dependency>> methods();
    // Field -> Dependency

    // Method -> *Dependency
}

interface Dependency {}
// Start all Fully
// Start all Lazy
// Start These Fully, and these lazy
// Lazy start All NOW, dont await on any

// Each Service
// Lazy Start
// Start Async/prestart (app.start() does not await on these....)
// Start Blocking

//// Lazy could also just be first line of methods, startIfNotStarted();
//// Obviously this will only work with Project Loom. Because
