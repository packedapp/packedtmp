/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.concurrent.TimeUnit;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.sandbox.StopOption;

/**
 * Every bean is automatically in ApplicationContext.
 */
public interface ApplicationContext extends Context<BaseExtension> {

    /** {@return the name of the application} */
    String name();

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
    // Er det ikke de eneste relevante states: Running, Stopping???
    // Hvad er usecase for Running???

    // Terminated <--- er ikke lovligt at vente paa internt
    // Den her er ikke saerlig brugbart
    void sleep(RunState state) throws InterruptedException;

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
    boolean sleep(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    /** {@return the current state of the application} */
    RunState currentState();

    /** {@return the desired state of the application.} */
    RunState desiredState();

    /** {@return whether or not the application is managed} */
    boolean isManaged();

    /**
     * Attempts to stop the application.
     *
     * @param options
     *            stop options
     * @return false if already shutdown otherwise true
     * @throws IllegalStateException
     *             if attempting to shutdown the container while initializing
     * @throws UnsupportedOperationException
     *             if the application is not {@link #isManaged() managed}
     */
    boolean stopAsync(StopOption... options);
}