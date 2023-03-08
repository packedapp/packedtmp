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

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * <p>
 * This class is modelled on the basis of {@link Future}.
 *
 */
// https://www.reddit.com/r/Kotlin/comments/oi9rh2/so_glad_that_result_is_finalized_in_15/
public final /* primitive */ class Result<T> {

    /** The result, or an exception if completed exceptional. */
    private final Object result;

    /** The state of the result. */
    private final State state;

    private Result(Object result, State state) {
        this.result = result;
        this.state = state;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Result<?> other && Objects.equals(state, other.state) && Objects.equals(result, other.result));
    }

    public Throwable exception() {
        if (state != State.SUCCESS) {
            return (Throwable) result;
        }
        throw new IllegalStateException("Result was success");
    }

    public T get() {
        if (isSuccess()) {
            return t();
        }
        throw new IllegalStateException("Result was a failure", (Throwable) result);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(result);
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

    @SuppressWarnings("unchecked")
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isFailed()) {
            return (Result<U>) this;
        } else {
            return Result.ofSuccess(mapper.apply(t()));
        }
    }

    /**
     * If a value was successfully computed, returns the value, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned, if a value failed to be computed. May be {@code null}.
     * @return the value, if successfully computed, otherwise {@code other}
     */
    public T orElse(T other) {
        return isSuccess() ? t() : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return isSuccess() ? t() : supplier.get();
    }

    @SuppressWarnings("unchecked")
    private T t() {
        return (T) result;
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
        case SUCCESS -> ofSuccess(f.resultNow());
        };
    }

    public static <T> Result<T> ofSuccess(T result) {
        return new Result<>(result, State.SUCCESS);
    }

    enum State {
        CANCELLED, FAILED, SUCCESS;
    }
}