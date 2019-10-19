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
package app.packed.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A helper class for creating native images.
 */
// Ideen er at folk faar en NativeImageSupport instance til at kalde ting...
// Det er f.eks. extensions der kan faa det...
// Skal vi have en instance af det?????

// NativeImageHelper, NativeImageSupport, NativeImageUtil
public final class NativeImage {

    public void saveMethod(Method m) {}

    protected static void checkNotNative(String message) {
        // for example
        // checkNotNative("Cannot change the JDBC driver, when running in native mode, was " + driver);
        // check grall
    }

    public static Mode mode() {
        return Mode.NO_IMAGE;
    }

    public static void registerConstructor(Constructor<?> c) {}

    public static void registerMethod(Method m) {}

    // Problemet er lidt. Hvordan kan vi lave de statements
    // der kalder Buildint_NATIVE som noops naar man er native?
    // public static void support(AnyBundle b) {
    //
    // }

    // Active_Generating, Active_Executing, Inactive?
    // Active_Profiling?
    public enum Mode {

        /** A mode indicating that we are building are native image. */
        IMAGE_GENERATION,

        /** A mode indicating that we are running with a native image. */
        IMAGE_RUN,

        /** A mode indicating that we neither building or running a native image. */
        NO_IMAGE;
    }

    // IMAGE_GENERATE
}
