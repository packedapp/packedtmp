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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.component.Component;
import app.packed.container.App;

/**
 * A lifecycle expansion interface. The main purpose of this interface is to avoid cluttering the interface of entities
 * such as {@link App} and {@link Component} with a large number of rarely used lifecycle methods. Instead of placing
 * the methods directly on the Container or Component interface. A method can be used to get a hold of an instance of
 * this interface.
 *
 * Instead an instance of this inte
 *
 */
// start/stop/stop(Throwable)
// uninstall()/uninstall(Throwable) / or stop(Throwable).uninstall(); (Stopping return a StopFuture with uninstall)
// TODO LifecycleExpansion
// Startup points, shutdown points. startup().now(); shutdown().in(10, TimeUnit.SECONDS)
// Nu har vi syncpoints paa en container, men ikke paa en component...
// Maaske er det bare for containeren???? Eller maaske smider de unsupported operation exception.
// Virker lidt maerkeligt at "inititalized" bleviver haandteret af component, men "dddd" bliver haandteret af
// containeren.

// Vi har ogsaa start/stop/stop(throwable).
// uninstall (attachable)

// LifecyclePoint extends SyncPoint() {
// LfecycleState() getEnum())
// lockAndRun....
// }
// component.pointOf("fooo").await();
// component.pointOf("fooo").thenRun();

// component.state().of(LifecycleState.Initializated).await();
// component.on(LifecycleState.Initializated).await();

// then run();

// General states vs point specific split det op i 2... 100% sikkert hvis vi skal
// have syncpoints... Fordi ellers giver det ikke mening
// F.eks. er der jo ikke nogen der string baseret syncpoints paa components.
// Saa vi kan ikke smide den paa lifecycle operations....

// Persistance.install(ComponentServiceInstaller installer();
// or Persistance.install(AnyBundle bundle);<- and then use with(), so we can install Annotations....
interface LifecycleSpecificOperations<T> {

    /**
     * Blocks until the object has reached the requested state, or the current thread is interrupted, whichever happens
     * first.
     * <p>
     * If the object has already reached or passed the specified state this method returns immediately. For example, if
     * attempting to wait on the {@link LifecycleState#RUNNING} state and the object has already been stopped. This method
     * will return immediately with true.
     *
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await( long, TimeUnit)
     * @see #whenAt(LifecycleState)
     */
    // on(LifecycleState.INITIALIZED).await();
    void await() throws InterruptedException;

    /**
     * Blocks until the object has reached the requested state, or the timeout occurs, or the current thread is interrupted,
     * whichever happens first.
     * <p>
     * If the object has already reached or passed the specified state this method returns immediately. For example, if
     * attempting to wait on the {@link LifecycleState#RUNNING} state and the object has already been stopped. This method
     * will return immediately with true.
     *
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return true if this object is in (or has already passed) the specified state and false if the timeout elapsed before
     *         reaching the state
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #whenAt(LifecycleState)
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    // if and only if
    // While the runnable
    // The component might be shutdown, but its just the shutdown procedures that will not be run
    // Initialize/Initialized makes no sense, because it is serial
    // Although, container

    // Starting Dont think it makes sense
    // Running /Yes
    // Shutdown <- Dont think it makes sense
    // Def not, final state so will never change
    // Could be read-write lock

    // Might make sense, to schedule some shutdown jobs?? But I think this should might be seperated into a ShutdownToken
    //
    // public static void main(Component c) {
    // c.lifecycle().runOn(LifecycleState.STOPPING, () -> System.out.print("Stopping"));
    // }

    default CompletableFuture<?> runOn(Runnable action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the specified action iff the current state of the owning object is identical to the specified state. The
     * state of the object is guaranteed to not change while the runnable is being executed.
     * <p>
     * Invocations should be infrequent
     * <p>
     * This method can be invoked concurrently by multiple threads.
     *
     * @param action
     *            the action to execute
     * @return true if the action was executed, otherwise false
     */
    boolean runIfStateIs(Runnable action);

    // runUnderLockIfStateIs() //check state, lock, check state igen
    // runUnderLockIf(Predicate) <--- bliver noedt til at eksekvere
    // runUnderLock()

    // Maaaske byt them, ud saa kan vi have vargs state
    default <V> V supplyIfStateAt(Supplier<V> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a completion stage that will be completed whenever the component reaches the specified state.
     * <p>
     * For example, the following example will print a simple string if the component starts successfully:
     *
     * <pre>
     *  {@code
     * Component component = ...;
     * component.whenAtState(State.RUNNING).thenRunAsync(() -> System.out.println("Component started successfully"));}
     * </pre>
     * <p>
     * If the component has already reached or passed the specified state the completion stage is already completed when
     * this method returns.
     *
     * @param state
     *            the state for which to return a completion stage
     * @return a completion stage for the specified state
     */
    // Do we want to add this to configuration? ContainerConfiguration.whenAtState(Running.class, print "Yeah");
    // We want a CompletionFuture instead. We want something like join() to be available.
    // whenAt(LifeState.INITIALIZING).then(c->c.install(ccc));
    CompletionStage<T> whenAt(LifecycleState state);

    // boolean isRestartable()
    // boolean isRestarting();

    // /**
    // * @param name
    // * @return
    // * @throws UnsupportedOperationException
    // * if the a synchronization point with the specified name does not exist
    // */
    // default SyncPoint withPoint(String name) {
    // throw new UnsupportedOperationException();
    // }
    //
    // default SyncPoint atPoint(String name) {
    // throw new UnsupportedOperationException();
    // }
    //
    // default CompletionStage<T> atState(String name) {
    // throw new UnsupportedOperationException();
    // }
}
