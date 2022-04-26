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
package app.packed.container.sandboxextension;

import app.packed.container.Extension;

/**
 *
 */

// Ideen er lidt at vi har en meget barebone extension...
// Her putter vi ogsaa fx java.util.system.Logger-> LoggingExtension
// @JavaBaseSupport

// Tror vi tillader at man kan registrere en String istedet for en class
// Der er ingen grund til at loade classer som man ikke noedvendigvis vil bruge.
// Jo vi skal jo kende @DependsOn
abstract class BaseExtension<E extends Extension<E>> extends Extension<E> {

}
