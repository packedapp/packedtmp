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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 */
// Ideen er at folk faar en NativeImageSupport instance til at kalde ting...
// Det er f.eks. extensions der kan faa det...
// Skal vi have en instance af det?????

// NativeImageHelper, NativeImageSupport
public final class NativeImage {

    public void saveMethod(Method m) {}

    public static Mode mode() {
        return Mode.NORMAL;
    }

    public static void registerMethod(Method m) {}

    public static void registerConstructor(Constructor<?> c) {}

    // Saa der er jo 3 modes..

    // Om vi koere i native
    // Om vi bygger til native
    // Alm

    // Problemet er lidt. Hvordan kan vi lave de statements
    // der kalder Buildint_NATIVE som noops naar man er native?
    public enum Mode {
        BUILDING_NATIVE, NATIVE, NORMAL;
    }
    // public static void support(AnyBundle b) {
    //
    // }
}
