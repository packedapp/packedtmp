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
package internal.app.packed.util.notused;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public class Checks {

    public static String checkLetterNumberUnderscoreDotOrHyphen(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.') {
                throw new IllegalArgumentException("The specified name must only consist of letters, digits, '-', '_' and '.', name = " + name);
            }
        }
        return name;
    }

    public static double checkFiniteDouble(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Cannot specify infinity or NaN as value, was " + value);
        }
        return value;
    }

    public static float checkFiniteFloat(float value) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException("Cannot specify infinity or NaN as value, was " + value);
        }
        return value;
    }

    /**
     * Checks whether or not the specified array is {@code null} or contains a {@code null} at any index.
     *
     * @param a
     *            the array to check
     * @return the specified array
     * @throws NullPointerException
     *             if the specified array is null or contains a null at any index
     */
    @SafeVarargs
    public static <T> T[] requireNonNullArray(String parameterName, T... a) {
        requireNonNull(a, parameterName + " is null");
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null) {
                throw new NullPointerException(parameterName + " array contains a null at index " + i);
            }
        }
        return a;
    }
}
