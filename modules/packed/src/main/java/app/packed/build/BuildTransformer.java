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

import app.packed.application.ApplicationTransformer;
import app.packed.assembly.AssemblyTransformer;
import app.packed.assembly.OperationTransformer;
import app.packed.bean.BeanTransformer;
import app.packed.container.ContainerTransformer;

/**
 *
 * <p>
 * A build transformer should be safe for multi threaded access.
 * <p>
 * If you need to store shit use locals
 *
 */
// Two ways to a

// Link
// -- MyAssTran.preBuild
// ---- Link(Ass)
// ....
// - Assembly.Build

// Application???
// Container
// Bean
// Operation
// Binding???
//// WebNamespace

// Can be applied as "ClientProxy" <-- Maybe this is Augmentation

// Can be applied as @BeanHook
// Can be applied as @AssemblyHook

//Can be applied as AssemblyTransformer

// Questions
//// 1. Stateless  (+ Locals) vs Statefull Must
//// 2. Pre/post? More control.
//// 3. Context or protected methods?

// Hook -> Match + Transformer

// BuildTransformer???

// Would be nice to have a way to fx apply @Debug everywhere.
// A readonly transformer

// Ville måske være godt at kunne få info ned...
// Ellers må vi jo have oplysningerne på ContainerConfiguration og så tage den med.

// Det ville være rigtig fedt at kunne se hvem der havde transformeret hvad

// Det ville også være fint at have hvem og hvad seperaret

// We don't actually transforming anything just prepare it
// Would be nice to able
// I think we are returning an delegating assembly

// I think delegating assembly may allow hooks annotations. But must be open!!
// No maybe this is simply the way we support it for now..

// We need some cool examples.
// Like print everytime a bean is instantiated

// Maybe it is instrument\
// Maaske optrader det en synthetics delegated assembly

// Recursively er specielt...
public sealed interface BuildTransformer extends BuildSource
        permits ApplicationTransformer, AssemblyTransformer, ContainerTransformer, BeanTransformer, OperationTransformer {}
