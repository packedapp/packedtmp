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
package internal.app.packed.container;

import app.packed.component.Authority;

/**
 * The owner of a bean. Either the application (via an assembly) or an extension instance.
 */
public sealed interface AuthoritySetup permits AssemblySetup, ExtensionSetup {

    /** {@return a realm representing the owner.} */
    Authority authority();

    /** {@return whether or not the authority entity is still configurable.} */
    boolean isConfigurable();
}
