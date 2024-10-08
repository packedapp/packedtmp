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
package app.packed.build;

/**
 * The goal of a build process. Or task??? Can we say just build an image from this part of the application
 */
public enum BuildGoal {

    /**
     * The goal is to build an application image that can be launched one or mores times at a later point.
     *
     * @see App#imageOf(Assembly, Wirelet...)
     */
    IMAGE,

    /**
     * The goal is to build an application and then immediately launch it.
     *
     * @see App#run(Assembly, Wirelet...)
     */
    LAUNCH,

    /**
     * The goal is to build an application and expose one or more mirrors, for example, {@link ApplicationMirror}.
     *
     * @see App#mirrorOf(Assembly, Wirelet...)
     */
    MIRROR,

    /**
     * The goal is to verify that the application is structural correct.
     *
     * @see App#verify(Assembly, Wirelet...)
     */
    VERIFY;

    /** {@return whether or not code will be generated doing the build task.} */
    public boolean isCodeGenerating() {
        return this == LAUNCH || this == IMAGE;
    }
}



///**
//* The goal is to build an application image that can be launched multiple times.
//*
//* @see App#newImage(Assembly, Wirelet...)
//*/
//LAUNCH_REPEATABLE,
