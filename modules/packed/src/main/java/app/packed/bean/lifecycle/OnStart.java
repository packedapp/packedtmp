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
package app.packed.bean.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.scanning.BeanTrigger;
import internal.app.packed.lifecycle.LifecycleAnnotationBeanIntrospector;

/**
 * An annotation used to indicate that a method should be invoked whenever the bean reaches the
 * {@link RunState#STARTING} state.
 *
 * <p>
 * A simple usage example:
 *
 * <pre>
 * &#064;OnStart()
 * public void executeOnBeanStart() {
 *     System.out.println(&quot;Bean is starting&quot;);
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
 * Normally services are not available from xx until all services have been successfully started. However, by using this
 * annotation. Services that not yet completed startup can be injected. It is up to the user to make sure that invoking
 * method on instances that injected this does not cause any problems. For example, calling a method on another service
 * that only works when the container is in the running phase.
 *
 * @see OnInitialize
 * @see OnStop
 */

// Lifetime = Whole Application, Non-Application Container Lifetime (ala session), Bean Lifetime

// onBean, onLifetime, onApplication...

// A bean is started when all OnStart methods on the bean has completed successfully

// When used on a field the target type must be Lazy
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@BeanTrigger.OnAnnotatedMethod(introspector = LifecycleAnnotationBeanIntrospector.class, requiresContext = OnStartContext.class, allowInvoke = true)

// Hvordan sikre vi os at DB kører førend WEB
// Hvis de ikke depender paa hinanden.
//// Samtidig vil vi gerne maximere async tid, saa joine senest muligt

// Tror phases er en daarlig ide. Fordi vi godt vil encapsulated foer og efter. Det fungere daar


public @interface OnStart {

    /**
     * If there are multiple methods with {@link OnStart} and the same {@link #order()} on a single bean. This attribute can
     * be used to control the order in which they are invoked. With a higher bean order be invoked first for
     * {@link OperationDependencyOrder#BEFORE_DEPENDENCIES}. And lower bean order be invoked first for
     * {@link OperationDependencyOrder#AFTER_DEPENDENCIES}. If the bean order are identical the framework may invoke them in
     * any order.
     * <p>
     * NOTE: Specifically this attribute can not be used to control any ordering with regards to operations on other beans.
     *
     * @return the bean order
     * @implNote current the framework will invoked them in the order returned by {@link Class#getMethods()}
     */
    byte beanOperationOrder() default 0; // use -1 and, alternative before, after (String operation name)

    // Fork with default settings, otherwise use Fork
    /**
     * {@return whether or not a new thread should be forked when running the annotated method.}
     */
    boolean fork() default false;

    /**
     * If the annotated method is still running when the lifetime is stopped (for example, if another bean failed to start
     * properly). This attribute can be used indicate to the framework that the thread executing the operation should be
     * interrupted by the framework.
     *
     * @return
     */
    boolean interruptOnStopping() default true;

    String lifetime() default "bean"; // The lifetime the bean is in

    // forkMode
    // forkAsDaemon (keepRunning) = Mark the bean as keep running. Don't await

    /**
     * Returns how this operation is ordered compared to other start operations.
     *
     * @return
     */
    // Jeg tror after_dependencies kraever vi monitorer bean state...
    // Fordi vi siger koer denne metode efter x-bean er started
    LifecycleDependantOrder order() default LifecycleDependantOrder.BEFORE_DEPENDANTS;

    /**
     * Whether or not the bean should be marked as failed to start if the method throws
     *
     * @return
     */
    boolean stopOnFailure() default true; // Class<? extends Throwable> stopOnFailure() default Throwable.class

    // String phase.

    // BeforeDependants, Before_Dependants_FORKED, AFTER_DEPENDANTS // Late must be

    // Mode, EAGER, EAGER_FORK, LATE
    public enum ForkMode {
        BEFORE_DEPENDANTS,

        FORK_BEFORE_DEPENDANTS_JOIN_AFTER_DEPENDANTS,

        FORK_BEFORE_DEPENDANTS_JOIN_BEFORE_START,

        AFTER_DEPENDANTS,

        FORK_AFTER_DEPENDANTS_JOIN_BEFORE_START,

        DISABLED,

        // On the way back. Ved ikke om den giver mening
        // Tror simpelthen ikke den giver mening
        // Basalt set fungere den som @OnStop(reverse order) -> task.join
        JOIN_AFTER_DEPENDANTS, // Await on all dependants having started

        // Tror vi har en java api her???
        // Ignore dependencies, but before Application is marked as running
        JOIN_BEFORE_START,
    }


    // Alternativ har vi phases
}

// Koer foer dependants
// Koer efter dependants
// Koer lige inde vi starter


///// !
//// Never await, keep running
//// Until Finished
//@Deprecated(since = "OnStart is always joining")
//JOIN_NEVER

//String JOIN_ON_AFTER_DEPENDENCIES = "AFTER_DEPENDENCIES";
//
///** Can be used, for example, */
//String JOIN_ON_KEEP_RUNNING = "LIFETIME_KEEP_RUNNING";
//
//String JOIN_ON_LIFETIME_COMPLETE = "LIFETIME_COMPLETE";

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