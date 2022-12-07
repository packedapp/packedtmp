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
package app.packed.context;

import app.packed.extension.Extension;

/**
 * A static context. By static we mean that is can be determined at runtime whether or not a given context is available
 * in a container, bean or operation.
 * 
 * 
 * @param <E>
 *            the extension the runtime context is a part of
 */
// StaticContext
public interface Context<E extends Extension<E>> {}
// Protected typeClass for configuration...


// Kan extension beans have it injected???? I don't think so....
// Only the owner of the ContextuablizedElement

// Ved ikke om det er problematisk at tage E med paa runtime...
// Den er jo ikke super needed. Og bliver jo encoded i klassen

//interface SchedulingContext extends RuntimeContext<ScheduledJobExtension> {}

//BuildTimeContext? IDK
