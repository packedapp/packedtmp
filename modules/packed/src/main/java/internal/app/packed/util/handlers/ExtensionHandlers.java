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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionUseSite;

/** Var and method handles for the app.packed.extension package. */
public final class ExtensionHandlers extends Handlers {

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = method(MethodHandles.lookup(), Extension.class, "newBeanIntrospector",
            BeanIntrospector.class);

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_MIRROR = method(MethodHandles.lookup(), Extension.class, "newExtensionMirror",
            ExtensionMirror.class);

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_POINT = method(MethodHandles.lookup(), Extension.class, "newExtensionPoint",
            ExtensionPoint.class, ExtensionUseSite.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = method(MethodHandles.lookup(), Extension.class, "onApplicationClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = method(MethodHandles.lookup(), Extension.class, "onAssemblyClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onNew()}. */
    private static final MethodHandle MH_EXTENSION_ON_NEW = method(MethodHandles.lookup(), Extension.class, "onNew", void.class);

    /** A handle that can access {@link ContainerHandleHandle#container}. */
    private static final VarHandle VH_EXTENSION_POINT_TO_USESITE = field(MethodHandles.lookup(), ExtensionPoint.class, "usesite",
            PackedExtensionUseSite.class);

    /** A var handle for {@link #getExtensionHandle(Extension)}. */
    private static final VarHandle VH_EXTENSION_TO_HANDLE = field(MethodHandles.lookup(), Extension.class, "extension", ExtensionSetup.class);

    public static ExtensionSetup getExtensionHandle(Extension<?> extension) {
        return (ExtensionSetup) VH_EXTENSION_TO_HANDLE.get(extension);
    }

    public static PackedExtensionUseSite getExtensionPointPackedExtensionUseSite(ExtensionPoint<?> extensionPoint) {
        return (PackedExtensionUseSite) VH_EXTENSION_POINT_TO_USESITE.get(extensionPoint);
    }

    public static BeanIntrospector invokeExtensionNewBeanIntrospector(Extension<?> extension) {
        try {
            return (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static ExtensionMirror<?> invokeExtensionNewExtensionMirror(Extension<?> extension) {
        try {
            return (ExtensionMirror<?>) MH_EXTENSION_NEW_EXTENSION_MIRROR.invokeExact(extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static void invokeExtensionOnApplicationClose(Extension<?> extension) {
        try {
            MH_EXTENSION_ON_APPLICATION_CLOSE.invokeExact(extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public static void invokeExtensionOnAssemblyClose(Extension<?> extension) {
        try {
            MH_EXTENSION_ON_ASSEMBLY_CLOSE.invokeExact(extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static void invokeExtensionOnNew(Extension<?> extension) {
        try {
            MH_EXTENSION_ON_NEW.invokeExact(extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public static ExtensionPoint<?> newExtensionPoint(Extension<?> extension, ExtensionUseSite usesite) {
        try {
            return (ExtensionPoint<?>) MH_EXTENSION_NEW_EXTENSION_POINT.invokeExact(extension, usesite);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
