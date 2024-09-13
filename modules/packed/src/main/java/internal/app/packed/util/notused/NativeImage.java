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

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A helper class for creating native images.
 */
// Ideen er at folk faar en NativeImageSupport instance til at kalde ting...
// Det er f.eks. extensions der kan faa det...
// Skal vi have en instance af det?????

// NativeImageHelper, NativeImageSupport, NativeImageUtil

// Er ikke sikker paa vi gider lave denne offentlig som standard
// Folk maa selv kalde ind i graal
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

    public static void registerField(Field field) {
       // NativeImage0.register(field);
    }

    public static void register(Executable m) {
       // NativeImage0.register(m);
    }
    // from
    // https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java
    static final String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode";

    static final String PROPERTY_IMAGE_CODE_VALUE_BUILDTIME = "buildtime";

    static final String PROPERTY_IMAGE_CODE_VALUE_RUNTIME = "runtime";

    public static boolean inImageCode() {
        return System.getProperty(PROPERTY_IMAGE_CODE_KEY) != null;
    }

    public static boolean inImageRuntimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_RUNTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

    public static boolean inImageBuildtimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_BUILDTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }
    // Problemet er lidt. Hvordan kan vi lave de statements
    // der kalder Buildint_NATIVE som noops naar man er native?
    // public static void support(AnyAssembly b) {
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
