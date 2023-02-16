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
package app.packed.extension.container.other;

import app.packed.container.ContainerConfiguration;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.Extension;
import app.packed.extension.container.ContainerHandle;
import app.packed.extension.container.ContainerHolderConfiguration;
import app.packed.extension.container.ContainerHolderService;
import app.packed.extension.container.ContainerTemplate;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class Usage extends Extension<Usage> {
    private static final ContainerTemplate CT = ContainerTemplate.OPERATION.linkWith(BaseExtensionPoint.EXPORTED_SERVICE_LOCATOR);

    public ContainerConfiguration foo() {
        ContainerHolderConfiguration<Guest> chg = base().containerHolderInstallIfAbsent(Guest.class, c -> {});
        ContainerHandle h = base().containerBuilder(CT).holder(chg).build();
        return new ContainerConfiguration(h);
    }

    record Guest(@ContainerHolderService ServiceLocator sl) {}
}
