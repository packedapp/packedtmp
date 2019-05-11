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
package app.packed.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A application can have a single main entry point which is the first instructions in a program that is executed, Must
 * be placed on a method on a bundle. Why not individual components??
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// @InterruptOnStop
public @interface Main {

    /**
     * Whether or not the application should be shutdown when this method completes. The default value is true.
     * 
     * @return or not the application should be shutdown when this method completes. The default value is true
     */
    // Syntes ikke den er god, hvad hvis man kalder run()....
    boolean shutdownOnCompletion() default true; // Taenker hellere det maa vaere noget @OnRunning()...

    boolean overridable() default true;
}
// https://en.wikipedia.org/wiki/Entry_point

// Cli kan vel ogsaa bruges paa en injector????
// Nahhh