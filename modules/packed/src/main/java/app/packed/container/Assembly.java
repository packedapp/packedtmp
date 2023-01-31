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

/**
 * <p>
 * An assembly encapsulates the instructions to build an application or parts hereof.
 *
 *
 * <p>
 * An assembly can statically link other assemblies, typically by calling
 * {@link ContainerConfiguration#link(Assembly, Wirelet...)}. Paa den
 *
 * <p>
 * This class cannot be extended directly, instead you should typically extend {@link BaseAssembly} instead.
 *
 *
 *
 */
@SuppressWarnings("rawtypes")
public sealed abstract class Assembly permits BuildableAssembly, DelegatingAssembly, ComposerAssembly {}
/*
 *
 * Assemblies contain instructions on how to build an application
 *
 * Assemblies are the main way
 *
 * An assembly is basically instructions on how to create an application. And all applications in Packed are created
 * either directly or indirectly from an Assembly. A single assembly either forms the whole or application or is linkage
 * in an tree. In such a way as the assemblies form a tree.
 *
 * Every component of an application has an assembly where they where configured.
 *
 */
