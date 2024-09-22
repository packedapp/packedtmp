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

import internal.app.packed.container.wirelets.InternalBuildWirelet;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/**
 *
 */
public class ContainerWirelets {
    public static final class ContainerOverrideNameWirelet extends InternalBuildWirelet {

        /** The (validated) name to override with. */
        private final String name;

        /**
         * Creates a new name wirelet
         *
         * @param name
         *            the name to override any existing container name with
         */
        public ContainerOverrideNameWirelet(String name) {
            this.name = NameCheck.checkComponentName(name); // throws IAE
        }

        /** {@inheritDoc} */
        @Override
        public void onImageLaunch(ContainerSetup c, ApplicationLaunchContext ic) {
            ic.name = name;
        }

        /** {@inheritDoc} */
        @Override
        public void onBuild(PackedContainerInstaller installer) {
            installer.nameFromWirelet = name;// has already been validated
        }
    }
}
