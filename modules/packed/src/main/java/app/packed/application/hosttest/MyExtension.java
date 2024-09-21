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

import app.packed.application.ApplicationRepositoryConfiguration;
import app.packed.application.ApplicationTemplate;
import app.packed.application.hosttest.AaaaDoo.GuestBean;
import app.packed.extension.Extension;

public class MyExtension extends Extension<MyExtension> {
    MyExtension() {}

    @Override
    protected void onNew() {

    }

    public ApplicationRepositoryConfiguration<MyAppHandle, GuestBean> newRepo(ApplicationTemplate<GuestBean> template) {
        ApplicationRepositoryConfiguration<MyAppHandle, GuestBean> c = ApplicationRepositoryConfiguration.install(base(), template);
        return c;
    }

    @Override
    protected void onAssemblyClose() {
        super.onAssemblyClose();
    }

}