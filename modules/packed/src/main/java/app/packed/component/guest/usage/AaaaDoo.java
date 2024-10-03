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
import app.packed.application.repository.ApplicationRepository;
import app.packed.application.repository.ApplicationRepositoryConfiguration;
import app.packed.assembly.BaseAssembly;
import app.packed.binding.Key;
import app.packed.component.guest.usage.GuestBean.GuestApplicationHandle;
import app.packed.lifecycle.OnInitialize;
import app.packed.runtime.RunState;

/**
 *
 */
public class AaaaDoo extends BaseAssembly {

    static AtomicLong al = new AtomicLong();

    /** {@inheritDoc} */
    @Override
    protected void build() {
        ApplicationRepositoryConfiguration<GuestApplicationHandle> c = ApplicationRepository.install(GuestBean.T, base());

        c.provideAs(new Key<ApplicationRepository<GuestApplicationHandle>>() {});
        c.installChildApplication(i -> i.named("foo").install(new SubApplication()));
        c.installChildApplication(i -> i.named("foox").install(new SubApplication()));


        install(MyBean.class);
    }

    public static void main(String[] args) {
        App.run(new AaaaDoo());
    }

    public record MyBean(ApplicationRepository<GuestApplicationHandle> repository) {

        @OnInitialize
        public void oni() {
            repository.stream().forEach(c -> {
                for (int i = 0; i < 100; i++) {
                    GuestBean b = c.launch(RunState.RUNNING);
                    System.out.println(b.nanos());
                }
                System.out.println("Sub app " + c.mirror().name());
            });

            if (al.incrementAndGet() < 100) {
                System.out.println(al.get());
                repository.newApplication().install(new AaaaDoo()).launch(RunState.RUNNING);
            }
        }
    }
}
