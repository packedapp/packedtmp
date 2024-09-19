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
package app.packed.application.hosttest;

import static java.util.Objects.requireNonNull;

import app.packed.application.App;
import app.packed.application.ApplicationRepository;
import app.packed.application.ApplicationRepositoryConfiguration;
import app.packed.application.ApplicationTemplate;
import app.packed.assembly.BaseAssembly;
import app.packed.lifetime.OnInitialize;
import app.packed.util.Key;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;

/**
 *
 */
public class AaaaDoo extends BaseAssembly {

    ApplicationTemplate T = ApplicationTemplate.of(c -> {
        c.guest(GuestBean.class);
        c.container(new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION));
    });

    /** {@inheritDoc} */
    @Override
    protected void build() {
        ApplicationRepositoryConfiguration<MyAppHandle> c = use(MyExtension.class).newRepo();

        c.provideAs(new Key<ApplicationRepository<MyAppHandle>>() {});

        c.build1(T, "foo", i -> i.install(new SubApplication(), MyAppHandle::new));

        c.build1(T, "foox", i -> i.install(new SubApplication(), MyAppHandle::new));

        c.build1(T, "food", i -> i.install(new SubApplication(), MyAppHandle::new));

        install(MyBean.class);
//        c.build2(T, new SubApplication(), MyAppHandle::new);
    }

    static record GuestBean() {

    }

    public static void main(String[] args) {
        App.run(new AaaaDoo());
    }

    public static class MyBean {
        ApplicationRepository<MyAppHandle> repo;

        public MyBean(ApplicationRepository<MyAppHandle> repo) {
            this.repo = requireNonNull(repo);
        }

        @OnInitialize
        public void oni() {
            repo.stream().forEach(c -> {
                System.out.println("Sub app " + c.mirror().name());
            });
            MyAppHandle mh = repo.get("foo").get();

            ApplicationSetup as = ApplicationSetup.crack(mh);

            System.out.println(as);
        }
    }
}
