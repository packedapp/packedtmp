/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.service.ServiceNamespaceConfiguration;
import app.packed.service.ServiceNamespaceMirror;
import internal.app.packed.service.ServiceNamespaceHandle;

/**
 * Access helper for service namespace and related classes.
 */
public abstract class ServiceAccessHandler extends AccessHelper {

    private static final Supplier<ServiceAccessHandler> CONSTANT = StableValue.supplier(() -> init(ServiceAccessHandler.class, ServiceNamespaceConfiguration.class));

    public static ServiceAccessHandler instance() {
        return CONSTANT.get();
    }

//    /**
//     * Creates a new ServiceNamespaceConfiguration.
//     *
//     * @param handle the service namespace handle
//     * @param extension the base extension
//     * @return the configuration
//     */
//    public abstract ServiceNamespaceConfiguration newServiceNamespaceConfiguration(ServiceNamespaceHandle handle, BaseExtension extension);

    /**
     * Creates a new ServiceNamespaceMirror.
     *
     * @param handle the service namespace handle
     * @return the mirror
     */
    public abstract ServiceNamespaceMirror newServiceNamespaceMirror(ServiceNamespaceHandle handle);
}
