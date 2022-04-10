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

import app.packed.container.Assembly;
import app.packed.container.ComposerAction;
import app.packed.extension.Extension;

/**
 *
 */
@SuppressWarnings("rawtypes")
// ComponentRealm
public sealed interface ComponentRealm permits Assembly,Extension,ComposerAction {}
// A realm can be closed...

// Assembly realm -> when build returns
// Extension realm -> when the application is in the last phase
// Composer action realm -> when build returns

// Nu naar vi faar Application beans med application scope...
// Giver det jo ikke rigtig mening at sige den er installeret via denne Assembly.