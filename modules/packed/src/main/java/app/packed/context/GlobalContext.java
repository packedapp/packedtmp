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

import app.packed.extension.BaseExtension;

// All operations are automatically in the global context.
// However this is never mentioned when querying for context mirrors? Or maybe it is

// Can you inject it? Why not

// Maybe ApplicationContext?
// Man specificere da bare Context.class
/**
 * A special marker interface that can be used with {@link ServiceResolver#contexts()} to indicate that only services
 * with no context should be resolved.
 */
interface GlobalContext extends Context<BaseExtension> {}