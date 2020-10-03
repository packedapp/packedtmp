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
package packed.internal.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Various {@link Throwable} utility methods. */
public final class ThrowableUtil {

    /** Cannot instantiate. */
    private ThrowableUtil() {}

    /**
     * If the specified future has returned exceptionally, returns the cause. Otherwise returns null.
     * 
     * @param future
     *            the completable future
     * @return any throwable the specified future has been completed with, or null if it has not been completed or has
     *         completed normally.
     */
    public static Throwable fromCompletionFuture(CompletableFuture<?> future) {
        if (future.isCompletedExceptionally()) {
            try {
                future.getNow(null);
            } catch (CancellationException e) {
                return e;
            } catch (CompletionException e) {
                return e.getCause();
            }
        }
        return null;
    }

    /**
     * Throws the specified throwable if it is an {@link Error} or a {@link RuntimeException}. Otherwise returns the
     * specified throwable.
     *
     * @param throwable
     *            the throwable
     * @return the specified throwable if it not an Error or a RuntimeException
     */
    public static UndeclaredThrowableException orUndeclared(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        return new UndeclaredThrowableException(throwable);
    }

    /**
     * Throws the specified throwable ignore whatever type it has
     * 
     * @param throwable
     *            the throwable to throw
     */
    public static void throwAny(Throwable throwable) {
        ThrowableUtil.<RuntimeException>throwAny0(throwable);
    }

    public static <T> T throwReturn(Throwable throwable) {
        ThrowableUtil.<RuntimeException>throwAny0(throwable);
        throw new Error("This should never happen");
    }

    /**
     * Throws the specified throwable.
     * 
     * @param <E>
     *            the type of throwable to throw
     * @param throwable
     *            throwable to throw
     * @throws E
     *             the throwable to throw
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAny0(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
//
///**
// * Throws the specified throwable if it is an {@link Error} or a {@link Exception}. Otherwise returns the specified
// * throwable.
// *
// * @param <T>
// *            the type of throwable
// * @param throwable
// *            the throwable
// * @return the specified throwable if it not an Error or a Exception
// * @throws Exception
// *             if the specified throwable is a checked exception
// */
//public static <T extends Throwable> T rethrowErrorOrException(T throwable) throws Exception {
//    if (throwable instanceof Error) {
//        throw (Error) throwable;
//    } else if (throwable instanceof Exception) {
//        throw (Exception) throwable;
//    }
//    return throwable;
//}
//
//public static <T extends Throwable> T throwIfUnchecked(T throwable) {
//    if (throwable instanceof Error) {
//        throw (Error) throwable;
//    } else if (throwable instanceof RuntimeException) {
//        throw (RuntimeException) throwable;
//    }
//    return throwable;
//}