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
package app.packed.container;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import packed.internal.container.AssemblyRealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.PackedContainerDriver;

/**
 * An extension for adding contain new containers.
 */
public class ContainerExtension extends Extension {

    /** The container we installing beans into. */
    final ContainerSetup container;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object
     */
    /* package-private */ ContainerExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
    }

    public ContainerMirror link(Assembly assembly, Wirelet... wirelets) {
        return link(PackedContainerDriver.DEFAULT, assembly, wirelets);
    }

    /**
     * Links a new assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param realm
     *            realm
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     */
    public ContainerMirror link(ContainerDriver driver, Assembly assembly, Wirelet... wirelets) {
        PackedContainerDriver d = (PackedContainerDriver) driver; 
        
        // Create a new realm for the assembly
        AssemblyRealmSetup newRealm = new AssemblyRealmSetup(d, container, assembly, wirelets);

        container.realm.wirePrepare(); // check that the container is open for business

        // Close the new realm again after the assembly has been successfully linked
        newRealm.build();

        return (ContainerMirror) newRealm.container.mirror();
    }
}
