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
package app.packed.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attempts to find a wirelet of targets type.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Collect, Receive, Accept, Consume

// Grunden til jeg ikke kan WireletInject er den kan puttes paa en parameter...
// Men det kan @Inject ikke.
// @Nullable, Optional, List
// VarHandle, MethodHandle doesn't really work with WireletHandle...
// methods are conditional invoked.....

// Hmm Hvad hvis jeg har foo(Optional<EEE>)
// Skal den virkelig invokes alligevel???
//  

// @WireletLink...  Nah @WireletLink Optional<>
public @interface WireletReceive {}

/// Metode??? det giver jo god mening...
/// Men maaske hellere i forbindelse med @Initialize