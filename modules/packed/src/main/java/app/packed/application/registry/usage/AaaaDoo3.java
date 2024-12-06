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

import app.packed.application.App;
import app.packed.application.registry.ApplicationRegistryExtension;
import app.packed.application.registry.LaunchableApplication;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.OnStart;
import app.packed.runtime.ManagedLifecycle;

/**
 * Wauv...
 */
public class AaaaDoo3 extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        use(ApplicationRegistryExtension.class).provideApplication(SimpleManagedApplication.MANAGED, i -> i.install(new SubApplication()));
        install(MyBean.class);
    }

    public static void main(String[] args) {
        App.run(new AaaaDoo3());
    }

    public record MyBean(LaunchableApplication<ManagedLifecycle> launcher) {

        @OnStart
        public void oni() {
            for (int i = 0; i < 10; i++) {
                launcher.startNew();
            }
        }
    }
}
