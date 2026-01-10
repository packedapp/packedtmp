/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package sandbox.lifetime.external;

/**
 *
 */
// Tror faktisk OnInitialize er et godt bud paa steder hvor man kan spoerge om disse ting

// Stop reason
//// Normal
//// Abnormal (with failure)

// What happens next
////

public enum LifetimeModel {
    // managed vs unmanaged

    // result-based yes/no

    // Optional<NextAction>
    // restartable yes/no (Same Process, New Process)

    // isSuspendable/resumable (locally, remote, cloneable-image)

    // Redeploy (Same Process, New Process)
}

// WorkSet? <-- muligheden for at gemme noget
// Taenker det ogsaa kan bruges distribueret



// Replicable // Ikke en del af lifetime'