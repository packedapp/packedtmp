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
package packed.internal.inject.util.nextapi;

import app.packed.service.Injector;

/**
 *
 */

// Why we need it...

// To get all services

// We need a clear separation between the outfacing injector and the internal injector...

// Replace with ContainerContext....

// Containeren har e
interface InjectorContext extends Injector {

    Injector publicInjector();// Hmm bliver svaer at extende mht til App

    // I don't want to call it services.....
}
