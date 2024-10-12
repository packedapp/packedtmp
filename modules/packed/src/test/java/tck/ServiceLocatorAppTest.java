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
package tck;

import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.component.guest.FromComponentGuest;
import app.packed.operation.Op1;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class ServiceLocatorAppTest extends AbstractBootstrapedAppTest<ServiceLocator> {

    private static final BootstrapApp<ServiceLocator> APP = BootstrapApp
            .of(ApplicationTemplate.ofUnmanaged(new Op1<@FromComponentGuest ServiceLocator, ServiceLocator>(e -> e) {}));

    public ServiceLocatorAppTest() {
        super(APP);
    }

}
