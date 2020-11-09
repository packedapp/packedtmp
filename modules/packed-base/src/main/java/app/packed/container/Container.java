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
package app.packed.container;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.component.Assembly;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import packed.internal.component.PackedBuildContext;
import packed.internal.component.PackedContainer.ExecutingImage;

/**
 * A container instance.
 */
// Restarting a guest actually means terminated an existing guest. And starting a new one.
// Component, Service = Describes something, Guest also controls something...
// I would imagine we want something to iterate over all state machines...
public interface Container {

    /**
     * Blocks until the container has reached the specified state, or the current thread is interrupted, whichever happens
     * first.
     * <p>
     * If the container has already reached or passed the specified state this method returns immediately. For example, if
     * attempting to wait on the {@link ContainerState#RUNNING} state and the container has already been successfully
     * terminated. This method will return immediately.
     *
     * @param state
     *            the state to wait on
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(ContainerState, long, TimeUnit)
     * @see #state()
     */
    void await(ContainerState state) throws InterruptedException;

    /**
     * Blocks until the container has reached the requested state, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     * <p>
     * If the container has already reached or passed the specified state this method returns immediately with. For example,
     * if attempting to wait on the {@link ContainerState#RUNNING} state and the object has already been stopped. This
     * method will return immediately with true.
     *
     * @param state
     *            the state to wait on
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if this container is in (or has already passed) the specified state and {@code false} if the
     *         timeout elapsed before reaching the state
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(ContainerState)
     * @see #state()
     */
    boolean await(ContainerState state, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Returns an immutable snapshot of the container's current status.
     * 
     * @return an immutable snapshot of the container's current status
     */
    ContainerInfo info();

    /**
     * Starts and awaits the container if it has not already been started.
     * <p>
     * Normally, there is no need to call this methods since most methods on the container will lazily start the container
     * whenever it is needed. For example, invoking use will automatically start the container if it has not already been
     * started by another action.
     * 
     * @see #startAsync(Object)
     */
    void start();

    default CompletableFuture<Void> startAsync() {
        return startAsync(null);
    }

    <@Nullable T> CompletableFuture<T> startAsync(T result);

    /**
     * Returns the current state of the container.
     * <p>
     * Calling this method will never block the current thread.
     * 
     * @return the current state of the container
     */
    ContainerState state();

    /**
     * Stops the container.
     * 
     * @param options
     *            optional stop options
     * @see #stopAsync(Object, StopOption...)
     */
    void stop(StopOption... options);

    default CompletableFuture<Void> stopAsync(StopOption... options) {
        return stopAsync(null, new StopOption[] {});
    }

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     * 
     * @param <T>
     *            the type of result in case of success
     * @param result
     *            the result the completable future
     * @param options
     *            optional guest stop options
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     * @see #stop(StopOption...)
     */
    <T> CompletableFuture<T> stopAsync(@Nullable T result, StopOption... options);

    public static void execute(Assembly<?> assembly, Wirelet... wirelets) {
        PackedBuildContext build = PackedBuildContext.build(assembly, false, false, null, wirelets);
        build.process(null);
    }

    public static <T> T execute(Assembly<?> assembly, Class<T> resultType, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static <T> T execute(Assembly<?> assembly, TypeToken<T> resultType, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static Image<Void> imageOf(Assembly<?> assembly, Wirelet... wirelets) {
        PackedBuildContext build = PackedBuildContext.build(assembly, false, true, null, wirelets);
        return new ExecutingImage(build);
    }

    public static <T> Image<T> imageOf(Assembly<?> assembly, Class<T> resultType, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Image<T> imageOf(Assembly<?> assembly, TypeToken<T> resultType, Wirelet... wirelets) {
        return imageOf(assembly, (Class) resultType.rawType(), wirelets);
    }

    // ContainerStopOption????
    // Eller er det generisk..? Kan den bruges paa en actor??? et Actor Trae...
    // Hehe, hvis actor ogsaa er en artifact... Saa

    // Men taenker det her lifecycle er rimligt generisk...

    // StopOptions is Wirelets at stop time...
    // GuestStopOption????

    // Flags.. Minder lidt om wirelets...
    // Og list stop options
    // https://twitter.github.io/util/docs/com/twitter/app/App.html

    // ER HELT SIKKER IKKE EN DEL AF LIFECYCLE VIL JEG MENE
    // Som udgangspunkt er det noget med Guest og goere..
    // Med mindre instanser lige pludselig kan bruge det.

    /** Various options that can be used when stopping a container. */
    // Panic vs non-panic, a panic signals a non-normal shutdown

    // interrupt vs non-interrupt
    // forced - non-forced
    //// Er kun aktuelt naar shutdown ikke foreloeber korrect...
    // Den burde ikke have semantics indvirkning paa hvad der sker efter stop

    public interface StopOption {

        // Forced = try and interrupt
        static StopOption forced() {
            throw new UnsupportedOperationException();
        }

        // Can be used as wirelet as well...
        static StopOption forcedGraceTime(long timeout, TimeUnit unit) {
            // before forced???
            throw new UnsupportedOperationException();
        }

        // Er vel forced???
        static StopOption now() {
            // Now == shutdownNow();
            throw new UnsupportedOperationException();
        }

        static StopOption now(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        // panic()?
        static StopOption panic(Supplier<Throwable> cause) {
            throw new UnsupportedOperationException();
        }

        static StopOption panic(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        static StopOption restart(Wirelet... wirelets) {
            // restart(Wirelet.rename("Restart at ....");
            //// Men okay hvad hvis det ikke kan lade sige goere at omnavngive den...
            throw new UnsupportedOperationException();
        }

        // Will override default settings.
        // linger would be nice
        // Or maybe somewhere to replace the guest with a tombstone of some kind.
        // Summarizing everything in the guest...
        static StopOption undeploy() {
            throw new UnsupportedOperationException();
        }
        // restart.. (Artifact must have been started with RestartWirelets.restartable();
    }
    // normal
    // normal + restart(manual)
    // erroneous[cause]
    // erroneous[cause] + restart(manual)
    // forced() (either directly, or after
    // forced(cause?)
    // delayForce(10 min) <- try shutdown normally and then forced after X mins...

    // Scheduled (Altsaa er det ikke folks eget ansvar???)
    // Kun fordi vi supporter noget af det med wirelets
    // shutdown in 10 minutes and then restart... (altsaa kan man ikke s)
}

//10 seconds is from start.. Otherwise people must use an exact deadline
//start(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS));
//sart(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS), ()-> New CancelledException()); (failure)

//Rename fordi vi gerne vil have at ArtifactDriver hedder det samme og
//AppHost.xxx() .. Dumt det hedder App.of og AppHost.instantiate
///**
//* Initiates an asynchronously startup of the application. Normally, there is no need to call this methods since most
//* methods on the container will lazily start the container whenever it is needed. For example, invoking
//* {@link #use(Class)} will automatically start the container if it has not already been started by another action.
//* <p>
//* If the container is in the process of being initialized when invoking this method, for example, from a method
//* annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
//* constructed.
//* <p>
//* Invocation has no additional effect if the container has already been started or shut down.
//*
//* @return a future that can be used to query whether the application has completed startup or is still in the process
//* of starting up. Can also be used to retrieve any exception that might have prevented the container in
//* starting properly
//*/
