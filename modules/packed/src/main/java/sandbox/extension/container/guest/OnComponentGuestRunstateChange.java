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
package sandbox.extension.container.guest;

import app.packed.runtime.RunState;

/**
 *
 */
// On a host manager bean
// Can have The guest injected, And the transition (Maybe a StopInfo if toStates is only toStop)
// Maybe we have a generic RunStateChange that also has an exception (would be nice for initalization errors)
public @interface OnComponentGuestRunstateChange {
    RunState[] fromStates() default {};

    RunState[] toStates() default {};

    // Will only be executed if the state change is do to a failure
    boolean onlyOnFailure() default false;
}
