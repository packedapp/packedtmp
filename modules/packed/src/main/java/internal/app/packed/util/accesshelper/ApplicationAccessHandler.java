/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import internal.app.packed.application.ApplicationSetup;

/**
 * Access helper for ApplicationHandle and related classes.
 */
public abstract class ApplicationAccessHandler extends AccessHelper {

    private static final Supplier<ApplicationAccessHandler> CONSTANT = StableValue.supplier(() -> init(ApplicationAccessHandler.class, ApplicationHandle.class));

    public static ApplicationAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Gets the ApplicationHandle from an ApplicationConfiguration.
     *
     * @param configuration the configuration
     * @return the application handle
     */
    public abstract ApplicationHandle<?, ?> getApplicationConfigurationHandle(ApplicationConfiguration configuration);

    /**
     * Gets the ApplicationSetup from an ApplicationHandle.
     *
     * @param handle the handle
     * @return the application setup
     */
    public abstract ApplicationSetup getApplicationHandleApplication(ApplicationHandle<?, ?> handle);

    /**
     * Gets the ApplicationHandle from an ApplicationMirror.
     *
     * @param mirror the mirror
     * @return the application handle
     */
    public abstract ApplicationSetup getApplicationMirrorHandle(ApplicationMirror mirror);
}
