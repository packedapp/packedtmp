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
package app.packed.bean;

import app.packed.operation.OperationMirror;

/**
 * A mirror for an operation that creates a new instance of a bean.
 * <p>
 * The operator of this operation is always {@link BeanExtension}.
 */
public class BeanInstantiationOperationMirror extends OperationMirror {}

// Hvis jeg register en instance har min bean ikke en
// Men factory og non-static class har altid en
// En void eller static bean har aldrig en

// Operatoren er vel altid operateren af lifetimen?
// Hmm hvad med @Conf <--- Her er operatoren vel ConfigExtension
// det betyder at operatoren maa vaere BeanExtension hvilket vel er aligned
// med @OnInitialize