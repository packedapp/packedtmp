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
package sandbox.extension.container;

import app.packed.component.guest.FromGuest;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerTemplate;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class Usage extends Extension<Usage> {

    /**
     * @param handle
     */
    protected Usage(ExtensionHandle handle) {
        super(handle);
    }

    private static final ContainerTemplate CT = ContainerTemplate.GATEWAY
            .reconfigure(c -> c.carrierType(Guest.class).withPack(BaseExtensionPoint.EXPORTED_SERVICE_LOCATOR));

    public ContainerConfiguration installContainerWithImplicitCarrier() {
        ContainerHandle<?> h = base().newContainer(CT).install(ContainerHandle::new);
        return h.configuration();
    }

//    public ContainerConfiguration installContainerWithExplicitCarrier() {
//        ComponentGuestAdaptorBeanConfiguration<Guest> chg = null;// base().installContainerHost(Guest.class).bindInstance(String.class, "Ssdo");
//        ContainerHandle<?> h = base().newContainer(CT).carrierUse(chg).install(ContainerHandle::new);
//        return h.configuration();
//    }

    record Guest(@FromGuest ServiceLocator sl, String foo) {}
}
