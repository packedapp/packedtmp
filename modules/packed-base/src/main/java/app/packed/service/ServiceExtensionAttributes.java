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

/**
 *
 */
public class ServiceExtensionAttributes {
    public static final Attribute<String> SERVICE_DESCRIPTION = Attribute.of(MethodHandles.lookup(), "description", String.class);

    public static final Attribute<String> SERVICE_OTHER = Attribute.of(MethodHandles.lookup(), "other", String.class);

    public static final Attribute<ServiceContract> SERVICE_CONTRACT = Attribute.of(MethodHandles.lookup(), "contract", ServiceContract.class);
}
