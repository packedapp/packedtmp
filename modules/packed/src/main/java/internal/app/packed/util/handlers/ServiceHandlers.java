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

import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.service.ServiceNamespaceConfiguration;
import app.packed.service.ServiceNamespaceMirror;
import internal.app.packed.service.ServiceNamespaceHandle;

/**
 *
 */
public final class ServiceHandlers extends Handlers {

    /** A handle for invoking the protected method {@link NamespaceHandle#onNamespaceClose()}. */
    private static final MethodHandle MH_NEW_SERVICE_NAMESPACE_CONFIGURATION = constructor(MethodHandles.lookup(), ServiceNamespaceConfiguration.class,
            ServiceNamespaceHandle.class, BaseExtension.class, ComponentRealm.class);

    /** A handle for invoking the protected method {@link NamespaceHandle#onNamespaceClose()}. */
    private static final MethodHandle MH_NEW_SERVICE_NAMESPACE_MIRROR = constructor(MethodHandles.lookup(), ServiceNamespaceMirror.class,
            ServiceNamespaceHandle.class);

    /** Call {@link Extension#onAssemblyClose()}. */
    public static ServiceNamespaceConfiguration newServiceNamespaceConfiguration(ServiceNamespaceHandle handle, BaseExtension extension) {
        try {
            return (ServiceNamespaceConfiguration) MH_NEW_SERVICE_NAMESPACE_CONFIGURATION.invokeExact(handle, extension);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public static ServiceNamespaceMirror newServiceNamespaceMirror(ServiceNamespaceHandle handle) {
        try {
            return (ServiceNamespaceMirror) MH_NEW_SERVICE_NAMESPACE_MIRROR.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
