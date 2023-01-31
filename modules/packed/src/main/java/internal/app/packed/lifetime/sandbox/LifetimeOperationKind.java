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
package internal.app.packed.lifetime.sandbox;

/**
 *
 */
// Tror kun der findes de her 4 faste operationer


// Vi har LifetimeKind, LifetimeManagementKind og Saa LifetimeOperationKind.
// Som er taet forbundet. Og som jeg helst ser blev kogt ned til nogle faerre enums


public enum LifetimeOperationKind {
    INITIALIZE, // Unmanaged

    INITIALIZE_AND_START, //Managed_async

    STOP,  // Managed_async

    EXECUTE; // Managed_sync
}

// initialize = always singlethreaded, Bare en liste af operationer der bliver kaldt efter hinanden
// De andre 3 kan vaere multi-threaded og der kan indgaa flere state skifte. Og evt afventning af
// fx container shutdown.