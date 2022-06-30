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
package internal.app.packed.inject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.inject.service.build.ServiceSetup;
import internal.app.packed.lifetime.LifetimePoolSetup;

/**
 * Something that
 * 
 * {@link BeanSetup} for methods or fields that needs an instance of the component source
 * 
 * 
 * {@link ServiceSetup} a service of some kind
 */
public interface DependencyProducer {

    /**
     * Returns a method handle that can be used to access a dependency. This is typically done by either having previously
     * stored a constant in {@link MethodHandles#constant(Class, Object)}. A prototype as bla bla
     * 
     * A sidecar
     * 
     * A constant that is created an initialization time
     * 
     * The returned method handle takes a single argument of type {@link LifetimePoolSetup}. And returns an instance of the
     * dependency.
     * <p>
     * If the instance is cached by the runtime. the returned method handle must uphold it
     * 
     * @return a method handle that can be use to create an instance of the dependency
     */
    // was toMethodHandle.. Men vi har ligesom 2 slags. Dem der accessor "raw" og saa dem der tager hensyn
    // til om en instance er cached et sted
    MethodHandle dependencyAccessor();

    /**
     * Returns a dependency consumer, if this producer itself needs dependencies fulfilled. Returns null if it requires no
     * dependencies.
     * <p>
     * This method is primarily used in connection with discovering cycles in the dependency graph.
     * 
     * @return an injectable if this dependency provider itself needs dependencies fulfilled, otherwise null
     */
    @Nullable
    DependencyNode dependencyConsumer();
}
