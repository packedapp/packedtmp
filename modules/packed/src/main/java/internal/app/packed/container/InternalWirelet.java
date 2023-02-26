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
package internal.app.packed.container;

import app.packed.container.Wirelet;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/** Internal wirelets have their logic directly embedded into the wirelet. */
public abstract class InternalWirelet extends Wirelet {

    /**
     * Checks that the specified component is the root component (container) of an application.
     *
     * @param component
     *            the component to check
     * @throws IllegalArgumentException
     *             if the specified component is not the root component of an application
     * @return the application of the component (for method chaining)
     */
    protected final ApplicationSetup checkIsApplication(ContainerSetup container) {
        ApplicationSetup application = container.application;
        if (application.container != container) {
            throw new IllegalArgumentException("This wirelet can only be specified when wiring the root container of an application, wirelet = " + this);
        }
        return application;
    }

    public void onImageInstantiation(ContainerSetup component, ApplicationInitializationContext context) {
        throw new IllegalArgumentException(
                "The wirelet {" + getClass().getSimpleName() + "} must be specified at build-time. It cannot be specified when instantiating an image");
    }

    /**
     * Invoked by the runtime when the component is initially wired at build-time.
     *
     * @param installer
     *            an installer for the container
     */
    public abstract void onInstall(AbstractContainerBuilder installer);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
