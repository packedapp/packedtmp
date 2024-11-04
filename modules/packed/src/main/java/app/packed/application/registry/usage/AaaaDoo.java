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
package app.packed.application.registry.usage;

import java.util.concurrent.atomic.AtomicLong;

import app.packed.application.App;
import app.packed.application.registry.ApplicationRegistry;
import app.packed.application.registry.ApplicationRegistryConfiguration;
import app.packed.application.registry.ApplicationRegistryExtension;
import app.packed.application.registry.usage.SimpleManagedApplication.GuestApplicationHandle;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.Initialize;

/**
 *
 */
public class AaaaDoo extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        ApplicationRegistryConfiguration<SimpleManagedApplication, GuestApplicationHandle> repo = use(ApplicationRegistryExtension.class)
                .provideRegistry(SimpleManagedApplication.MANAGED_SUB_APPLICATION);
        repo.installApplication(i -> i.named("foo").install(new SubApplication()));
        install(MyBean.class);
    }

    public static void main(String[] args) {
        App.run(new AaaaDoo());
    }

    public record MyBean(ApplicationRegistry<SimpleManagedApplication, GuestApplicationHandle> repository) {

        static AtomicLong al = new AtomicLong();

        @Initialize
        public void oni() {
            repository.applications().forEach(c -> {
                for (int i = 0; i < 10; i++) {
                    c.startNew();
                }
            });

            if (al.incrementAndGet() < 10) {
                repository.install(i -> i.install(new AaaaDoo())).startNew();
            }
        }
    }
}
