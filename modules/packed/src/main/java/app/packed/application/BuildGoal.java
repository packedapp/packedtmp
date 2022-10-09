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
package app.packed.application;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 * The goal of a build task.
 */
public enum BuildGoal {

    /**
     * The goal is to build an {@link ApplicationImage} that can be launched a single time.
     * 
     * @see ApplicationDriver#reusableImageOf(Assembly, Wirelet...)
     */
    NEW_LAUNCHER,

    /**
     * The goal is to build an {@link ApplicationImage} that can be launched multiple times.
     * 
     * @see ApplicationDriver#imageOf(Assembly, Wirelet...)
     */
    NEW_IMAGE,

    /**
     * The goal is to build an application and then immediately launch it.
     * 
     * @see ApplicationDriver#launch(Assembly, Wirelet...)
     */
    LAUNCH,

    /**
     * The goal is to build an {@link ApplicationMirror}.
     *
     * @see ApplicationDriver#mirrorOf(Assembly, Wirelet...)
     */
    NEW_MIRROR,

    /**
     * The goal is to verify that the application is structural correct.
     * 
     * @see ApplicationDriver#verify(Assembly, Wirelet...)
     */
    VERIFY;

    public boolean isLaunchable() {
        return this == LAUNCH || this == NEW_LAUNCHER || this == NEW_IMAGE;
    }

}