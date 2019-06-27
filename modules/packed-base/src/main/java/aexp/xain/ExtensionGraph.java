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
package aexp.xain;

import java.util.Optional;

import app.packed.container.Extension;

/**
 *
 */
interface ExtensionGraph<T extends Extension<T>> {

    Optional<T> parentOf(T extension);

    Optional<T> firstAncestorOf(T extension);

}
// 1
// --2
// --3
// --4

// 2 +4 Defines JMX
// Skal vel ikke vaere taendt med mindre parent'en bestemmer det....

// Tror ikke vi har behov for recursive... Alt skal defineres

// JMX -> uses firstAncestorOf or Host.AttachmentMap
// Injector -> Uses firstParent or Host.AttachmentMap
// Lifecycle -> Checks Parent if none... Fail
// Maybe just have parent...

// ErrorHandling???? <- Hierachical
// AOP???
// Logging
// Metrics

//// ErrorHandlingExtension()`