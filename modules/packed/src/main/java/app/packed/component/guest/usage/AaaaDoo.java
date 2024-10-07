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
package app.packed.component.guest.usage;

import java.util.concurrent.atomic.AtomicLong;

import app.packed.application.App;
import app.packed.application.ApplicationRepository;
import app.packed.application.ApplicationRepositoryConfiguration;
import app.packed.assembly.BaseAssembly;
import app.packed.component.guest.usage.GuestBean.GuestApplicationHandle;
import app.packed.lifecycle.OnInitialize;

/**
 *
 */
public class AaaaDoo extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        ApplicationRepositoryConfiguration<GuestBean, GuestApplicationHandle> repo = ApplicationRepository.provide(GuestBean.T, base());
        repo.installApplication(i -> i.named("foo").install(new SubApplication()));
        install(MyBean.class);
    }

    public static void main(String[] args) {
        App.run(new AaaaDoo());
    }

    public record MyBean(ApplicationRepository<GuestBean, GuestApplicationHandle> repository) {

        static AtomicLong al = new AtomicLong();

        @OnInitialize
        public void oni() {
            repository.launchers().forEach(c -> {
                for (int i = 0; i < 10; i++) {
                    c.startGuest();
                }
            });

            if (al.incrementAndGet() < 10) {
                repository.install(i -> i.install(new AaaaDoo())).startGuest();
            }
        }
    }
}