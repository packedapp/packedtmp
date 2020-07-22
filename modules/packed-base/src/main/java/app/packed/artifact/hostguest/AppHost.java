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
package app.packed.artifact.hostguest;

import app.packed.component.sandhox.ConfiguredBy;
import app.packed.container.ContainerBundle;

/**
 *
 */
//extends ConfiguredVia<AppHostConfiguration>  <--- skal have en statisk metode der hedder driver...
public interface AppHost extends ConfiguredBy<AppHostConfiguration> {

    long size();

    // Eller skal den vaere paa configurationen???
    static HostDriver<AppHostConfiguration> driver() {
        return AppHostConfiguration.DRIVER;
    }
}

class FooBar extends ContainerBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        // <- must have a static driver method... and be open to packed... (and readable to bundle)
        add(AppHost.class);
    }
}