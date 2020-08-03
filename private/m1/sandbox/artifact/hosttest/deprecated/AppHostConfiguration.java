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
package sandbox.artifact.hosttest.deprecated;

import app.packed.artifact.App;
import app.packed.component.ComponentDriver;
import sandbox.artifact.hostguest.HostConfigurationContext;

/**
 *
 */
public final class AppHostConfiguration extends HostConfiguration<AppHost> {

    /** An artifact driver for creating {@link App} instances. */
    static final ComponentDriver<AppHostConfiguration> DRIVER = new ComponentDriver<>() {};

    /**
     * @param context
     */
    AppHostConfiguration(HostConfigurationContext context) {
        super(null, null); // Maybe move it to driver... Then we have to generify the driver...
    }

}
