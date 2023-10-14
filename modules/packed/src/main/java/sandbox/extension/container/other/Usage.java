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
package sandbox.extension.container.other;

import app.packed.container.ContainerConfiguration;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.Extension;
import app.packed.service.ServiceLocator;
import sandbox.extension.container.ContainerCarrierBeanConfiguration;
import sandbox.extension.container.ContainerCarrierService;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */
public class Usage extends Extension<Usage> {

    private static final ContainerTemplate CT = ContainerTemplate.GATEWAY.carrierType(Guest.class).withPack(BaseExtensionPoint.EXPORTED_SERVICE_LOCATOR);

    public ContainerConfiguration installContainerWithImplicitCarrier() {
        ContainerHandle h = base().newContainer(CT).build();
        return new ContainerConfiguration(h);
    }

    public ContainerConfiguration installContainerWithExplicitCarrier() {
        ContainerCarrierBeanConfiguration<Guest> chg = base().installContainerCarrier(Guest.class).overrideService(String.class, "Ssdo");
        ContainerHandle h = base().newContainer(CT).carrierUse(chg).build();
        return new ContainerConfiguration(h);
    }

    record Guest(@ContainerCarrierService ServiceLocator sl, String foo) {}
}
