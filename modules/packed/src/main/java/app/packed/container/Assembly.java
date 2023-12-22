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
package app.packed.container;

import app.packed.container.AbstractComposer.ComposableAssembly;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.PackedContainerBuilder;

/**
 * Assemblies are the basic building block for creating applications in the framework.
 * <p>
 * An assembly provides the concrete instructions for creating an application, and every application is constructed
 * either directly or indirectly from one or more assemblies. As such a single assembly can either comprise the entire
 * application or serve as the root of an assembly hierarchy, in which each node is responsible for building a part of
 * the application.
 * <p>
 * Assemblies does not have a runtime representation. They are a strictly build-time construct. Which is also why they
 * are not considered as a component.
 *
 * state are not carried over to the runtime. And any state that must be carried over to the runtime. Must be done so
 * doing the build process.
 * <p>
 * Assemblies provide a simply way to package components and build modular application. This is useful, for example,
 * for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * (DELETE ME) For more complicated needs an application can itself be split into a hierarchy of application nodes with
 * a single application as the root.
 *
 * <p>
 * There are currently two types of assemblies available:
 * <ul>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about services, and creates injector instances using
 * .</li>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about both services and components, and creates
 * container instances using .</li>
 * </ul>
 * A assembly instance can be used as part of exactly one ({@link #build()} build-process). Attempting to reuse an
 * assembly will fail with {@link IllegalStateException}.
 * <p>
 * This class cannot be extended directly. If you are developing an application on top of the framework, you would
 * typically extend {@link BaseAssembly} instead.
 */
public sealed abstract class Assembly permits BuildableAssembly, DelegatingAssembly, ComposableAssembly {

    /**
     * Invoked by the runtime (via a MethodHandle) to build the assembly.
     *
     * @param builder
     *            a container builder for the root container of the assembly
     * @return an assembly configuration object
     *
     * @apiNote this method is for internal use only
     */
    abstract AssemblySetup build(PackedContainerBuilder builder);

    /** The state of an {@link Assembly}. */
    public enum State {

        /** The assembly has not yet been used in a build process. */
        BEFORE_BUILD,

        /** The assembly is currently being used in a build process. */
        IN_USE,

        /** The assembly has already been used in a build process (either successfully or unsuccessfully). */
        AFTER_BUILD;
    }
}
