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
package packed.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Various {@link Exception} and {@link Throwable} utility methods.
 *
 */
public class ThrowableUtil {

    /**
     * Throws the specified throwable if it is an {@link Error} or a {@link RuntimeException}. Otherwise returns the
     * specified throwable.
     *
     * @param throwable
     *            the throwable
     * @return the specified throwable if it not an Error or a RuntimeException
     */
    public static <T extends Throwable> T rethrowErrorOrRuntimeException(T throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        return throwable;
    }

    public static <T extends Throwable> T rethrowErrorOrException(T throwable) throws Exception {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        } else if (throwable instanceof Exception) {
            throw (Exception) throwable;
        }
        return throwable;
    }

    public static <T extends Throwable> T stripLastInvocation(T t, String className, String methodName) {
        StackTraceElement[] elements = t.getStackTrace();
        int last = -1;
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement ste = elements[i];
            if (ste.getClassName().equals(className) && ste.getMethodName().equals(methodName)) {
                last = i;
            }
        }
        if (last > 0) {
            t.setStackTrace(Arrays.copyOfRange(elements, last + 1, elements.length));
        }
        return t;
    }

    public static Throwable fromCompletionFuture(CompletableFuture<?> cf) {
        if (cf.isCompletedExceptionally()) {
            try {
                cf.getNow(null);
            } catch (CancellationException e) {
                return e;
            } catch (CompletionException e) {
                return e.getCause();
            }
        }
        return null;
    }

    public static void throwAny(Throwable e) {
        ThrowableUtil.<RuntimeException>doThrow0(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void doThrow0(Throwable e) throws E {
        throw (E) e;
    }

    public static String format(Throwable thrown) {
        if (thrown == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        thrown.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
