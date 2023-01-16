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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;
import internal.app.packed.container.CompositeWirelet;
import internal.app.packed.container.WireletWrapper;

/**
 *
 */
public final class RuntimeApplicationLauncher {

    private final ApplicationSetup application;

    public RuntimeApplicationLauncher(ApplicationSetup application) {
        this.application = application;
    }

    public <A> A launchImmediately(ApplicationDriver<A> driver) {
        return ApplicationInitializationContext.launch(driver, application, null);
    }

    public <A> A launchFromImage(ApplicationDriver<A> driver, Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");

        // If launching an image, the user might have specified additional runtime wirelets
        WireletWrapper wrapper = null;
        if (wirelets.length > 0) {
            wrapper = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
        }
        return ApplicationInitializationContext.launch(driver, application, wrapper);
    }
}
