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
package sandbox.service;

import java.util.Optional;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.util.Key;
import sandbox.operation.mirror.BindingTarget;

/**
 *
 */

// Det ville vaere fedt paa en eller anden maade at kunne statisk beregne et eller andet
// der saa kunne vaere parameter

// https://stackoverflow.com/questions/47683465/how-do-i-get-the-binding-target-in-a-guice-provider

// En slags reducer -> ServiceProvisionMirror -> Parameter

// Vi kan ikke rigtigt tage et mirror... Hvorfor???
///SPM.printUsers()

// Tror ikke vi har den...
// Syntes det bliver for besvaergeligt

interface ServiceProvisionContext extends Context<BaseExtension> {
    Optional<BindingTarget> target();
    Key<?> key();
}
