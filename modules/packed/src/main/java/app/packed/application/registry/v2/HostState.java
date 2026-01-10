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
package app.packed.application.registry.v2;

/**
 *
 */
// Ideen er egentlig lidt at have en generisk state.
// For hosts with guest. Typically they are no immediately ready
// And they will typically also spend some time on cleaning up the guests
public enum HostState {
 // Installing AKA building... But not observable I think.

    /** Application instances can be created. */
    READY,

    /** Applications instances can no longer be created. */
    DISABLED,

    /**
     * All managed application instances of the application are being stopped, but some have not yet fully terminated.
     * <p>
     * When they have all successfully been stopped. The application will be uninstalled.
     */
    STOPPING_INSTANCES,
}
