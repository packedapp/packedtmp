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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import internal.app.packed.application.ApplicationSetup;

/**
 *
 */
public final class ApplicationHandlers extends Handlers {

    /** A handle that can access ApplicationConfiguration#application. */
    private static final VarHandle VH_APPLICATION_CONFIGURATION_TO_HANDLE = field(MethodHandles.lookup(), ApplicationConfiguration.class,
            "handle", ApplicationHandle.class);

    /** A handle that can access {@link BeanHandleHandle#bean}. */
    private static final VarHandle VH_APPLICATION_HANDLE_TO_SETUP = field(MethodHandles.lookup(), ApplicationHandle.class, "application",
            ApplicationSetup.class);

    /** A handle that can access ApplicationMirror#application. */
    private static final VarHandle VH_APPLICATION_MIRROR_TO_HANDLE = field(MethodHandles.lookup(), ApplicationMirror.class, "handle",
            ApplicationHandle.class);

    public static ApplicationHandle<?, ?> getApplicationConfigurationHandle(ApplicationConfiguration configuration) {
        return (ApplicationHandle<?, ?>) VH_APPLICATION_CONFIGURATION_TO_HANDLE.get(configuration);
    }

    public static ApplicationSetup getApplicationHandleApplication(ApplicationHandle<?, ?> handle) {
        return (ApplicationSetup) VH_APPLICATION_HANDLE_TO_SETUP.get(handle);
    }

    public static ApplicationHandle<?, ?> getApplicationMirrorHandle(ApplicationMirror mirror) {
        return (ApplicationHandle<?, ?>) VH_APPLICATION_MIRROR_TO_HANDLE.get(mirror);
    }

}
