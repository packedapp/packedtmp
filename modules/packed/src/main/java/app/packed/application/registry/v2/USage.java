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
package app.packed.application.registry.v2;

import app.packed.application.ApplicationImage;
import app.packed.assembly.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class USage extends BaseAssembly {

    // Hvordan fortaeller vi den hvordan den skal lave et image
    // Baade template og Image????
    // Reeelt set skal vi jo ikke se Handle
    void foo(ApplicationRepository<Image, ?> repo) {
        repo.image("df").initialize();

        repo.image("df").named("haha").initialize();

        repo.install(i -> i.install(new USage()));
        repo.install(i -> i.named("blabla").install(new USage()));
        repo.install(i -> i.install(new USage(), Wirelet.named("blabla")));
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {}

    interface Image extends ApplicationImage {

        @Override
        Image named(String name);

        /** Creates a new service locator application from this image. */
        ServiceLocator initialize();
    }
}
