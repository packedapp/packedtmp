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
package app.packed.lifetime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionMetaHook.AnnotatedBeanMethodHook;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link RunState#STARTING} state.
 *
 * <p>
 * A simple usage example:
 *
 * <pre>
 * &#064;OnStart()
 * public void executeOnComponentStart() {
 *     System.out.println(&quot;Component is starting&quot;);
 * }
 * </pre>
 *
 * Methods annotated with OnStart can have any service that is also available from the component. For example, the
 * following method will print out the name and the state of the container when it starts.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello(Container container) {
 *     System.out.println(&quot;The current state of container &quot; + container.getName() + &quot; is &quot; + container.getState());
 * }
 * </pre>
 * <p>
 * A common usage examples is initializing services with data:
 *
 * For example, fetching it from disk.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello() throws IOException {
 *     // load data from disk
 *     // prepare service for use
 * }
 * </pre>
 *
 * Notice that this method throws a checked exception. Any method that throws an Exception will result in the
 * application failing to start. After which the application will automatically move to the shutdown phase.
 * <p>
 *
 * Normally services are not available from xx until all services have been successfully started. However, by using this
 * annotation. Services that not yet completed startup can be injected. It is up to the user to make sure that invoking
 * method on instances that injected this does not cause any problems. For example, calling a method on another service
 * that only works when the container is in the running phase.
 *
 * @see OnInitialize
 * @see OnStop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedBeanMethodHook(allowInvoke = true, extension = BaseExtension.class)
public @interface OnStart {

    String JOIN_ON_AFTER_DEPENDENCIES = "AFTER_DEPENDENCIES";

    String JOIN_ON_LIFETIME_COMPLETE = "LIFETIME_COMPLETE";

    /** Can be used, for example, */
    String JOIN_ON_KEEP_RUNNING = "LIFETIME_KEEP_RUNNING";


    // Fork with default settings, otherwise use Fork
    boolean fork() default false;

    LifecycleOrder order() default LifecycleOrder.BEFORE_DEPENDENCIES;
}
//
///**
// * Starts a new thread to run the given task.
// *
// * @return {@code true} if
// */
//// Synchronous before dependencies
//// Synchronous after dependencies
//// Asynchronous before dependencies, stop after dependencies
//// Asynchronous before dependencies, stop lifetime.complete
//// Asynchronous before dependencies, keep running
//// Asynchronous after dependencies, stop lifetime.complete
//// Asynchronous After dependencies, keep running
////// Before some string based "Event"
//
//// async =
//boolean fork() default false;
//
//// MethodReferences would just be fucking awesome
//// joinPolicy default JoinPolicy::immediately();
//
///**
// * Whether or not any thread will be interrupted if shutdown while starting
// *
// * @return
// */
//// Only if Async??? I think this only works on forked...
//// Because if on main thread this annotation is completely ignored
//// Unless we interrupt the main thread from another thread...
//// Interrupt on stop is part of fork
//boolean interruptOnStop() default false; // Maybe have an InterruptionPolicy {NEVER, DEFAULT, ALWAYS}

// order = "SomE:1"; (I forhold til andre der er bruger SomE
// eller maaske = "->Foo"; (Jeg released Foo)
// String[] after() default {};

// Problemet med at have baade after og before er at det er forskellige modeller.
// Hvis tilfoejer en before <- Saa virker den paa den maade at skal med en beforeX skal vaere faerdige inde vi kan
// begyndede...
// Maaske kun have starting points til at starte med???

// Maaske gaa over til XXX og *XXX
// XXX venter man paa en, *XXX venter paa alle er faerdige, flere end en kan complete den...

// OnStart()
// @StartPoint();<- fields and methods
// public CompletableFuture<>