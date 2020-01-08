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
package packed.internal.host.test;

import app.packed.artifact.AppHost;
import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.HostConfigurationContext;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.lang.Key;

/**
 *
 */

// Why host...

// You want to be able to remove it....
// You want want delayed startup
// Replace it.

public class MyHostConf extends AbstractServiceableConfiguration<AppHost> {

    @Override
    public MyHostConf setName(String name) {
        super.setName(name);
        return this;
    }

    /**
     * @param wrapper
     */
    protected MyHostConf(HostConfigurationContext wrapper) {
        super(wrapper);
    }

    @Override
    public MyHostConf as(Class<? super AppHost> key) {
        super.as(key);
        return this;
    }

    @Override
    public MyHostConf as(Key<? super AppHost> key) {
        super.as(key);
        return this;
    }

    public void deploy(ContainerSource source, ArtifactDriver<?> driver, Wirelet... wirelets) {
        context.deploy(source, driver, wirelets);
    }

    public void lazyDeploy(ContainerSource source, ArtifactDriver<?> driver, Wirelet... wirelets) {
        context.deploy(source, driver, wirelets);
    }

    //// Nej det betyder jo at alle kan tilfoeje det....
    // AppHost.configure(Bundle b);
}
