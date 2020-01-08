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
package app.packed.artifact;

import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 *
 */

// permanent | ikke-permanent
// Hvor langt skal man gaa naar man deployer... Initialize | start | ect.
public class AppHostConfiguration {

    AppHostConfiguration(HostConfigurationContext context) {

    }

    // Cannot return guest, because it might be an image...
    // Maximum we can do is return a ref, that can be resolved at runtime...
    void link(ContainerSource bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

}

class MyAppHostConfiguration extends AppHostConfiguration {

    /**
     * @param context
     */
    protected MyAppHostConfiguration(HostConfigurationContext context) {
        super(context);
    }

    public void onStarted(App app) {

    }
}
