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
package app.packed.util;

import java.util.concurrent.Future;

/**
 *
 * <p>
 * This class is modelled on the basis of {@link Future}.
 *
 */
public final /* primitive */ class Result<T> {

    /** The result, or an exception if completed exceptional. */
    private final Object result;

    /** The state of the result. */
    private final State state;

    private Result(Object result, State state) {
        this.result = result;
        this.state = state;
    }

    public Throwable exception() {
        if (state != State.SUCCESS) {
            return (Throwable) result;
        }
        throw new IllegalStateException("Result was success");
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (state == State.SUCCESS) {
            return (T) result;
        }
        throw new IllegalStateException("Result was a failure");

    }

    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

    public boolean isCompletedExceptional() {
        return state != State.SUCCESS;
    }

    public boolean isFailed() {
        return state == State.FAILED;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public static <T> Result<T> ofCancelled(Throwable t) {
        return new Result<>(t, State.CANCELLED);
    }

    public static <T> Result<T> ofFailed(Throwable t) {
        return new Result<>(t, State.FAILED);
    }

    public static <T> Result<T> ofFuture(Future<T> f) {
        return switch (f.state()) {
        case RUNNING -> throw new IllegalStateException("The specified future is still running");
        case CANCELLED -> ofCancelled(f.exceptionNow());
        case FAILED -> ofFailed(f.exceptionNow());
        case SUCCESS -> ofResult(f.resultNow());
        };
    }

    public static <T> Result<T> ofResult(T result) {
        return new Result<>(result, State.SUCCESS);
    }

    enum State {
        CANCELLED, FAILED, SUCCESS;
    }
}