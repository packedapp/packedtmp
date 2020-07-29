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
package sandbox.artifact.hostguest;

import app.packed.component.ComponentDriver;
import app.packed.container.ContainerBundle;
import sandbox.component.ConfiguredBy;

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

    static ComponentDriver<AppHostConfiguration> driver2() {
        throw new UnsupportedOperationException();
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