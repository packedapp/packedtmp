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
package app.packed.lifecycle;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * Information about why an component was stopped.
 * <p>
 * This interface provides information about the outcome of stopping an component. It follows
 * similar semantics to {@link java.util.concurrent.CompletableFuture}:
 * <ul>
 *   <li>{@link #isFailed()} returns {@code true} if the component completed exceptionally (including cancellation)</li>
 *   <li>{@link #isCancelled()} returns {@code true} only if the component was cancelled</li>
 *   <li>If {@code isCancelled()} is {@code true}, then {@code isFailed()} is also {@code true}</li>
 * </ul>
 * <p>
 * Once an component has been stopped, this information will not change.
 *
 * @see java.util.concurrent.CompletableFuture
 */
public interface StopInfo {

    /** {@return the exception if failed, otherwise empty} */
    Optional<Throwable> failure();

    /** {@return true if the component was cancelled} */
    boolean isCancelled();

    /**
     * {@return true if the component failed}
     * <p>
     * This includes cancellation. If this method returns {@code true}, {@link #failure()} will return
     * the causing exception (a {@link java.util.concurrent.CancellationException} if cancelled).
     *
     * @see java.util.concurrent.CompletableFuture#isCompletedExceptionally()
     */
    boolean isFailed();

    /** {@return the run state from which the component was stopped} */
    RunState stoppedFromState();

    /** {@return the trigger that initiated the stop} */
    Trigger triggeredBy();

    /**
     * A trigger indicating why an component was stopped.
     * <p>
     * The framework provides a number of pre-defined triggers, but users and extensions are free to define their own.
     */
    public final class Trigger {

        final String trigger;

        private Trigger(String trigger) {
            this.trigger = requireNonNull(trigger);
        }

        /**
         * Indicates that the JVM is shutting down and the component is stopping as a result of a shutdown hook.
         *
         * @see app.packed.application.ApplicationWirelets#shutdownHook(StopOption...)
         * @see app.packed.application.ApplicationWirelets#shutdownHook(java.util.function.Function, StopOption...)
         * @see Runtime#addShutdownHook(Thread)
         */
        public static final Trigger SHUTDOWN_HOOK = new Trigger("ShutdownHook");

        /** Indicates that the component has timed out. */
        public static final Trigger TIMEOUT = new Trigger("Timeout");

        /** Indicates normal completion. */
        public static final Trigger NORMAL = new Trigger("Normal");

        /** Indicates that the component failed internally. */
        public static final Trigger FAILED_INTERNALLY = new Trigger("Failed");

        /** Indicates an unknown trigger. */
        public static final Trigger UNKNOWN = new Trigger("Unknown");

        @Override
        public String toString() {
            return trigger;
        }
    }
}

// TODO Trigger cleanup:
// - RESTARTING trigger?
// - CANCELLED trigger?
// - UPGRADING/Redeploy trigger?
// - SESSION_TIMEOUT vs TIMEOUT distinction?
// - TimeToStart timeout vs entrypoint timeout?

// Notes:
// - Triggers are shared between beans and containers
// - StopInfo is created exactly once and does not change
// - Cannot have information about relationship between root and children (isDependant)
// - If child closes normally and parent closes exceptionally - child keeps its original StopInfo
