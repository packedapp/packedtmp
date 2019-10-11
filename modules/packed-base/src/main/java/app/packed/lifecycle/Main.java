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
package app.packed.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.extension.UseExtension;

/**
 * A application can have a single main entry point which is the first instructions in a program that is executed, Must
 * be placed on a method on a bundle. Why not individual components??
 * <p>
 * The annotated method will be executed once the artifact in which it is registered has transtioned to the
 * {@link RunState#RUNNING} state. When the method has finished executing (either succesfully or because of a failure)
 * the artifact will be shutdown.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// @InterruptOnStop
// https://en.wikipedia.org/wiki/Entry_point
// Main kan vel ogsaa bruges paa en injector???? Nahhh, hvordan styre vi det???
// Maaske bare at alle annoteringer + extensions, udover Provide+Inject fejler???

// Move main to app.packed.lifecycle ??? I think it has a lot to do with lifecycle....
// Because it is actually important that people understand the model....
// It is really heavily related to App actually because, you cannot have a Main for a Container
// Only a main for an App.
// Furthermore we also want to put cli here...

// Main skal have en lifecycle. Fordi der er forskel paa
// inde main() bliver koert -> Initialized/Starting
// naar main bliver koert -> Running
// og efter main er koert.... -> Stoppping/Stopped
// Ogsaa selvom vi ikke har shutdown metoder...

// Det samme kan man sige om CLI
@UseExtension(LifecycleExtension.class)
public @interface Main {

    /**
     * Whether or not the application should be shutdown when this method completes. The default value is true.
     * 
     * @return or not the application should be shutdown when this method completes. The default value is true
     */
    // Syntes ikke den er god, hvad hvis man kalder run()....
    // Shutdown on Success instead? Always shutdown on Failure I guess
    boolean shutdownOnCompletion() default true; // Taenker hellere det maa vaere noget @OnRunning()...

    boolean overridable() default true;

    //// Nice performance measurement. Keep installing noop
    // ContainerImages, with undeploy
    // Maaske hellere en option i EntryPoint??? Eller begge dele...
    /// Problemet er at vi gerne ville knytte det til Main
    // Deployment option????
    boolean undeployOnCompletion() default true;
}