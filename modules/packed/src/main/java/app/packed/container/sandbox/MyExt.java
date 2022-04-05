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
package app.packed.container.sandbox;

import app.packed.container.sandbox.MyExt.MyBootstrap;
import app.packed.extension.Extension;
import app.packed.extension.Extension.BootstrapWith;
import app.packed.inject.service.ServiceExtension;

/**
 *
 */
@BootstrapWith(MyBootstrap.class)
public class MyExt extends Extension<MyExt> {

    static class MyBootstrap extends Bootstrap {

        /** {@inheritDoc} */
        @Override
        protected void bootstrap() {
            dependsOn(ServiceExtension.class);
        }
    }
}
