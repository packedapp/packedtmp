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
package app.packed.extension.context;

import app.packed.context.Context;

/**
 *
 */
// Hmm, fungere ikke super godt sammen med ContextValue
// Skal vi Koere @From(HttpContext.class) HttpRequest?
// Eller bare HttpRequest

// Tror faktisk den er super forvirrende
@Deprecated(since = "Didn't really work")

// Den virker saadan set. Det er mere en usability issue.
//

public @interface From {
    Class<? extends Context<?>> value();
}
