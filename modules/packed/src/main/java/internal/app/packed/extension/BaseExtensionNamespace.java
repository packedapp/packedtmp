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
package internal.app.packed.extension;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionNamespace;
import app.packed.extension.ExtensionNamespaceHandle;
import internal.app.packed.invoke.MethodHandleInvoker.ExportedServiceWrapper;
import internal.app.packed.service.ExportedService;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.service.util.ServiceMap;
import internal.app.packed.util.accesshelper.ExtensionAccessHandler;

/**
 * The extension namespace for {@link BaseExtension}.
 */
public final class BaseExtensionNamespace extends ExtensionNamespace<BaseExtensionNamespace, BaseExtension> {

    /** All service providers in the namespace. */
    public final ServiceMap<NamespaceServiceProviderHandle> serviceProviders = new ServiceMap<>();

    // All provided services are automatically exported
    public boolean exportAll;

    /** A map of exported service method method handles, must be computed. */
    @Nullable
    private Map<Key<?>, ExportedServiceWrapper> exportedServices;

    /** Exported services from the container. */
    public final ServiceMap<ExportedService> exports = new ServiceMap<>();

    protected BaseExtensionNamespace(ExtensionNamespaceHandle<BaseExtensionNamespace, BaseExtension> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public BaseExtension newExtension(ExtensionHandle<BaseExtension> handle) {
        return ExtensionAccessHandler.instance().create_BaseExtension(this, handle);
    }
}
