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
package internal.app.packed.util;

import java.lang.reflect.UndeclaredThrowableException;

/** Various {@link Throwable} utility methods. */
public final class ThrowableUtil {

    /** Cannot instantiate. */
    private ThrowableUtil() {}

    /**
     * Throws the specified throwable if it is an {@link Error} or a {@link RuntimeException}. Otherwise returns the
     * specified throwable.
     *
     * @param throwable
     *            the throwable
     * @return the specified throwable if it not an Error or a RuntimeException
     */
    public static UndeclaredThrowableException orUndeclared(Throwable throwable) {
        if (throwable instanceof RuntimeException re) {
            throw re;
        } else if (throwable instanceof Error er) {
            throw er;
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
