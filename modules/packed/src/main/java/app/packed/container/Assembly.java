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

import app.packed.container.AbstractComposer.ComposerAssembly;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.container.AssemblySetup;

/**
 * An assembly is the basic building block for creating applications in Packed.
 * <p>
 * An assembly provides the instructions for creating an application, and every application in Packed is constructed
 * either directly or indirectly from an assembly. A single assembly can either comprise the entire application or serve
 * as the root of an assembly hierarchy in which each node is responsible for building a part of the application.
 * <p>
 * Assemblies provide a simply way to package components and build modular application. This is useful, for example,
 * for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of assemblies available:
 * <ul>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about services, and creates injector instances using
 * .</li>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about both services and components, and creates
 * container instances using .</li>
 * </ul>
 *
 * A assembly instance can be used ({@link #build()}) exactly once. Attempting to use it multiple times will fail with
 * an {@link IllegalStateException}.
 *
 * <p>
 * For more complicated needs an application can itself be split into a hierarchy of application nodes with a single
 * application as the root.
 * <p>
 * This class cannot be extended directly, instead you should typically extend {@link BaseAssembly} instead.
 */
@SuppressWarnings("rawtypes") // Eclipse bug
public sealed abstract class Assembly permits BuildableAssembly, DelegatingAssembly, ComposerAssembly {

    /**
     * Invoked by the runtime (via a MethodHandle) to build the assembly.
     *
     * @param builder
     *            a builder for the root container of the assembly
     * @return an assembly configuration object
     */
    abstract AssemblySetup build(PackedContainerBuilder builder);
}
