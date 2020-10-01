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
package app.packed.service;

import java.lang.invoke.MethodHandles;

import app.packed.base.Attribute;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceRegistry;

/**
 *
 */
// Attributes that are available on a Service, on a ServiceExtension, at Build time, atRuntime...
public final class ServiceAttributes {

    private ServiceAttributes() {}

    /** An attribute that is present on {@link ServiceExtension} components that have at least 1 exported service. */
    public static final Attribute<ServiceRegistry> EXPORTED_SERVICES = Attribute.of(MethodHandles.lookup(), "exported-services", ServiceRegistry.class);

    // I think we use the exported service

    // Not so quick we also have requirements..... ARGHHHH

    public static final Attribute<ServiceContract> SERVICE_CONTRACT = Attribute.of(MethodHandles.lookup(), "contract", ServiceContract.class);
}
