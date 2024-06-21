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
package app.packed.build;

import app.packed.assembly.Assembly;
import app.packed.build.hook.BuildHook;
import app.packed.extension.Extension;

/**
 * All applications are build on the basis of three different types of build sources. The different types of sources
 * are:
 *
 * <p>
 * Where the "root" build source is always a single {@link Assembly}. Hmm what about
 *
 * <p>
 * Typically, assemblies are created by the application developer. Extensions are primary resuable, but in some
 * situations application developers will need to create their own. And build transformers are a mix of the two.
 */
/// Hmm Delegating Assembly??? Kan jo ikke execute noget. Taenker det kun er ting der har fat i en XConfiguration
// Men maaske fint at have her for at goere det simpler. Hvis man vil branche ud...
// Maybe only have BuildSourceMirror.

// BuildCodeSource (I virkeligheden er det jo Bytecode)
// Var BuildSource... Maaske er det bedre
// To Abstract Class?
public sealed interface BuildCodeSource extends BuildSource permits Assembly, Extension, BuildHook {}

// Det her er lige Imperitive sources, Men vi har ogsaa declarive ones, Class (Bean) eller conf file (may be from Network)
////Hvad med fx Environment Variables. Der boer System.Properties vel veare en Source. Det samme med Environment

// Is a bean a source??????????????
// I mean it kind of is
// It is a declaritive source. And the others are imperitive source
// What about a configuration file??? .name = x...

// ========== Building
// An application starts with a single Assembly.
// And then you kind of adds sources (for example, extension, assemblies, transformers, classes, config files)

// ======================= Build Delegate
//Should probably add BuildDelegate at some point. Ideen er at man kan lave nogle
//templates der inkludere beans og korrekt styre
//Soeg efter BuildDelegate i incubator

//Den er jo ikke kun build step. Det er også ejer af ting...
//Maaske har vi ikke author, men build step istedet for...

//Is it ComposerStep????
//For example, delayed Codegen kunne også vaere et build step

//Maybe BuildTask is better? But then again Extensions are 2 tasks really

//ContainerHook er jo en slags BuildStep...
//Extension har sådan set 2 build steps.....
//ComponentTransformers kan ogsaa have flere startes

//Motherfucker AssemblyTransformer

// Problemet med BuildSourceMirror er ExtensionMirror som jo er per instance og ikke per ExtensionTree...
// Men det er maaske ogsaa fint man kan se instancen...

// Omvendt tror jeg BuildSource som saadan ikke giver nogen mening kun selve mirror'et
// Men vi har den lige her for at taenke over det

//Are bean hooks a BuildSource?? I would think no
