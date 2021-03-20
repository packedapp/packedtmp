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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

/**
 *
 */
class NativeImage0 {

    private static final MethodHandle MH_REGISTER_EXECUTABLES;
    private static final MethodHandle MH_REGISTER_FIELDS;

    // from
    // https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java
    static final String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode";

    static final String PROPERTY_IMAGE_CODE_VALUE_BUILDTIME = "buildtime";

    static final String PROPERTY_IMAGE_CODE_VALUE_RUNTIME = "runtime";
    static {
        if (inImageCode()) {
            Class<?> rr = null;
            try {
                rr = Class.forName("org.graalvm.nativeimage.hosted.RuntimeReflection");
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(e);
            }

            try {
                MH_REGISTER_EXECUTABLES = LookupUtil.lookupStaticPublic(rr, "register", void.class, Executable[].class);
                MH_REGISTER_FIELDS = LookupUtil.lookupStaticPublic(rr, "register", void.class, Field[].class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ExceptionInInitializerError(e);
            }
        } else {
            MH_REGISTER_EXECUTABLES = null;
            MH_REGISTER_FIELDS = null;
        }

        // MH_REGISTER_EXECUTABLES = null;
        // MH_REGISTER_FIELDS = null;
    }

    static void register(Executable... e) {
        if (MH_REGISTER_EXECUTABLES != null) {
            if (inImageRuntimeCode()) {
                throw new IllegalStateException("OOPS, this method should have called when creating the image");
            }
            try {
                MH_REGISTER_EXECUTABLES.invokeExact(e); // TODO change to invoke exact
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    static void register(Field... f) {
        if (MH_REGISTER_FIELDS != null) {
            if (inImageRuntimeCode()) {
                throw new IllegalStateException("OOPS, this method should have called when creating the image");
            }
            try {
                MH_REGISTER_FIELDS.invokeExact(f);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean inImageCode() {
        return System.getProperty(PROPERTY_IMAGE_CODE_KEY) != null;
    }

    public static boolean inImageRuntimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_RUNTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

    public static boolean inImageBuildtimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_BUILDTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }
}
