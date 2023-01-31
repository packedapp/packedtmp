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
package app.packed.lifetime.sandbox;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import app.packed.framework.Nullable;
import app.packed.lifetime.RunState;

// This is basically something thats wraps a state that is 100 Linear
// It is not 100 % clean because of restarting... IDK about that

/**
 * An application runtime is available for all runnable applications.
 *
 * A component instance.
 */
// Restarting a guest actually means terminated an existing guest instance. And starting a new one.
// Component, Service = Describes something, Guest also controls something...
// I would imagine we want something to iterate over all state machines...
// Was Runner
// ExecutionEnvironment... IDK
// StateHost
// Kan jo faktisk godt sige det er en Host...
// Host... everything with state are called Guests...
// AutoClosable???
// Maybe just Runtime? Saa kan vi bruge den andre steder end fra application...

//RuntimeEnvironment
// Vil vaere fint at have et 1-ords navn.. istedet for ApplicationRuntime
// Naa vi naar hen til ApplicationRuntimeExtensionMirror begynder det at blive grimt...
// Maaske fjerner vi Extension fra mirror...
// Environtment..

// ExecutionHost
// ExecutionManager
// Application Container
public interface ManagedLifetimeController {

    // Optional<Throwable> getFailure();

    /**
     * Blocks until the lifetime reaches the specified run state, or the current thread is interrupted, whichever happens
     * first.
     * <p>
     * If the lifetime has already reached (or passed) the specified state this method returns immediately. For example, if
     * attempting to wait on the {@link RunState#RUNNING} state and the lifetime has already terminated. This method will
     * return immediately.
     *
     * @param state
     *            the state to wait on
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(RunState, long, TimeUnit)
     * @see #state()
     */
    void await(RunState state) throws InterruptedException;

    /**
     * Blocks until the component has reached the requested state, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     * <p>
     * If the component has already reached or passed the specified state this method returns immediately with. For example,
     * if attempting to wait on the {@link RunState#RUNNING} state and the object has already been stopped. This method will
     * return immediately with true.
     *
     * @param state
     *            the state to wait on
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if this component is in (or has already passed) the specified state and {@code false} if the
     *         timeout elapsed before reaching the state
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(RunState)
     * @see #state()
     */
    boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Starts and awaits the component if it has not already been started.
     * <p>
     * Normally, there is no need to call this methods since most methods on the component will lazily start the component
     * whenever it is needed. For example, invoking use will automatically start the component if it has not already been
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
     * Returns the current state of the component.
     * <p>
     * Calling this method will never block the current thread.
     *
     * @return the current state of the component
     */
    RunState state();

    /**
     * Stops the component.
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
    // Does not take null. use StopAsync
    <T> CompletableFuture<T> stopAsync(T result, StopOption... options);

    // Vs main?????
    // Tror main er bl.a. propper det ind som et system image...

    // Den er cool men sgu ikke super smart for forstaelsen
//    static void run(Assembly  assembly, Wirelet... wirelets) {
//        ApplicationRuntimeImplementation.DRIVER.launch(assembly, wirelets);
//    }

    // altsaa problemet her jo i virkeligheden image...
    // Vi gider ikke have flere maader at launche et image paa...
//    static <A> A launch(ApplicationDriver<A> driver, Assembly  assembly, Wirelet... wirelets) {
//        return driver.launch(assembly, wirelets);
//    }

//    // TODO return Image<Host>?
//    static ApplicationImage<?> newImage(Assembly  assembly, Wirelet... wirelets) {
//        return ApplicationRuntimeImplementation.DRIVER.imageOf(assembly, wirelets);
////
////        PackedBuildInfo build = PackedBuildInfo.build(assembly, false, true, null, wirelets);
////        return new ExecutingImage(build);
//    }

//    static Image<Void> imageOfMain(Assembly<?> assembly, Wirelet... wirelets) {
//        PackedBuildInfo build = PackedBuildInfo.build(assembly, false, true, null, wirelets);
//        return new ExecutingImage(build);
//    }

//
//    static <T> T execute(Assembly<?> assembly, Class<T> resultType, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }
//
//    static <T> T execute(Assembly<?> assembly, TypeToken<T> resultType, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }
//
//    static <T> Image<T> imageOf(Assembly<?> assembly, Class<T> resultType, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }
//
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    static <T> Image<T> imageOf(Assembly<?> assembly, TypeToken<T> resultType, Wirelet... wirelets) {
//        return imageOf(assembly, (Class) resultType.rawType(), wirelets);
//    }

    // componentStopOption????
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

    // Scheduled (Altsaa er det ikke folks eget ansvar???)
    // Kun fordi vi supporter noget af det med wirelets
    // shutdown in 10 minutes and then restart... (altsaa kan man ikke s)
}

///**
//*
//*/
//final class ApplicationRuntimeImplementation {
//    static final ApplicationDriver<Void> DRIVER = ApplicationDriver.builder().executable(ApplicationLaunchMode.EXECUTE_UNTIL_TERMINATED).build(MethodHandles.lookup(), Void.class);
//}

//10 seconds is from start.. Otherwise people must use an exact deadline
//start(new SomeAssembly(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS));
//sart(new SomeAssembly(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS), ()-> New CancelledException()); (failure)

//Rename fordi vi gerne vil have at ArtifactDriver hedder det samme og
//AppHost.xxx() .. Dumt det hedder App.of og AppHost.instantiate
///**
//* Initiates an asynchronously startup of the application. Normally, there is no need to call this methods since most
//* methods on the component will lazily start the component whenever it is needed. For example, invoking
//* {@link #use(Class)} will automatically start the component if it has not already been started by another action.
//* <p>
//* If the component is in the process of being initialized when invoking this method, for example, from a method
//* annotated with {@link OnInitialize}. The component will automatically be started immediately after it have been
//* constructed.
//* <p>
//* Invocation has no additional effect if the component has already been started or shut down.
//*
//* @return a future that can be used to query whether the application has completed startup or is still in the process
//* of starting up. Can also be used to retrieve any exception that might have prevented the component in
//* starting properly
//*/
