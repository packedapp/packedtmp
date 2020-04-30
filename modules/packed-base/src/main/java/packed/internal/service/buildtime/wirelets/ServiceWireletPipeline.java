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
package packed.internal.service.buildtime.wirelets;

import static java.util.Objects.requireNonNull;

import app.packed.container.ExtensionMember;
import app.packed.container.WireletPipeline;
import app.packed.service.ServiceExtension;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.service.buildtime.ServiceExtensionNode;

/** The default wirelet pipeline for */
@ExtensionMember(ServiceExtension.class)
public final class ServiceWireletPipeline extends WireletPipeline<ServiceWireletPipeline, ServiceWirelet> {

    /** The service extension node */
    final ServiceExtensionNode node;

    ServiceWireletPipeline(ServiceExtension extension) {
        this.node = requireNonNull(ModuleAccess.service().extensionToNode(extension));
    }
}